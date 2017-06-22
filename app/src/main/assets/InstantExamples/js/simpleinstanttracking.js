var World = {

    circle: null,
    posX:0,
    posY:0,
    init: function initFn() {
        this.createOverlays();
    },

    createOverlays: function createOverlaysFn() {
        var crossHairsRedImage = new AR.ImageResource("assets/crosshairs_red.png");
        var crossHairsRedDrawable = new AR.ImageDrawable(crossHairsRedImage, 1.0, {
            //rotate : { x: 90}
        });

        var crossHairsBlueImage = new AR.ImageResource("assets/crosshairs_blue.png");
        var crossHairsBlueDrawable = new AR.ImageDrawable(crossHairsBlueImage, 1.0,{
            //rotate : { x: 90}
        });

        var target = new AR.Circle(3,{
            opacity: 1,
            style:{
                fillColor : '#00000000',
                outlineColor : '#FFFFFF',
                outlineSize: 2
            }
         });
        //geoloc, northing, easting, altitude
        var location = new AR.RelativeLocation(null, -10, -8, 0);
        World.circle = new AR.GeoObject(location, {
          enabled : true,
          drawables:{
          cam: [target]
          }
        });


        this.tracker = new AR.InstantTracker({
            onChangedState:  function onChangedStateFn(state) {

            },
            // device height needs to be as accurate as possible to have an accurate scale
            // returned by the Wikitude SDK
            deviceHeight: 1.6,
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

                var model = new AR.Model("assets/rocco_2048_slam.wt3", {
                   scale: {
                       x: 1.0,
                       y: 1.0,
                       z: 1.0
                   }
                });
                //model.translate.global.x = World.posX;
                //model.translate.global.y = World.posY;
                World.model = model;
                this.drawables.addCamDrawable(model);
                // do something when tracking is started (recognized)
            },
            onTrackingStopped: function onTrackingStoppedFn() {
            this.drawables.removeCamDrawable(World.model);
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
        /*
        	The custom function World.onLocationChanged checks with the flag World.initiallyLoadedData if the function was already called.
        	With the first call of World.onLocationChanged an object that contains geo information will be created which will be later used
        	to create a marker using the World.loadPoisFromJsonData function.
        */
    },
    getInstantiation: function getInstantiationFn(args){
        World.posX = args.posx;
        World.posY = args.posy;

        World.init();
    }
};


AR.context.onLocationChanged = World.locationChanged;
