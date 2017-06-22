// implementation of AR-Experience (aka "World")
var World = {
	// true once data was fetched
	initiallyLoadedData: false,
    panelOpen: false,

    markerInFocus: null,
    isMarkerInFocus: false,
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
		World.markerDrawable_idle = new AR.ImageResource("assets/marker_idle_stretch.png");
		World.markerDrawable_selected = new AR.ImageResource("assets/marker_selected_stretch.png");
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
        	};
        	singlePoi.description = "Lorem ipsum dolor sit amet, ei justo commune vim, cu legere euripidis vulputate vim";
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

        $("#ar-btn").unbind();
        $("#ar-btn").click(function(){
            if(World.isInArea){
                //alert("it IS")
                if(!World.areaMarker.poiData.visited && !World.areaMarker.poiData.hasAR){

                    $("#backBtn").unbind();
                    $("#backBtn").click(function(){
                        World.resume();
                    });
                    World.areaMarker.setDeselected(World.areaMarker);
                    World.areaMarker.markerObject.enabled = false;

                    World.question = new QuestionObject(World.areaMarker.poiData);
                    World.isAnswering = true;
                }else if(World.areaMarker.poiData.hasAR){
                    //var architectSdkUrl = "architectsdk://GEOAR?id=" + encodeURIComponent(World.areaMarker.poiData.id);
                    //document.location = architectSdkUrl;
                    var args = {
                        action: "GEOAR",
                        id: World.areaMarker.poiData.id
                    };
                    World.areaMarker.setDeselected(World.areaMarker);
                    AR.platform.sendJSONObject(args);
                }
             /*   $("#panel-poidetail").slideToggle();
                World.panelOpen = false;
                World.currentMarker = null;
                World.areaMarker.enabled = false;*/
            }
        });
        //if(!World.isMarkerInFocus)
		    //World.focusScene(markerInFocus)
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
        var $button = $("#ar-btn");
        var $clone = $button.clone();
        $button.button();

        $clone.css({"color":"#A9A9A9"});
        $clone.button();
        $button.replaceWith($clone);
        //$("#ar-btn").css({"color":"#A9A9A9"});
        //$("#ar-btn").button().button("refresh");

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
        $("#closeBtn").unbind();
		$("#closeBtn").click(function(){

			World.currentMarker.setDeselected(World.currentMarker);
            $("#panel-poidetail").slideToggle();
            World.panelOpen = false;
            World.currentMarker = null;
		});
        $("#open-details-activity").unbind();
		$("#open-details-activity").click(function(){
            //var architectSdkUrl = "architectsdk://details?id=" + encodeURIComponent(marker.poiData.id);
            /*
                The urlListener of the native project intercepts this call and parses the arguments.
                This is the only way to pass information from JavaSCript to your native code.
                Ensure to properly encode and decode arguments.
                Note: you must use 'document.location = "architectsdk://...' to pass information from JavaScript to native.
                ! This will cause an HTTP error if you didn't register a urlListener in native architectView !
            */
            //document.location = architectSdkUrl;

            var args = {
                action: "details",
                id: marker.poiData.id
            };
            AR.platform.sendJSONObject(args);
		});
        $("#open-map").unbind();
		$("#open-map").click(function(){
            //var architectSdkUrl = "architectsdk://map?id=" + encodeURIComponent(marker.poiData.id);
            //document.location = architectSdkUrl;
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
        }/*
        if(!marker.poiData.visited && World.isInArea){
            $("#ar-btn").css({color:'#000000'});
            $("#ar-btn").button().button("refresh");
        }else{
            //$("#ar-btn").css({color:'#A9A9A9'});
            //$("#ar-btn").button().button("refresh");
        }
        */
        $("#panel-poidetail").slideToggle();
    	World.currentMarker = marker;
    },

    userEnteredArea: function userEnteredAreaFn(areaId){
        World.isInArea = true
        World.currentArea = areaId;
        if(World.initiallyLoadedData && !World.isMarkerInFocus){
            World.areaMarker = World.minimizeRest(areaId);
            /*if(!World.areaMarker.poiData.visited || World.areaMarker.poiData.hasAR){
                $("#ar-btn").css({'color':'#ff000000'}).button().button("refresh");
            }*/
        }
    },

    userLeftArea: function userLeftAreaFn(areaId){
        World.isInArea = false;
        World.currentArea = null;
        if(!World.isAnswering){
            World.areaMarker = null;
            World.restoreRest();
            //$("#ar-btn").css({'color':'#A9A9A9'}).button().button("refresh");
        }
    },
    // screen was clicked but no geo-object was hit
    onScreenClick: function onScreenClickFn() {
    },

    questionAnswered: function questionAnsweredFn(success){
        if(success){
            //update SCORE
            //alert("Answered Correctly!")
            //document.location = "architectsdk://score?id=" + encodeURIComponent(World.question.poiData.id)+"&success=true";
            var args = {
                action: "score",
                id: World.question.poiData.id,
                success: true
            };
            AR.platform.sendJSONObject(args);
            World.resume();
        }else{
            //alert("Nope!")
            //update SCORE NEGATIVE
            //document.location = "architectsdk://score?id=" + encodeURIComponent(World.question.poiData.id)+"&success=false";
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
            World.question.GeoObject.destroy();
        }
        if(World.isInArea){
            World.areaMarker.markerObject.enabled = true;
        }else{
            for(var it=0; it < World.markerList.length;it++){
                World.markerList[it].markerObject.enabled = true;
            }
        }
    },
    triggerQuestion: function triggerQuestionFn(args){
        var singlePoi = {
        	"id": args.id,
        	"latitude": parseFloat(args.latitude),
        	"longitude": parseFloat(args.longitude),
        	"title": args.name,
        	"description": args.description
        };
        World.userEnteredArea(singlePoi.id);
    },

    focusScene: function focusSceneFn(scene_id){
        World.markerInFocus = scene_id;
        if(World.initiallyLoadedData){
            World.isMarkerInFocus = true;
            alert("markerToFocus:"+ World.markerInFocus + "isInFocus: " + World.isMarkerInFocus);
            World.hideRest(scene_id);
        }
    },
    clearFocus: function clearFocusFn(){
        World.showAll();
        World.markerInFocus = null;
        World.isMarkerInFocus = false;
    },

    hideRest: function hideRestFn(scene_id){
        var marker = null;
        for(var i=0; i < World.markerList.length; i++){
            World.markerList[i].markerObject.enabled = false;
            if(World.markerList[i].poiData.id == scene_id){
                World.markerList[i].markerObject.enabled = true;
                marker =  World.markerList[i];
            }
        }
        return marker;
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
                    var animX = new AR.PropertyAnimation(World.markerList[i].markerObject.drawables.cam[j], 'scale.x', null, 0.3, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
                        amplitude: 2.0
                    }));
                    var animY = new AR.PropertyAnimation(World.markerList[i].markerObject.drawables.cam[j], 'scale.y', null, 0.3, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
                        amplitude: 2.0
                    }));
                    animList.push(animX);
                    animList.push(animY);
                }
            }
        }
        animate = new AR.AnimationGroup(AR.CONST.ANIMATION_GROUP_TYPE.PARALLEL, animList);
        animate.start()
        return marker;
    },
    restoreRest: function showAll(){
        for(var i=0; i < World.markerList.length; i++){
            World.markerList[i].markerObject.scale.x = 1;
            World.markerList[i].markerObject.scale.y = 1;
        }
    },
    showAll: function showAll(){
        for(var i=0; i < World.markerList.length; i++){
            World.markerList[i].markerObject.enabled = true;
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