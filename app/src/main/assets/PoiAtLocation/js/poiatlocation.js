// implementation of AR-Experience (aka "World")
var World = {
	// true once data was fetched
	initiallyLoadedData: false,
    panelOpen: false,
	// POI-Marker asset
	markerDrawable_idle: null,
	markerDrawable_selected: null,

	// The last selected marker
	currentMarker: null,

    //POI-Marker List
	markerList: [],

	// called to inject new POI data
	loadPoisFromJsonData: function loadPoisFromJsonDataFn(poiData) {

		World.markerList = [];
		/*
			The example Image Recognition already explained how images are loaded and displayed in the augmented reality view.
			This sample loads an AR.ImageResource when the World variable was defined.
			It will be reused for each marker that we will create afterwards.
		*/
		World.markerDrawable_idle = new AR.ImageResource("assets/marker_idle.png");
		World.markerDrawable_selected = new AR.ImageResource("assets/marker_selected.png");
		/*
			For creating the marker a new object AR.GeoObject will be created at the specified geolocation.
			An AR.GeoObject connects one or more AR.GeoLocations with multiple AR.Drawables.
			The AR.Drawables can be defined for multiple targets. A target can be the camera, the radar or a direction indicator.
			Both the radar and direction indicators will be covered in more detail in later examples.
		*/
		// loop through POI-information and create an AR.GeoObject (=Marker) per POI
        for (var currentPlaceNr = 0; currentPlaceNr < poiData.length; currentPlaceNr++) {
        	var singlePoi = {
        		"id": poiData[currentPlaceNr].id,
        		"latitude": parseFloat(poiData[currentPlaceNr].latitude),
        		"longitude": parseFloat(poiData[currentPlaceNr].longitude),
        		"title": poiData[currentPlaceNr].name,
        		"description": poiData[currentPlaceNr].description
        	};
        	/*
        		To be able to deselect a marker while the user taps on the empty screen,
        		the World object holds an array that contains each marker.
        	*/
        	World.markerList.push(new Marker(singlePoi));
        }
		World.updateStatusMessage(currentPlaceNr + ' places loaded');
	},

	// updates status message shown in small "i"-button aligned bottom center
	updateStatusMessage: function updateStatusMessageFn(message, isWarning) {
		var themeToUse = isWarning ? "e" : "c";
		var iconToUse = isWarning ? "alert" : "info";
		$("#status-message").html(message);
		$("#popupInfoButton").buttonMarkup({
			theme: themeToUse
		});
		$("#popupInfoButton").buttonMarkup({
			icon: iconToUse
		});
	},

	// location updates, fired every time you call architectView.setLocation() in native environment
	locationChanged: function locationChangedFn(lat, lon, alt, acc) {
		/*
			The custom function World.onLocationChanged checks with the flag World.initiallyLoadedData if the function was already called.
			With the first call of World.onLocationChanged an object that contains geo information will be created which will be later used
			to create a marker using the World.loadPoisFromJsonData function.
		*/
	},

	// fired when user pressed maker in cam
    onMarkerSelected: function onMarkerSelectedFn(marker) {
        if(World.currentMarker != null){
            World.currentMarker.setDeselected(World.currentMarker);
        }
        World.currentMarker = marker;

		/*
			In this sample a POI detail panel appears when pressing a cam-marker (the blue box with title & description),
			compare index.html in the sample's directory.
		*/
		// update panel values
		$("#poi-detail-title").html(marker.poiData.title);
		$("#poi-detail-description").html(marker.poiData.description);


		// It's ok for AR.Location subclass objects to return a distance of `undefined`. In case such a distance was calculated when all distances were queried in `updateDistanceToUserValues`, we recalculate this specific distance before we update the UI.
		if( undefined == marker.distanceToUser ) {
			marker.distanceToUser = marker.markerObject.locations[0].distanceToUser();
		}

		// distance and altitude are measured in meters by the SDK. You may convert them to miles / feet if required.
		var distanceToUserValue = (marker.distanceToUser > 999) ? ((marker.distanceToUser / 1000).toFixed(2) + " km") : (Math.round(marker.distanceToUser) + " m");

		$("#poi-detail-distance").html(distanceToUserValue);

		// show panel
		//$("#panel-poidetail").panel("open");
		$("#panel-poidetail").slideToggle();
        World.panelOpen = true;
		$(".ui-panel-dismiss").unbind("mousedown");

		// deselect AR-marker when user exits detail screen div.
		$("#panel-poidetail").on("panelbeforeclose", function(event, ui) {
			World.currentMarker.setDeselected(World.currentMarker);
            World.panelOpen = false;
		});
    	/*// deselect previous marker
    	if (World.currentMarker) {
    		if (World.currentMarker.poiData.id == marker.poiData.id) {
    			return;
    		}
    		World.currentMarker.setDeselected(World.currentMarker);
    	}

    	// highlight current one
    	marker.setSelected(marker);
    	World.currentMarker = marker;*/
    },

    // screen was clicked but no geo-object was hit
    onScreenClick: function onScreenClickFn() {
    	if (World.currentMarker) {
    		World.currentMarker.setDeselected(World.currentMarker);
    	}
        if(World.panelOpen){
		    $("#panel-poidetail").slideToggle();
		    World.panelOpen = false;
        }
    }
};

/* 
	Set a custom function where location changes are forwarded to.
	There is also a possibility to set AR.context.onLocationChanged to null.
	In this case the function will not be called anymore and no further location updates will be received.
*/
AR.context.onLocationChanged = World.locationChanged;
/*
	To detect clicks where no drawable was hit set a custom function on AR.context.onScreenClick where the currently selected marker is deselected.
*/
AR.context.onScreenClick = World.onScreenClick;