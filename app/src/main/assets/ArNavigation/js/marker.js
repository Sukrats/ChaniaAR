var kMarker_AnimationDuration_ChangeDrawable = 500;
var kMarker_AnimationDuration_Resize = 1000;

function Marker(poiData) {

    this.poiData = poiData;
    this.isSelected = false;

    this.animationGroup_idle = null;
    this.animationGroup_selected = null;

    var thumb = new AR.ImageResource(poiData.thumbnail);


    this.thumbnail = new AR.ImageDrawable( thumb, 1.2, {
         zOrder: 1,
         offsetY: -0.5,
         offsetX: -2.3,
         horizontalAnchor: AR.CONST.HORIZONTAL_ANCHOR.LEFT,
         verticalAnchor: AR.CONST.HORIZONTAL_ANCHOR.BOTTOM
     });

    //create an AR.Label for the marker's distance
    this.distanceLabel = new AR.Label("", 0.5, {
        zOrder: 1,
        offsetY: -0.5,
        offsetX: 1.2,
        opacity: 1.0,
        horizontalAnchor: AR.CONST.HORIZONTAL_ANCHOR.CENTER,
        verticalAnchor: AR.CONST.HORIZONTAL_ANCHOR.BOTTOM,
        style: {
            textColor: '#FFFFFF',
            fontStyle: AR.CONST.FONT_STYLE.BOLD
        }
    });
    // create the AR.GeoLocation from the poi data
    var markerLocation = new AR.GeoLocation(poiData.latitude, poiData.longitude, poiData.altitude);

    // create an AR.ImageDrawable for the marker in idle state
    this.markerDrawable_idle = new AR.ImageDrawable(World.markerDrawable_idle, 2.5, {
        zOrder: 0,
        opacity: 1.0,
        /*
            To react on user interaction, an onClick property can be set for each AR.Drawable. The property is a function which will be called each time the user taps on the drawable. The function called on each tap is returned from the following helper function defined in marker.js. The function returns a function which checks the selected state with the help of the variable isSelected and executes the appropriate function. The clicked marker is passed as an argument.
        */
        onClick: Marker.prototype.getOnClickTrigger(this)
    });

    // create an AR.ImageDrawable for the marker in selected state
    this.markerDrawable_selected = new AR.ImageDrawable(World.markerDrawable_selected, 2.5, {
        zOrder: 0,
        opacity: 0.0,
        onClick: null
    });

    // create an AR.Label for the marker's title
    var displayTitle = poiData.title;
    this.titleLabel = new AR.Label(displayTitle.trunc(15), 0.7, {
        zOrder: 1,
        offsetY: 0.5,
        style: {
            textColor: '#FFFFFF',
            fontStyle: AR.CONST.FONT_STYLE.BOLD
        }
    });

    this.radarCircle = new AR.Circle(0.03, {
            horizontalAnchor: AR.CONST.HORIZONTAL_ANCHOR.CENTER,
            opacity: 0.8,
            style: {
                fillColor: "#ffffff"
            }
        });

        this.radarCircleSelected = new AR.Circle(0.05, {
            horizontalAnchor: AR.CONST.HORIZONTAL_ANCHOR.CENTER,
            opacity: 0.8,
            style: {
                fillColor: "#0066ff"
            }
        });

        this.radardrawables = [];
        this.radardrawables.push(this.radarCircle);

        this.radardrawablesSelected = [];
        this.radardrawablesSelected.push(this.radarCircleSelected);

    /*
        Create the AR.GeoObject with the drawable objects and define the AR.ImageDrawable as an indicator target on the marker AR.GeoObject. The direction indicator is displayed automatically when necessary. AR.Drawable subclasses (e.g. AR.Circle) can be used as direction indicators.
    */

        this.markerObject = new AR.GeoObject(markerLocation, {
            drawables: {
                cam: [this.markerDrawable_idle, this.markerDrawable_selected, this.titleLabel, this.thumbnail, this.distanceLabel],
                radar: this.radardrawables
            }
        });
        World.areaMarker = this.markerObject;

    return this;
}

Marker.prototype.getOnClickTrigger = function(marker) {

    /*
        The setSelected and setDeselected functions are prototype Marker functions.
        Both functions perform the same steps but inverted.
    */

    return function() {

        if (!Marker.prototype.isAnyAnimationRunning(marker)) {
            if (marker.isSelected) {

                Marker.prototype.setDeselected(marker);
                try {
                    World.onMarkerDeSelected(marker);
                } catch (err) {
                    alert(err);
                }
            } else {
                Marker.prototype.setSelected(marker);
                try {
                    World.onMarkerSelected(marker);
                } catch (err) {
                    alert(err);
                }

            }
        } else {
            AR.logger.debug('a animation is already running');
        }


        return true;
    };
};

/*
    Property Animations allow constant changes to a numeric value/property of an object, dependent on start-value, end-value and the duration of the animation. Animations can be seen as functions defining the progress of the change on the value. The Animation can be parametrized via easing curves.
*/

Marker.prototype.setSelected = function(marker) {

    marker.isSelected = true;

    // New:
    if (marker.animationGroup_selected === null) {

        // create AR.PropertyAnimation that animates the opacity to 0.0 in order to hide the idle-state-drawable
        var hideIdleDrawableAnimation = new AR.PropertyAnimation(marker.markerDrawable_idle, "opacity", null, 0.0, kMarker_AnimationDuration_ChangeDrawable);
        // create AR.PropertyAnimation that animates the opacity to 1.0 in order to show the selected-state-drawable
        var showSelectedDrawableAnimation = new AR.PropertyAnimation(marker.markerDrawable_selected, "opacity", null, 1.0, kMarker_AnimationDuration_ChangeDrawable);

        // create AR.PropertyAnimation that animates the scaling of the idle-state-drawable to 1.2
        var idleDrawableResizeAnimationX = new AR.PropertyAnimation(marker.markerDrawable_idle, 'scale.x', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the idle-state-drawable to 1.2
        var idleDrawableResizeAnimationY = new AR.PropertyAnimation(marker.markerDrawable_idle, 'scale.y', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));



        // create AR.PropertyAnimation that animates the scaling of the selected-state-drawable to 1.2
        var selectedDrawableResizeAnimationX = new AR.PropertyAnimation(marker.markerDrawable_selected, 'scale.x', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the selected-state-drawable to 1.2
        var selectedDrawableResizeAnimationY = new AR.PropertyAnimation(marker.markerDrawable_selected, 'scale.y', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));


        // create AR.PropertyAnimation that animates the scaling of the title label to 1.2
        var titleLabelResizeAnimationX = new AR.PropertyAnimation(marker.titleLabel, 'scale.x', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
         // create AR.PropertyAnimation that animates the scaling of the title label to 1.2
        var titleLabelResizeAnimationY = new AR.PropertyAnimation(marker.titleLabel, 'scale.y', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));

        // create AR.PropertyAnimation that animates the scaling of the description label to 1.2
        var descriptionLabelResizeAnimationX = new AR.PropertyAnimation(marker.thumbnail, 'scale.x', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the description label to 1.2
        var descriptionLabelResizeAnimationY = new AR.PropertyAnimation(marker.thumbnail, 'scale.y', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the description label to 1.2
        var distanceLabelResizeAnimationX = new AR.PropertyAnimation(marker.distanceLabel, 'scale.x', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the description label to 1.2
        var distanceLabelResizeAnimationY = new AR.PropertyAnimation(marker.distanceLabel, 'scale.y', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));

        /*
            There are two types of AR.AnimationGroups. Parallel animations are running at the same time, sequentials are played one after another. This example uses a parallel AR.AnimationGroup.
        */
        marker.animationGroup_selected = new AR.AnimationGroup(AR.CONST.ANIMATION_GROUP_TYPE.PARALLEL,
            [distanceLabelResizeAnimationX, distanceLabelResizeAnimationY, showSelectedDrawableAnimation,hideIdleDrawableAnimation, idleDrawableResizeAnimationY,idleDrawableResizeAnimationX,selectedDrawableResizeAnimationX,selectedDrawableResizeAnimationY, titleLabelResizeAnimationX,titleLabelResizeAnimationY,descriptionLabelResizeAnimationX,descriptionLabelResizeAnimationY]);
    }

    // removes function that is set on the onClick trigger of the idle-state marker
    marker.markerDrawable_idle.onClick = null;
    // sets the click trigger function for the selected state marker
    marker.markerDrawable_selected.onClick = Marker.prototype.getOnClickTrigger(marker);

    marker.markerObject.drawables.radar = marker.radardrawablesSelected;

    // starts the selected-state animation
    marker.animationGroup_selected.start();
};

Marker.prototype.setDeselected = function(marker) {

    marker.isSelected = false;

    marker.markerObject.drawables.radar = marker.radardrawables;

    if (marker.animationGroup_idle === null) {

        // create AR.PropertyAnimation that animates the opacity to 1.0 in order to show the idle-state-drawable
        var showIdleDrawableAnimation = new AR.PropertyAnimation(marker.markerDrawable_idle, "opacity", null, 1.0, kMarker_AnimationDuration_ChangeDrawable);
        // create AR.PropertyAnimation that animates the opacity to 0.0 in order to hide the selected-state-drawable
        var hideSelectedDrawableAnimation = new AR.PropertyAnimation(marker.markerDrawable_selected, "opacity", null, 0, kMarker_AnimationDuration_ChangeDrawable);

        // create AR.PropertyAnimation that animates the scaling of the idle-state-drawable to 1.2
        var idleDrawableResizeAnimationX = new AR.PropertyAnimation(marker.markerDrawable_idle, 'scale.x', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the idle-state-drawable to 1.2
        var idleDrawableResizeAnimationY = new AR.PropertyAnimation(marker.markerDrawable_idle, 'scale.y', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));



        // create AR.PropertyAnimation that animates the scaling of the selected-state-drawable to 1.2
        var selectedDrawableResizeAnimationX = new AR.PropertyAnimation(marker.markerDrawable_selected, 'scale.x', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the selected-state-drawable to 1.2
        var selectedDrawableResizeAnimationY = new AR.PropertyAnimation(marker.markerDrawable_selected, 'scale.y', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));


        // create AR.PropertyAnimation that animates the scaling of the title label to 1.2
        var titleLabelResizeAnimationX = new AR.PropertyAnimation(marker.titleLabel, 'scale.x', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
         // create AR.PropertyAnimation that animates the scaling of the title label to 1.2
        var titleLabelResizeAnimationY = new AR.PropertyAnimation(marker.titleLabel, 'scale.y', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));

        // create AR.PropertyAnimation that animates the scaling of the description label to 1.2
        var descriptionLabelResizeAnimationX = new AR.PropertyAnimation(marker.thumbnail, 'scale.x', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the description label to 1.2
        var descriptionLabelResizeAnimationY = new AR.PropertyAnimation(marker.thumbnail, 'scale.y', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));

        // create AR.PropertyAnimation that animates the scaling of the description label to 1.2
        var distanceLabelResizeAnimationX = new AR.PropertyAnimation(marker.distanceLabel, 'scale.x', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the description label to 1.2
        var distanceLabelResizeAnimationY = new AR.PropertyAnimation(marker.distanceLabel, 'scale.y', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        /*
            There are two types of AR.AnimationGroups. Parallel animations are running at the same time, sequentials are played one after another. This example uses a parallel AR.AnimationGroup.
        */
        marker.animationGroup_idle = new AR.AnimationGroup(AR.CONST.ANIMATION_GROUP_TYPE.PARALLEL,
        [distanceLabelResizeAnimationX, distanceLabelResizeAnimationY, showIdleDrawableAnimation, hideSelectedDrawableAnimation, idleDrawableResizeAnimationX, selectedDrawableResizeAnimationX, titleLabelResizeAnimationX, descriptionLabelResizeAnimationX, idleDrawableResizeAnimationY, selectedDrawableResizeAnimationY, titleLabelResizeAnimationY, descriptionLabelResizeAnimationY]);


    }

    // sets the click trigger function for the idle state marker
    marker.markerDrawable_idle.onClick = Marker.prototype.getOnClickTrigger(marker);
    // removes function that is set on the onClick trigger of the selected-state marker
    marker.markerDrawable_selected.onClick = null;

    // starts the idle-state animation
    marker.animationGroup_idle.start();
};

Marker.prototype.isAnyAnimationRunning = function(marker) {

    if (marker.animationGroup_idle === null || marker.animationGroup_selected === null) {
        return false;
    } else {
        if ((marker.animationGroup_idle.isRunning() === true) || (marker.animationGroup_selected.isRunning() === true)) {
            return true;
        } else {
            return false;
        }
    }
};

// will truncate all strings longer than given max-length "n". e.g. "foobar".trunc(3) -> "foo..."
String.prototype.trunc = function(n) {
    return this.substr(0, n - 1) + (this.length > n ? '...' : '');
};