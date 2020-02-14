// implementation of AR-Experience (aka "World")
var World = {
	// true once data was fetched
	initiallyLoadedData: false,
    panelOpen: false,
	// POI-Marker asset
	markerDrawable_idle: null,
	markerDrawable_selected: null,

    question:null,
    isAnswering:false,
	// The last selected marker
	currentMarker: null,
    // Last Area Triggered
    isInArea: false,
	currentArea: 0,
    areaMarker:null,
    //POI-Marker List
	markerList: [],
	locationUpdateCounter:0,
	userLocation: null,
	Player: null,

	// called to inject new POI data
	loadPoisFromJsonData: function loadPoisFromJsonDataFn(poiData) {

		// show radar & set click-listener
		PoiRadar.show();
        PoiRadar.setMaxDistance(Math.max(100, 1));
		$('#radarContainer').unbind('click');
		$("#radarContainer").click(PoiRadar.clickedRadar);

		World.markerList = [];
		/*
			The example Image Recognition already explained how images are loaded and displayed in the augmented reality view.
			This sample loads an AR.ImageResource when the World variable was defined.
			It will be reused for each marker that we will create afterwards.
		*/
		World.markerDrawable_idle = new AR.ImageResource("assets/marker_idle_colored.png");
		World.markerDrawable_selected = new AR.ImageResource("assets/marker_selected_colored.png");
		World.markerDrawable_idle_q = new AR.ImageResource("assets/marker_idle_stretch.png");
		World.markerDrawable_selected_q = new AR.ImageResource("assets/marker_selected_stretch.png");
		/*
			For creating the marker a new object AR.GeoObject will be created at the specified geolocation.
			An AR.GeoObject connects one or more AR.GeoLocations with multiple AR.Drawables.
			The AR.Drawables can be defined for multiple targets. A target can be the camera, the radar or a direction indicator.
			Both the radar and direction indicators will be covered in more detail in later examples.
		*/
		if(poiData.length == 0){
		    World.updateStatusMessage('No Scenes in your Area');
		    return;
		}
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
        		"hasAR":poiData[currentPlaceNr].hasAR,
        		"thumbnail":poiData[currentPlaceNr].thumb_uri
        	};
        	/*
        		To be able to deselect a marker while the user taps on the empty screen,
        		the World object holds an array that contains each marker.
        	*/
        	World.markerList.push(new Marker(singlePoi));
        }
		World.initiallyLoadedData = true;
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
		if(World.markerList.length > 0)
		    $("#info-footer").hide();

		 World.updateDistanceToUserValues();
	},

	// location updates, fired every time you call architectView.setLocation() in native environment
	locationChanged: function locationChangedFn(lat, lon, alt, acc) {
	    World.userLocation = {
    			'latitude': lat,
    			'longitude': lon,
    			'altitude': alt,
    			'accuracy': acc
    		};

        World.locationUpdateCounter++;
	    if (World.locationUpdateCounter === 10 && World.initiallyLoadedData) {
    			// update placemark distance information frequently, you max also update distances only every 10m with some more effort
    			World.updateDistanceToUserValues();
                World.locationUpdateCounter = 0;
    	}
	},
    onMarkerDeSelected: function onMarkerDeSelectedFn(marker){
        $("#panel-poidetail").slideToggle();
        panelOpen = false;
        World.currentMarker = null;
    },
	// fired when user pressed maker in cam
    onMarkerSelected: function onMarkerSelectedFn(marker) {
        $("#play-ar").hide();
        $("#play-inst").hide();
        $("#poi-detail-description").hide();
        $("#answer-ar").hide();
		/*
			In this sample a POI detail panel appears when pressing a cam-marker (the blue box with title & description),
			compare index.html in the sample's directory.
		*/
		// update panel values
		$("#poi-detail-title").html(marker.poiData.title);
		var descr = marker.poiData.description.trunc(100);

		if( marker.poiData.visited == true ){
		    $("#poi-detail-description").html(descr);
            $("#poi-detail-description").show();
		}else if(marker.poiData.hasAR == false){
            $("#answer-ar").show();
            $("#answer-ar").unbind();
            $("#answer-ar").click(function(){
                if(World.isInArea){
                    $("#backBtn").unbind();
                    $("#backBtn").click(function(){
                        World.resume();
                    });
                    World.currentMarker.setDeselected(World.currentMarker);
                    World.areaMarker.setDeselected(World.areaMarker);
                    World.areaMarker.markerObject.enabled = false;
                    $("#panel-poidetail").slideToggle();

                    World.question = new QuestionObject(World.areaMarker.poiData);
                    World.isAnswering = true;
                }else{
                    alert("You need to get closer!")
                }
            });
		}
		if( marker.poiData.hasAR == true){
            $("#play-ar").show();
            $("#play-ar").unbind();
            $("#play-ar").click(function(){
                if(World.isInArea){
                    var args = {
                        action: "GEOAR",
                        id: World.areaMarker.poiData.id
                    };
                    World.areaMarker.setDeselected(World.areaMarker);
                    AR.platform.sendJSONObject(args);
                }else{
                    alert("You need to get closer!")
                }
            });
            $("#play-inst").show();
            $("#play-inst").unbind();
            $("#play-inst").click(function(){
                if(World.isInArea){
                    var args = {
                        action: "INSTANT",
                        id: World.areaMarker.poiData.id
                    };
                    World.areaMarker.setDeselected(World.areaMarker);
                    AR.platform.sendJSONObject(args);
                }else{
                    alert("You need to get closer!")
                }
            });
		}

		// It's ok for AR.Location subclass objects to return a distance of `undefined`. In case such a distance was calculated when all distances were queried in `updateDistanceToUserValues`, we recalculate this specific distance before we update the UI.
		if( undefined == marker.distanceToUser ) {
			marker.distanceToUser = marker.markerObject.locations[0].distanceToUser();
		}
		// distance and altitude are measured in meters by the SDK. You may convert them to miles / feet if required.
		var distanceToUserValue = (marker.distanceToUser > 999) ? ((marker.distanceToUser / 1000).toFixed(2) + " km") : (Math.round(marker.distanceToUser) + " m");

		$("#poi-detail-distance").html(distanceToUserValue);

		$(".ui-panel-dismiss").unbind("mousedown");
		// deselect AR-marker when user exits detail screen div.
        $("#closeBtn").unbind();
		$("#closeBtn").click(function(){
			World.currentMarker.setDeselected(World.currentMarker);
            $("#panel-poidetail").slideToggle();
            World.panelOpen = false;
            World.currentMarker = null;
		});
        $("#open-details-activity").unbind();
		$("#open-details-activity").click(function(){
            var args = {
                action: "details",
                id: marker.poiData.id
            };
            AR.platform.sendJSONObject(args);
		});
        $("#open-map").unbind();
		$("#open-map").click(function(){
            var args = {
                action: "map",
                id: marker.poiData.id
            };
            AR.platform.sendJSONObject(args);
		});
        $("#mark-place").unbind();
        if(marker.poiData.saved){
            $("#mark-place").buttonMarkup({theme: "b"});
        }
        else{
            $("#mark-place").buttonMarkup({theme: "d"});
        }
		$("#mark-place").click(function(){
                    var themeToUse = ($("#mark-place").attr("data-theme") == "d"? "b" : "d");
                    $("#mark-place").buttonMarkup({
                        theme: themeToUse
                    });
                    World.currentMarker.poiData.saved = true;
                    //var architectSdkUrl = "architectsdk://mark?id=" + encodeURIComponent(marker.poiData.id);
                    //document.location = architectSdkUrl;
                    var args = {
                        action: "mark",
                        id: marker.poiData.id
                    };
                    AR.platform.sendJSONObject(args);
        });


        if(World.currentMarker != null){
            if (World.currentMarker.poiData.id == marker.poiData.id) {
                   return;
            }else{
                   World.currentMarker.setDeselected(World.currentMarker);
                   World.currentMarker = marker;
                   return;
            }
        }

        $("#panel-poidetail").slideToggle();
    	World.currentMarker = marker;
    },

    userEnteredArea: function userEnteredAreaFn(areaId){
        World.isInArea = true
        World.currentArea = areaId;
        if(World.initiallyLoadedData ){
            World.areaMarker = World.minimizeRest(areaId);
        }else{
            setTimeout(function(){
                World.userEnteredArea(World.currentArea);
            }, 3000);
        }
    },

    userLeftArea: function userLeftAreaFn(areaId){
        World.isInArea = false;
        World.currentArea = null;
        if(!World.isAnswering){
            World.areaMarker = null;
            World.restoreRest();
        }
    },
    // screen was clicked but no geo-object was hit
    onScreenClick: function onScreenClickFn() {
    },

    questionAnswered: function questionAnsweredFn(success){
        if(success){
            $("#score").unbind();
            $("#score").click();
            $("#positionOrigin").html("+250");
            setTimeout(function(){
                World.initPlayerIconPopup();
                $("#positionOrigin").popup("close");
                World.resume();
            }, 1000);
            World.areaMarker.poiData.visited = true;
            var args = {
                action: "score",
                id: World.question.poiData.id,
                success: true
            };
            AR.platform.sendJSONObject(args);
        }else{
            $("#score").unbind();
            $("#score").click();
            $("#positionOrigin").html("-250");
            setTimeout(function(){
                World.initPlayerIconPopup();
                $("#positionOrigin").popup("close");
            }, 1000);
            var args = {
                action: "score",
                id: World.question.poiData.id,
                success: false
            };
            AR.platform.sendJSONObject(args);
        }
    },
    resume: function resumeFn(){
        if(World.isAnswering){
            World.isAnswering = false;
            World.question.GeoObject.enabled = false;
            World.question.GeoObject.destroy();
        }

        if(World.isInArea){
            World.areaMarker.markerObject.enabled = true;
            $("#panel-poidetail").slideToggle();
        }
        World.restoreRest();
    },
    minimizeRest: function scaleRestFn(scene_id){
        var marker = null;
        var animate = null;
        var animList = [];
        for(var i=0; i < World.markerList.length; i++){
            for(var j=0; j < World.markerList[i].markerObject.drawables.cam.length;j++ ){
                if(World.markerList[i].poiData.id == scene_id){
                     marker =  World.markerList[i];
                }else{
                    if( World.markerList[i].markerObject.drawables.cam[j].enabled = true ){
                        var animX = new AR.PropertyAnimation(World.markerList[i].markerObject.drawables.cam[j], 'scale.x', null, 0.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
                            amplitude: 2.0
                        }));
                        var animY = new AR.PropertyAnimation(World.markerList[i].markerObject.drawables.cam[j], 'scale.y', null, 0.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
                            amplitude: 2.0
                        }));
                        animList.push(animX);
                        animList.push(animY);
                    }
                }
            }
        }
        animate = new AR.AnimationGroup(AR.CONST.ANIMATION_GROUP_TYPE.PARALLEL, animList);
        animate.start();
        return marker;
    },
    restoreRest: function showAll(){
        var animList = [];
        var animate = null;
        for(var i=0; i < World.markerList.length; i++){
            for(var j=0; j < World.markerList[i].markerObject.drawables.cam.length;j++ ){
               var animX = new AR.PropertyAnimation(World.markerList[i].markerObject.drawables.cam[j], 'scale.x', null, 1, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
                   amplitude: 2.0
               }));
               var animY = new AR.PropertyAnimation(World.markerList[i].markerObject.drawables.cam[j], 'scale.y', null, 1, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
                   amplitude: 2.0
               }));
               animList.push(animX);
               animList.push(animY);
            }
        }
        animate = new AR.AnimationGroup(AR.CONST.ANIMATION_GROUP_TYPE.PARALLEL, animList);
        animate.start();
    },
    // sets/updates distances of all makers so they are available way faster than calling (time-consuming) distanceToUser() method all the time
    updateDistanceToUserValues: function updateDistanceToUserValuesFn() {
    	for (var i = 0; i < World.markerList.length; i++) {
    		World.markerList[i].distanceToUser = World.markerList[i].markerObject.locations[0].distanceToUser();
            // distance and altitude are measured in meters by the SDK. You may convert them to miles / feet if required.
            var distanceToUserValue = (World.markerList[i].distanceToUser > 999) ? ((World.markerList[i].distanceToUser / 1000).toFixed(2) + " km") : (Math.round(World.markerList[i].distanceToUser) + " m");
    	    World.markerList[i].distanceLabel.text = distanceToUserValue;
    	}
    },
    InjectPlayer: function injectPlayerFn(player){
        var tempPlayer = {
            	"username": player.username,
            	"firstname": player.firstname,
            	"lastname": player.lastname,
            	"score": player.score
            };
        World.Player = tempPlayer;
        World.initPlayerIconPopup();
    },
    initPlayerIconPopup: function initPlayerIconPopupFn(){
            $("#score").unbind();
            $("#score").click(function(){
                $("#positionOrigin").html("<h3>"+World.Player.username+"</h3><p>Score: "+World.Player.score+"</p>");
            });
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
