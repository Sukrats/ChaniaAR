var World = {
	loaded: false,
	rotating: false,
	objList:[],
	currentModelShown: null,
	controlsShown: false,
    scene:null,
    sceneList:[],

	/*init: function initFn() {
		this.createModelAtLocation();
	},*/

	createModelAtLocation: function createModelAtLocationFn() {
	    World.objList = [];
		/*
			First a location where the model should be displayed will be defined. This location will be relativ to the user.
		*/
		for(var i = 0; i < World.sceneList.length; i++){
            /*
                Next the model object is loaded.
            */
            var model = new AR.Model(World.sceneList[i].path, {
                scale: {
                    x: 1.0,
                    y: 1.0,
                    z: 1.0
                },
                onLoaded: this.worldLoaded,
                verticalAnchor: AR.CONST.VERTICAL_ANCHOR.BOTTOM
            });
            model.translate.y=-2;

            var location = new AR.GeoLocation(World.sceneList[i].latitude, World.sceneList[i].longitude , -2);
            var indicatorImage = new AR.ImageResource("assets/indi.png");
            var indicatorDrawable = new AR.ImageDrawable(indicatorImage, 0.1, {
                verticalAnchor: AR.CONST.VERTICAL_ANCHOR.TOP
            });

            /*
                Putting it all together the location and 3D model is added to an AR.GeoObject.
            */
            //for(var i = 0; i < locations.length; i++){
		    var snippet = World.scene.title.trunc(25)+i;
            var titleLabel = new AR.Label(snippet, 1, {
                zOrder: 1,
                offsetY: 0.55,
                style: {
                    textColor: '#FFFFFF',
                    fontStyle: AR.CONST.FONT_STYLE.BOLD
                }
            });
            World.objList.push(new AR.GeoObject(location, {
                drawables: {
                   cam: [model,titleLabel],
                   indicator: [indicatorDrawable]
                },
                enabled:false
            }));
        }

	},

    getScene: function getSceneFn(args) {
        World.sceneList = [];
        for(var i =0; i < args.ar_scenes.length; i++){
            var ar = {
              "path": args.ar_scenes[i].path,
              "latitude": args.ar_scenes[i].latitude,
              "longitude": args.ar_scenes[i].longitude
            };
            World.sceneList.push(ar);
        }
        var singlePoi = {
        	"id": args.id,
        	"latitude": parseFloat(args.latitude),
        	"longitude": parseFloat(args.longitude),
        	"title": args.name,
        	"description": args.description,
        	"num": args.num
        };
        World.scene = singlePoi;
		World.createModelAtLocation();
    },

	worldLoaded: function worldLoadedFn() {
		World.loaded = true;
		var e = document.getElementById('loadingMessage');
		e.parentElement.removeChild(e);
//        alert("NUM: "+World.sceneList.length);

        $("#slider").prop({
            min: 1,
            max: World.sceneList.length
        }).slider("refresh");
		//$("#slider").slider( "option", "max", World.sceneList.length  );
		//$("#slider").slider( "option", "min", World.sceneList.length  );
		$("#slider").on( "slidestop", function( event, ui ) {
		    var val = $("#slider").val();
		    World.showModel(val);
		} );

		var val = $("#slider").val();
		World.showModel(val);

		$("#overflow").click(function(){
		    World.controlsShown = World.controlsShown ? false : true ;
		    var iconToUse = World.controlsShown ? "arrow-d" : "arrow-u" ;
		    $("#controls").slideToggle();
            var height = $("#cont").height();
		    if(World.controlsShown){
                $( "#overflow" ).animate({ bottom : "+="+height });
            }else{
                $( "#overflow" ).animate({ bottom : 0 });
            }
            $("#overflow").buttonMarkup({
                icon: iconToUse
            });
		});
	},
	userEnteredArea: function userEnteredAreaFn(){},
	userLeftArea: function userLeftAreaFn(){},

	ShowBackBtn: function ShowBackBtnFn(){
        $("#backBtn").show();
        $("#backBtn").click(function(){
            //document.location = "architectsdk://arNav";
            var args = {
                action: "arNav"
            };
            AR.platform.sendJSONObject(args);
        });
	},

	showModel: function showModel(index){
        if(World.currentModelShown != null){
	        World.currentModelShown.enabled = false;
	    }
        World.currentModelShown = World.objList[index-1];
        World.currentModelShown.enabled = true;
	},

	locationChanged: function locationChangedFn(lat, lon, alt, acc) {
    /*
    	The custom function World.onLocationChanged checks with the flag World.initiallyLoadedData if the function was already called.
    	With the first call of World.onLocationChanged an object that contains geo information will be created which will be later used
    	to create a marker using the World.loadPoisFromJsonData function.
    */
    }
};
/*
World.init();*/

AR.context.onLocationChanged = World.locationChanged;

// will truncate all strings longer than given max-length "n". e.g. "foobar".trunc(3) -> "foo..."
String.prototype.trunc = function(n) {
    return this.substr(0, n - 1) + (this.length > n ? '...' : '');
};