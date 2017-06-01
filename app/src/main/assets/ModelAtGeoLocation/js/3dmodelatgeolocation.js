var World = {
	loaded: false,
	rotating: false,
	objList:[],
	currentModelShown: null,
	controlsShown: false,
    scene:null,

	/*init: function initFn() {
		this.createModelAtLocation();
	},*/

	createModelAtLocation: function createModelAtLocationFn() {
	    World.objList = [];
	    var locations = [];
		/*
			First a location where the model should be displayed will be defined. This location will be relativ to the user.
		*/
		var location = new AR.RelativeLocation(null, 5, -5, -5);
		var location1 = new AR.RelativeLocation(null, 5, -5, 0);
		var location2 = new AR.RelativeLocation(null, 5, -5, 5);
		var location3 = new AR.RelativeLocation(null, 5, -5, 10);
		locations.push(location);
		locations.push(location1);
		locations.push(location2);
		locations.push(location3);

		/*
			Next the model object is loaded.
		*/
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
		/*
			Putting it all together the location and 3D model is added to an AR.GeoObject.
		*/
		for(var i = 0; i < locations.length; i++){
		    var snippet = World.scene.title.trunc(10)+i;
            var titleLabel = new AR.Label(snippet, 1, {
                zOrder: 1,
                offsetY: 0.55,
                style: {
                    textColor: '#FFFFFF',
                    fontStyle: AR.CONST.FONT_STYLE.BOLD
                }
            });
            World.objList.push(new AR.GeoObject(locations[i], {
                drawables: {
                   cam: [modelEarth,titleLabel],
                   indicator: [indicatorDrawable]
                },
                enabled:false
            }));
        }

	},

    getScene: function getSceneFn(args) {
        var singlePoi = {
        	"id": args.id,
        	"latitude": parseFloat(args.latitude),
        	"longitude": parseFloat(args.longitude),
        	"title": args.name,
        	"description": args.description
        };
        World.scene = singlePoi;
		this.createModelAtLocation();
    },

	worldLoaded: function worldLoadedFn() {
		World.loaded = true;
		var e = document.getElementById('loadingMessage');
		e.parentElement.removeChild(e);

		var val = $("#slider").val();
		World.showModel(val);
		$("#slider").on( "slidestop", function( event, ui ) {
		    var val = $("#slider").val();
		    World.showModel(val);
		} );

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

	ShowBackBtn: function ShowBackBtnFn(){
        $("#backBtn").show();
        $("#backBtn").click(function(){
            document.location = "architectsdk://arNav";
        });
	},

	showModel: function showModel(index){
        if(World.currentModelShown != null){
	        World.currentModelShown.enabled = false;
	    }
        World.currentModelShown = World.objList[index];
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