'use strict';

let React = require('react-native');

let {
  View,
  Component,
  requireNativeComponent,
  PropTypes,
  DeviceEventEmitter,
  Image
} = React;

/* RNGMAPS COMP */
var gmaps = {
  name: 'RNGMaps',
  propTypes: {
    center: PropTypes.object,
    zoomLevel: PropTypes.number,
    markers: PropTypes.array,
    zoomOnMarkers: PropTypes.bool,

    /* Hackedy hack hack hack */
    scaleX: React.PropTypes.number,
    scaleY: React.PropTypes.number,
    translateX: React.PropTypes.number,
    translateY: React.PropTypes.number,
    rotation: React.PropTypes.number,
  },
};
var colors = {
    HUE_AZURE   : 210.0,
    HUE_BLUE    : 240.0,
    HUE_CYAN    : 180.0,
    HUE_GREEN   : 120.0,
    HUE_MAGENTA : 300.0,
    HUE_ORANGE  : 30.0,
    HUE_RED     : 0.0,
    HUE_ROSE    : 330.0,
    HUE_VIOLET  : 270.0,
    HUE_YELLOW  : 60.0,
  };

let MapView = requireNativeComponent('RNGMaps', gmaps);

class RNGMaps extends Component {
  constructor (props) {
    super(props);
    this._event = null;
    this.state = {
      zoomOnMarkers: false,
      markers: []
    }
  }

  componentDidMount () {
    this._event = DeviceEventEmitter.addListener('mapChange', (e: Event) => {
      this.props.onMapChange&&this.props.onMapChange(e);
    });

    this._error = DeviceEventEmitter.addListener('mapError', (e: Event) => {
      console.log(`[GMAP_ERROR]: ${e.message}`);
      this.props.onMapError&&this.props.onMapError(e);
    });

    this.updateMarkers(this.props.markers);
  }

  componentWillUnmount () {
    this._event&&this._event.remove();
    this._error&&this._error.remove();
  }

  zoomOnMarkers (bool) {
    // HACK: Bleurgh, forcing the change on zoomOnMarkers.
    this.setState({ zoomOnMarkers: null }, () => {
      this.setState({ zoomOnMarkers: bool||true });
    });
  }

  updateMarkers (markers) {
    let newMarkers = [];
    for (var i = 0; i < markers.length; i++) newMarkers.push(markers[i]);
    this.setState({ markers: newMarkers });
  }

  _diffMarkers (markersOne, markersTwo) {
    if(markersOne.length!==markersTwo.length) return true;
    for (let i = 0; i < markersOne.length; i++) {
      for (let prop in markersOne[i].coordinates) {
        if (markersOne[i].coordinates.hasOwnProperty(prop)) {
          if(markersOne[i].coordinates[prop] !== markersTwo[i].coordinates[prop]) return true;
        }
      }
    }
    return false;
  }

  componentWillReceiveProps (nextProps) {
    if(this._diffMarkers(nextProps.markers, this.state.markers)) {
      this.updateMarkers(nextProps.markers);
    }
  }

  render () {
    return ( <MapView
      { ...this.props }
      markers={ this.state.markers }
      zoomOnMarkers={ this.state.zoomOnMarkers }
      />
    );
  }
}

module.exports = RNGMaps;
