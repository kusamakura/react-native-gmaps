# React Native (Android) Google Maps

A (currently) very lightweight implementation of Google Maps for Android.

### Example Map

Your build your map with the following properties; they are all optional (it will default to simply zooming in on london without any props) apart from you must define a style with 'height' and 'width'.

``` js
let RNGMap = require('react-native-gmaps');

...

<RNGMap
  ref={'gmap'}
  style={ { height: 500, width: 500 } }
  markers={ [
        { coordinates: {lng: 0.1, lat: 51.0} },
        { coordinates: {lng: -0.1, lat: 51.0}, title: "Click marker to see this title!" }
    ] }
  zoomLevel={10}
  onMapChange={(e) => console.log(e)}
  onMapError={(e) => console.log('Map error --> ', e)}
  center={ { lng: 0.1, lat: 51.0 } } />
```

##### onMapChange

You will notice that you can pass a function to 'onMapChange'; this will be called after every time the map move - including when it initialises and zooms to it's initial point.

##### onMapError

Pass in a function to onMapError to respond to any errors thrown by gmaps. The only errors currently passed back are:

- **Map is null** - `{ message: 'Map is null', type: 'map_null' }`
  - You will get this error if you dont have Google Play APK installed.

- **Map init error** - `{ message: 'Map initialize error', 'map_init_error' }`
  - If an initialization error is caught then this will be passed.

### Methods

- **zoomOnMarkers** - Call this to zoom the map in on any markers you may have added.


### Install

#### Step 1 - Install Google Play APK

Check [here](https://developers.google.com/android/guides/setup) && [here](http://stackoverflow.com/questions/20121883/how-to-install-google-play-services-in-a-genymotion-vm-with-no-drag-and-drop-su) for guidance.

#### Step 2 - Install the npm package
```sh
$ npm install react-native-gmaps --save
```

#### Step 3 - Update Gradle Settings

```gradle
// file: android/settings.gradle
...

include ':react-native-gmaps', ':app'
project(':react-native-gmaps').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-gmaps/android/app')
```

#### Step 4 - Update app Gradle Build

```gradle
// file: android/app/build.gradle
...

dependencies {
    ...
    compile project(':react-native-gmaps')
}
```

#### Step 5 - Register React Package

```java
...
import com.rota.rngmaps.RNGMapsPackage; // <-- import

public class MainActivity extends FragmentActivity implements DefaultHardwareBackBtnHandler {

    private ReactInstanceManager mReactInstanceManager;
    private ReactRootView mReactRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReactRootView = new ReactRootView(this);
        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModuleName("index.android")
                .addPackage(new MainReactPackage())
                .addPackage(new RNGMapsPackage()) // <-- Register package here
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        mReactRootView.startReactApplication(mReactInstanceManager, "AwesomeProject", null);
        setContentView(mReactRootView);
    }
...

```

#### Step 6 - Add Google Maps to your Project

Add this to your AndroidManifest file; make sure it goes at the bottom of the `<application>` tag.

[More info on API Keys can be found here](https://developers.google.com/maps/documentation/android-api/signup?hl=en)

``` xml
// file: android/app/src/main/AndroidManifest.xml
<uses-library android:name="com.google.android.maps" />
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY"/>
```

... That should do it! Please let me know of any issues ...
