
package com.rota.rngmaps;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.CatalystStylesDiffMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIProp;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Henry on 08/10/2015.
 */

public class RNGMapsModule extends SimpleViewManager<MapView> {
    public static final String REACT_CLASS = "RNGMaps";

    private MapView mView;
    private GoogleMap map;
    private ReactContext reactContext;
    private ArrayList<Marker> mapMarkers = new ArrayList<Marker>();

    @UIProp(UIProp.Type.MAP)
    public static final String PROP_CENTER = "center";

    @UIProp(UIProp.Type.NUMBER)
    public static final String PROP_ZOOM_LEVEL = "zoomLevel";

    @UIProp(UIProp.Type.ARRAY)
    public static final String PROP_MARKERS = "markers";

    @UIProp(UIProp.Type.BOOLEAN)
    public static final String PROP_ZOOM_ON_MARKERS = "zoomOnMarkers";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected MapView createViewInstance(ThemedReactContext context) {
        reactContext = context;
        mView = new MapView(context);
        mView.onCreate(null);
        mView.onResume();
        map = mView.getMap();

        if (map == null) {
            sendMapError("Map is null", "map_null");
        } else {
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setMyLocationEnabled(true);

            try {
                MapsInitializer.initialize(context.getApplicationContext());
                map.setOnCameraChangeListener(getCameraChangeListener());
            } catch (Exception e) {
                e.printStackTrace();
                sendMapError("Map initialize error", "map_init_error");
            }
        }

        return mView;
    }

    private void sendMapError(String message, String type) {
        WritableMap error = Arguments.createMap();
        error.putString("message", message);
        error.putString("type", type);

        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("mapError", error);
    }

    private GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                WritableMap params = Arguments.createMap();
                WritableMap latLng = Arguments.createMap();
                latLng.putDouble("lat", position.target.latitude);
                latLng.putDouble("lng", position.target.longitude);

                params.putMap("latLng", latLng);
                params.putDouble("zoomLevel", position.zoom);

                reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("mapChange", params);
            }
        };
    }

    private Boolean updateCenter(CatalystStylesDiffMap props) {
        try {
            CameraUpdate cameraUpdate;
            Double lng = props.getMap(PROP_CENTER).getDouble("lng");
            Double lat = props.getMap(PROP_CENTER).getDouble("lat");

            if (props.hasKey(PROP_ZOOM_LEVEL)) {
                int zoomLevel = props.getInt(PROP_ZOOM_LEVEL, 10);
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel);
            } else {
                cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
            }

            map.animateCamera(cameraUpdate);

            return true;
        } catch (Exception e) {
            // ERROR!
            e.printStackTrace();
            return false;
        }
    }

    private Boolean updateMarkers(CatalystStylesDiffMap props) {
        try {

            // First clear all markers from the map
            for (Marker marker : mapMarkers) {
                marker.remove();
            }
            mapMarkers.clear();

            // All markers to map
            for (int i = 0; i < props.getArray(PROP_MARKERS).size(); i++) {
                MarkerOptions options = new MarkerOptions();
                ReadableMap marker = props.getArray(PROP_MARKERS).getMap(i);
                if (marker.hasKey("coordinates")) {

                    options.position(new LatLng(
                                    marker.getMap("coordinates").getDouble("lat"),
                                    marker.getMap("coordinates").getDouble("lng")
                            )
                    );

                    if (marker.hasKey("title")) {
                        options.title(marker.getString("title"));
                    }
                    mapMarkers.add(map.addMarker(options));

                } else break;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Boolean zoomOnMarkers() {
        try {
            int padding = 150;

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : mapMarkers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            map.animateCamera(cu);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void updateView(MapView view, CatalystStylesDiffMap props) {
        super.updateView(view, props);
        if (props.hasKey(PROP_CENTER)) updateCenter(props);
        if (props.hasKey(PROP_ZOOM_LEVEL)) updateCenter(props);
        if (props.hasKey(PROP_MARKERS)) updateMarkers(props);
        if (props.hasKey(PROP_ZOOM_ON_MARKERS) && props.getBoolean(PROP_ZOOM_ON_MARKERS, false)) {
            zoomOnMarkers();
        }

    }
}
