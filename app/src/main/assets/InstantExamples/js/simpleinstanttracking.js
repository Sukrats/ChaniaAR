var World = {

    circle: null,
    viewport: null,
    scene: null,
    sceneList:[],
    modelList:[],
    location:null,
    currentModelShown: null,
    areas:[],
    scale:1,
    rotate:0,

    controlObject:null,
    camGeoDrawable:null,
    cHBearing: 0,
    cHDistance:0,
    modelBearing: 0,
    modelDistance: 0,
    inPosition: false,
    cHInit: false,
    modelInit: false,

    posX:0,
    posY:0,
    init: function initFn() {
        this.createOverlays();
    },

    createOverlays: function createOverlaysFn() {
        var crossHairsRedImage = new AR.ImageResource("assets/crosshairs_red.png");
        this.crossHairsRedDrawable = new AR.ImageDrawable(crossHairsRedImage, 1.0, {
            rotate : { x: 90}
        });

        var crossHairsBlueImage = new AR.ImageResource("assets/crosshairs_blue.png");
        var crossHairsBlueDrawable = new AR.ImageDrawable(crossHairsBlueImage, 1.0,{
            rotate : { x: 90}
        });

        var crossHairsPlayImage = new AR.ImageResource("assets/buttons/start.png");
        this.crossHairsPlayDrawable = new AR.ImageDrawable(crossHairsPlayImage, 2.0,{
            rotate : { x: 90}
        });
        World.camGeoDrawable = this.crossHairsPlayDrawable;

        this.tracker = new AR.InstantTracker({
            onChangedState:  function onChangedStateFn(state) {
            },
            // device height needs to be as accurate as possible to have an accurate scale
            // returned by the Wikitude SDK
            deviceHeight: 1.8,
            onError: function(errorMessage) {
                alert(errorMessage);
            }
        });

        this.instantTrackable = new AR.InstantTrackable(this.tracker, {
            drawables: {
                cam: crossHairsBlueDrawable,
                initialization: this.crossHairsRedDrawable
            },
            onTrackingStarted: function onTrackingStartedFn() {
                this.drawables.addCamDrawable(World.currentModelShown);
                // do something when tracking is started (recognized)
            },
            onTrackingStopped: function onTrackingStoppedFn() {
                this.drawables.removeCamDrawable(World.currentModelShown);
                // do something when tracking is stopped (lost)
            },
            onError: function(errorMessage) {
                alert(errorMessage);
            }
        });
    },

    changeTrackerState: function changeTrackerStateFn() {
        if (this.tracker.state === AR.InstantTrackerState.INITIALIZING ) {
            document.getElementById("tracking-start-stop-button").src = "assets/buttons/stop.png";
            this.tracker.state = AR.InstantTrackerState.TRACKING;
        } else {
            document.getElementById("tracking-start-stop-button").src = "assets/buttons/start.png";
            if(World.currentModelShown != null ){
                this.instantTrackable.drawables.removeCamDrawable(World.currentModelShown);
            }
            this.tracker.state = AR.InstantTrackerState.INITIALIZING;
        }
    },

    locationChanged: function locationChangedFn(lat, lon, alt, acc) {
        var location1 = {
            "lat": lat,
            "lon": lon
        };
        World.location = location1;
        /*
        	The custom function World.onLocationChanged checks with the flag World.initiallyLoadedData if the function was already called.
        	With the first call of World.onLocationChanged an object that contains geo information will be created which will be later used
        	to create a marker using the World.loadPoisFromJsonData function.
        */
    },
    getInstantiation: function getInstantiationFn(args){
        World.sceneList = [];
        for(var i =0; i < args.ar_scenes.length; i++){
            var ar = {
              "path": args.ar_scenes[i].path,
              "latitude": args.ar_scenes[i].latitude,
              "longitude": args.ar_scenes[i].longitude
            };
            World.sceneList.push(ar);
        }
        World.makeControlObject(World.sceneList[0]);
        var view = {
          "latitude": parseFloat(args.viewport.latitude),
          "longitude": parseFloat(args.viewport.longitude),
          "rotation": parseInt(args.viewport.rotation),
          "posX": parseFloat(args.viewport.translateX),
          "posY": parseFloat(args.viewport.translateY),
          "radius": parseInt(args.viewport.radius)
        };
        World.viewport = view;

        var singlePoi = {
        	"id": args.id,
        	"latitude": parseFloat(args.latitude),
        	"longitude": parseFloat(args.longitude),
        	"title": args.name,
        	"description": args.description,
        	"num": args.num
        };
        World.scene = singlePoi;
        World.initModels();
        World.init();
        World.initSlider();
    },
    initModels: function initModels(){
        World.modelList = [];
        for(var i=0; i < World.sceneList.length; i++ ){
            var model = new AR.Model(World.sceneList[i].path, {
               scale: {
                   x: 1.0,
                   y: 1.0,
                   z: 1.0
               },
               rotate: {
                   x: 0,
                   y: 0,
                   z: 0
               },
               onScaleChanged: function(scale) {
                   var scaleValue = World.scale * scale;
                   this.scale = {x: scaleValue, y: scaleValue, z: scaleValue};
               },
                onScaleEnded: function(scale) {
                    World.scale = this.scale.x;
               },
               onRotationChanged: function(angleInDegrees) {
                   this.rotate.z = World.rotate - angleInDegrees;
               },
               onRotationEnded: function(angleInDegrees) {
                  World.rotate = this.rotate.z
               },
               onClick: function(arObject,modelPart) {
                  alert("Rot: "+World.rotate + "\nScale: "+World.scale)
               }
            });
            World.modelList.push(model);
        }
    },
    initSlider: function initSlider(){
        if(World.modelList.length > 1){
            $("#model-slider-container").css("display", "inline-block");
            $("#model-slider").prop({
                min: 1,
                max: World.sceneList.length
            }).slider("refresh");

            $("#model-slider").on( "slidestop", function( event, ui ) {
                var val = $("#model-slider").val();
                World.setModel(val);
            } );

            var val = $("#model-slider").val();
            World.setModel(val);
        }else{
            World.setModel(1);
        }
    },

    isTracking: function isTrackingFn() {
        return (this.tracker.state === AR.InstantTrackerState.TRACKING);
    },

    setModel: function setModelFn(index){
        if (World.isTracking()) {
            World.scale = 1;
            World.rotate = 0;
            if(World.currentModelShown != null){
                this.instantTrackable.drawables.removeCamDrawable(World.currentModelShown);
            }
            World.currentModelShown = World.modelList[index-1];
            this.instantTrackable.drawables.addCamDrawable(World.currentModelShown);
        }else{
            World.currentModelShown = World.modelList[index-1];
        }
    },

    OnCrosshairPositionChange: function( bearing, distance ){
        World.cHBearing = bearing;
        World.cHDistance = distance;
        if(!World.cHInit)
            World.cHInit = true;
        $("#bearing-text").html("Bearing: "+bearing+"\nDistance: "+ distance);
        World.calcPointingPosition();
    },

    UpdateUserPosition: function ( bearing, distance ){
        World.modelBearing = bearing;
        World.modelDistance = distance;
        if(!World.modelInit){
            World.modelInit = true;
            alert("Target Bearing: "+bearing+"\nTarget Distance: "+ distance);
        }
        if(World.currentModelShown)
            World.currentModelShown.rotate.z = World.modelBearing;
        World.calcPointingPosition();
    },
    calcPointingPosition: function (){
        if( World.cHInit && World.modelInit ){
            if( Math.abs(World.cHBearing - World.modelBearing) <= 15 && Math.abs(World.modelDistance - World.cHDistance) <= 3 ){
                World.inPosition = true;
                document.getElementById("tracking-start-stop-button").disabled = false;
            } else {
                document.getElementById("tracking-start-stop-button").disabled = true;
            }
        }
    },
    makeControlObject: function (arg){

        var geoLoc = new AR.GeoLocation(arg.latitude, arg.longitude, 0.0);
        var modelEarth = new AR.Model("assets/earth.wt3", {
        			onLoaded: this.worldLoaded,
        			scale: {
        				x: 1,
        				y: 1,
        				z: 1
        			}
        		});

        var indicatorImage = new AR.ImageResource("assets/indi.png");

        var indicatorDrawable = new AR.ImageDrawable(indicatorImage, 0.1, {
            verticalAnchor: AR.CONST.VERTICAL_ANCHOR.TOP
        });

        var geoObject1 = new AR.GeoObject(geoLoc, {
          drawables: {
             cam: [modelEarth],
             indicator: [indicatorDrawable]
          }
        });
        World.controlObject = geoObject1;
    }
};

AR.context.onLocationChanged = World.locationChanged;
