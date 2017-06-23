var World = {

    circle: null,
    viewport: null,
    scene: null,
    sceneList:[],
    modelList:[],
    location:null,
    currentModelShown: null,
    areas:[],

    posX:0,
    posY:0,
    init: function initFn() {
        this.createOverlays();
    },

    createOverlays: function createOverlaysFn() {
        var crossHairsRedImage = new AR.ImageResource("assets/crosshairs_red.png");
        var crossHairsRedDrawable = new AR.ImageDrawable(crossHairsRedImage, 1.0, {
            rotate : { x: 90}
        });

        var crossHairsBlueImage = new AR.ImageResource("assets/crosshairs_blue.png");
        var crossHairsBlueDrawable = new AR.ImageDrawable(crossHairsBlueImage, 1.0,{
            rotate : { x: 90}
        });


        this.tracker = new AR.InstantTracker({
            onChangedState:  function onChangedStateFn(state) {
            },
            // device height needs to be as accurate as possible to have an accurate scale
            // returned by the Wikitude SDK
            deviceHeight: 1.7,
            onError: function(errorMessage) {
                alert(errorMessage);
            }
        });

        this.instantTrackable = new AR.InstantTrackable(this.tracker, {
            drawables: {
                cam: crossHairsBlueDrawable,
                initialization: crossHairsRedDrawable
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
        
        if (this.tracker.state === AR.InstantTrackerState.INITIALIZING) {
            document.getElementById("tracking-start-stop-button").src = "assets/buttons/stop.png";
            this.tracker.state = AR.InstantTrackerState.TRACKING;
        } else {
            document.getElementById("tracking-start-stop-button").src = "assets/buttons/start.png";
            this.tracker.state = AR.InstantTrackerState.INITIALIZING;
        }
    },

    locationChanged: function locationChangedFn(lat, lon, alt, acc) {
        var location1 = {
            "lat": lat,
            "lon":lon
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
                   z: World.viewport.rotation
               },
               translate: {
                   x: World.viewport.posX,
                   y: World.viewport.posY,
                   z: 0
               }
            });
            World.modelList.push(model);
        }
    },
    initSlider: function initSlider(){
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
    },
    isTracking: function isTrackingFn() {
        return (this.tracker.state === AR.InstantTrackerState.TRACKING);
    },
    setModel: function setModelFn(index){
        if (World.isTracking()) {
            if(World.currentModelShown != null){
                this.instantTrackable.drawables.removeCamDrawable(World.currentModelShown);
            }
            World.currentModelShown = World.modelList[index-1];
            this.instantTrackable.drawables.addCamDrawable(World.currentModelShown);
        }else{
            World.currentModelShown = World.modelList[index-1];
        }
    }
};


AR.context.onLocationChanged = World.locationChanged;
