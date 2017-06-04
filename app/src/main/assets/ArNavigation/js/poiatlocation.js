// implementation of AR-Experience (aka "World")
var World = {
	// true once data was fetched
	initiallyLoadedData: false,
    panelOpen: false,
	// POI-Marker asset
	markerDrawable_idle: null,
	markerDrawable_selected: null,

    question:null,
	// The last selected marker
	currentMarker: null,
    // Last Area Triggered
    isInArea: false,
	currentArea: 0,
    areaMarker:null,
    //POI-Marker List
	markerList: [],

	// called to inject new POI data
	loadPoisFromJsonData: function loadPoisFromJsonDataFn(poiData) {

		// show radar & set click-listener
		PoiRadar.show();
        PoiRadar.setMaxDistance(Math.max(400, 1));
		$('#radarContainer').unbind('click');
		$("#radarContainer").click(PoiRadar.clickedRadar);

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
        		"period_id": poiData[currentPlaceNr].period_id,
        		"description": poiData[currentPlaceNr].description,
        		"visited":poiData[currentPlaceNr].visited,
        		"saved":poiData[currentPlaceNr].saved,
        	};
        	singlePoi.description = "Lorem ipsum dolor sit amet, ei justo commune vim, cu legere euripidis vulputate vim";
        	/*
        		To be able to deselect a marker while the user taps on the empty screen,
        		the World object holds an array that contains each marker.
        	*/
        	World.markerList.push(new Marker(singlePoi));
        }
		World.updateStatusMessage(currentPlaceNr + ' places loaded');
		World.initiallyLoadedData = true;
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
		$("#info-footer").hide();
	},

	// location updates, fired every time you call architectView.setLocation() in native environment
	locationChanged: function locationChangedFn(lat, lon, alt, acc) {
		/*
			The custom function World.onLocationChanged checks with the flag World.initiallyLoadedData if the function was already called.
			With the first call of World.onLocationChanged an object that contains geo information will be created which will be later used
			to create a marker using the World.loadPoisFromJsonData function.
		*/
	},
    onMarkerDeSelected: function onMarkerDeSelectedFn(marker){
        $("#panel-poidetail").slideToggle();
        panelOpen = false;
        $("#ar-panel").hide();

        World.currentMarker = null;
    },
	// fired when user pressed maker in cam
    onMarkerSelected: function onMarkerSelectedFn(marker) {
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

		$(".ui-panel-dismiss").unbind("mousedown");
		// deselect AR-marker when user exits detail screen div.
		$("#panel-poidetail").on("panelbeforeclose", function(event, ui) {
			World.currentMarker.setDeselected(World.currentMarker);
		});
		$("#closeBtn").click(function(){
			World.currentMarker.setDeselected(World.currentMarker);
            $("#panel-poidetail").slideToggle();
            panelOpen = false;
            World.currentMarker = null;
		});
		$("#open-details-activity").click(function(){
            var architectSdkUrl = "architectsdk://details?id=" + encodeURIComponent(marker.poiData.id);
            /*
                The urlListener of the native project intercepts this call and parses the arguments.
                This is the only way to pass information from JavaSCript to your native code.
                Ensure to properly encode and decode arguments.
                Note: you must use 'document.location = "architectsdk://...' to pass information from JavaScript to native.
                ! This will cause an HTTP error if you didn't register a urlListener in native architectView !
            */
            document.location = architectSdkUrl;
		});

		$("#open-map").click(function(){
            var architectSdkUrl = "architectsdk://map?id=" + encodeURIComponent(marker.poiData.id);

            document.location = architectSdkUrl;
		});
		$("#mark-place").click(function(){
                    var themeToUse = ($("#mark-place").attr("data-theme") == "d"? "b" : "d");
                    $("#mark-place").buttonMarkup({
                        theme: themeToUse
                    });
                    var architectSdkUrl = "architectsdk://mark?id=" + encodeURIComponent(marker.poiData.id);
                    document.location = architectSdkUrl;
        });

        $("#ar-btn").click(function(){
                World.question = new QuestionObject(marker.poiData);
                World.areaMarker.enabled = false;
        });

		if (World.currentMarker != null) {
            if (World.currentMarker.poiData.id == marker.poiData.id) {
                return;
            }
            World.currentMarker.setDeselected(World.currentMarker);
            $("#panel-poidetail").slideToggle();
            panelOpen = false;
            if(!marker.poiData.visited && World.currentArea == marker.poiData.id){
                $("#ar-panel").show();
            }else{
                $("#ar-panel").hide();
            }
        }
        if(!World.panelOpen){
            $("#panel-poidetail").slideToggle();
            panelOpen = true;
            if(!marker.poiData.visited && World.currentArea == marker.poiData.id){
                $("#ar-panel").show();
            }else{
                $("#ar-panel").hide();
            }
        }
    	marker.setSelected(marker);
    	World.currentMarker = marker;
    },

    userEnteredArea: function userEnteredAreaFn(areaId){
        World.isInArea = true
        World.currentArea = areaId;
        if(World.initiallyLoadedData){
            for(var i=0; i < World.markerList.length; i++){
                World.markerList[i].markerObject.enabled = false;
                if(World.markerList[i].poiData.id == areaId){
                    World.markerList[i].markerObject.enabled = true;
                    World.areaMarker = World.markerList[i].markerObject;
                }
            }
        }
    },

    userLeftArea: function userLeftAreaFn(areaId){
        World.currentArea = null;
        World.isInArea = false;
        World.areaMarker = null;
        for(var i=0; i < World.markerList.length; i++){
            World.markerList[i].markerObject.enabled = true;
        }
    },
    // screen was clicked but no geo-object was hit
    onScreenClick: function onScreenClickFn() {
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