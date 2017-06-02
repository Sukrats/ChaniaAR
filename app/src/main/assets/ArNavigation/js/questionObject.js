var kMarker_AnimationDuration_ChangeDrawable = 500;
var kMarker_AnimationDuration_Resize = 1000;

function QuestionObject(poiData){

    this.poiData = poiData;
    this.isSelected = false;

    /*
        With AR.PropertyAnimations you are able to animate almost any property of ARchitect objects. This sample will animate the opacity of both background drawables so that one will fade out while the other one fades in. The scaling is animated too. The marker size changes over time so the labels need to be animated too in order to keep them relative to the background drawable. AR.AnimationGroups are used to synchronize all animations in parallel or sequentially.
    */

    this.animationGroup_idle = null;
    this.animationGroup_selected = null;
    /*
		The button is created similar to the overlay feature.
		An AR.ImageResource defines the look of the button and is reused for both buttons.
	*/
	var imgButton_otto = new AR.ImageResource("assets/buttons/Otto_idle.png");
	var imgButton_otto_sel = new AR.ImageResource("assets/buttons/Otto_selected.png");
	var imgButton_venn = new AR.ImageResource("assets/buttons/Ven_idle.png");
	var imgButton_venn_sel = new AR.ImageResource("assets/buttons/Ven_selected.png");
	var imgButton_mdrn = new AR.ImageResource("assets/buttons/Byz_idle.png");
	var imgButton_mdrn_sel = new AR.ImageResource("assets/buttons/Byz_selected.png");

    var ottoman = new Button(imgButton_otto, imgButton_otto_sel, poiData.period_id, 4, 1.25, {
        offsetX: 0,
        offsetY: -0.6,
        zOrder: 2
    });

	var venetian = new Button(imgButton_venn, imgButton_venn_sel, poiData.period_id, 3, 1.25, {
        offsetX: -1.65,
        offsetY: -0.7,
        zOrder: 2
    });

	var modern = new Button(imgButton_mdrn, imgButton_mdrn_sel, poiData.period_id, 5, 1.25, {
        offsetX: 1.65,
        offsetY: -0.7,
        zOrder: 2
    });

	this.ottomanBtn = ottoman.idleDrawable;
	this.ottomanBtn_selected = ottoman.selectedDrawable;
	this.venetianBtn = venetian.idleDrawable;
	this.venetianBtn_selected = venetian.selectedDrawable;
	this.modernBtn = modern.idleDrawable;
    this.modernBtn_selected = modern.selectedDrawable;

    // create the AR.GeoLocation from the poi data
    var markerLocation = new AR.GeoLocation(poiData.latitude, poiData.longitude, poiData.altitude);

    // create an AR.ImageDrawable for the marker in idle state
    this.markerDrawable_idle = new AR.ImageDrawable(World.markerDrawable_idle, 2.5, {
        zOrder: 0,
        opacity: 1.0,
    });

    // create an AR.ImageDrawable for the marker in selected state
    this.markerDrawable_selected = new AR.ImageDrawable(World.markerDrawable_selected, 2.5, {
        zOrder: 0,
        opacity: 0.0,
        onClick: null
    });

    // create an AR.Label for the marker's title
    this.titleLabel = new AR.Label(poiData.title.trunc(20), 0.6, {
        zOrder: 1,
        offsetY: 0.8,
        style: {
            textColor: '#FFFFFF',
            fontStyle: AR.CONST.FONT_STYLE.BOLD
        }
    });

    // create an AR.Label for the marker's description
    this.descriptionLabel = new AR.Label("Choose a Period:", 0.5, {
        zOrder: 1,
        offsetY: 0.2,
        style: {
            textColor: '#FFFFFF'
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

    this.GeoObject = new AR.GeoObject(markerLocation, {
        drawables: {
            cam: [this.markerDrawable_idle, this.markerDrawable_selected, this.titleLabel, this.descriptionLabel, this.ottomanBtn,
                  this.ottomanBtn_selected, this.venetianBtn, this.venetianBtn_selected, this.modernBtn, this.modernBtn_selected],
            radar: this.radardrawables
        }
    });



    return this;
}

Button = function(idle, selected, q_id, m_id, size, options ) {

    this.animationGroup_idle = null;
    this.animationGroup_selected = null;
    this.isSelected = false;
	/*
		As the button should be clickable the onClick trigger is defined in the options passed to the AR.ImageDrawable.
		In general each drawable can be made clickable by defining its onClick trigger.
     */
    this.id = m_id;
	options.onClick = function() {
	    if(q_id == m_id ){
            QuestionObject.prototype.getOnClickTrigger(this)
	    }
	};
	this.idleDrawable =  new AR.ImageDrawable(idle, size, options);
	options.opacity = 0;
	this.selectedDrawable =  new AR.ImageDrawable(selected, size, options);

	return this;
}



QuestionObject.prototype.getOnClickTrigger = function(question){
    if (!QuestionObject.prototype.isAnyAnimationRunning(question)) {
        if (question.isSelected) {

            QuestionObject.prototype.setDeselected(question);
            alert("setDeselected");
            /*try {
                World.onQuestionDeSelected(question);
            } catch (err) {
                alert(err);
            }*/
        } else {
            QuestionObject.prototype.setSelected(question);
            alert("setSelected");
            /*try {
                World.onQuestionSelected(question);
            } catch (err) {
                alert(err);
            }*/

        }
    } else {
        AR.logger.debug('a animation is already running');
    }
    return true;
};

QuestionObject.prototype.setSelected = function(question) {

    question.isSelected = true;

    // New:
    if (question.animationGroup_selected === null) {

        // create AR.PropertyAnimation that animates the opacity to 0.0 in order to hide the idle-state-drawable
        var hideIdleDrawableAnimation = new AR.PropertyAnimation(question.idleDrawable, "opacity", null, 0.0, kMarker_AnimationDuration_ChangeDrawable);
        // create AR.PropertyAnimation that animates the opacity to 1.0 in order to show the selected-state-drawable
        var showSelectedDrawableAnimation = new AR.PropertyAnimation(question.selectedDrawable, "opacity", null, 1.0, kMarker_AnimationDuration_ChangeDrawable);

        // create AR.PropertyAnimation that animates the scaling of the idle-state-drawable to 1.2
        var idleDrawableResizeAnimation = new AR.PropertyAnimation(question.idleDrawable, 'scaling', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the selected-state-drawable to 1.2
        var selectedDrawableResizeAnimation = new AR.PropertyAnimation(question.selectedDrawable, 'scaling', null, 1.2, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        /*
            There are two types of AR.AnimationGroups. Parallel animations are running at the same time, sequentials are played one after another. This example uses a parallel AR.AnimationGroup.
        */
        question.animationGroup_selected = new AR.AnimationGroup(AR.CONST.ANIMATION_GROUP_TYPE.PARALLEL, [hideIdleDrawableAnimation, showSelectedDrawableAnimation, idleDrawableResizeAnimation, selectedDrawableResizeAnimation]);
    }

    // removes function that is set on the onClick trigger of the idle-state marker
    question.idleDrawable.onClick = null;
    // sets the click trigger function for the selected state marker
    question.selectedDrawable.onClick = Marker.prototype.getOnClickTrigger(marker);
    // starts the selected-state animation
    question.animationGroup_selected.start();
};


QuestionObject.prototype.setDeselected = function(question) {

    question.isSelected = false;

    if (question.animationGroup_idle === null) {

        // create AR.PropertyAnimation that animates the opacity to 1.0 in order to show the idle-state-drawable
        var showIdleDrawableAnimation = new AR.PropertyAnimation(question.idleDrawable, "opacity", null, 1.0, kMarker_AnimationDuration_ChangeDrawable);
        // create AR.PropertyAnimation that animates the opacity to 0.0 in order to hide the selected-state-drawable
        var hideSelectedDrawableAnimation = new AR.PropertyAnimation(question.selectedDrawable, "opacity", null, 0, kMarker_AnimationDuration_ChangeDrawable);
        // create AR.PropertyAnimation that animates the scaling of the idle-state-drawable to 1.0
        var idleDrawableResizeAnimation = new AR.PropertyAnimation(question.idleDrawable, 'scaling', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        // create AR.PropertyAnimation that animates the scaling of the selected-state-drawable to 1.0
        var selectedDrawableResizeAnimation = new AR.PropertyAnimation(question.selectedDrawable, 'scaling', null, 1.0, kMarker_AnimationDuration_Resize, new AR.EasingCurve(AR.CONST.EASING_CURVE_TYPE.EASE_OUT_ELASTIC, {
            amplitude: 2.0
        }));
        /*
            There are two types of AR.AnimationGroups. Parallel animations are running at the same time, sequentials are played one after another. This example uses a parallel AR.AnimationGroup.
        */
        question.animationGroup_idle = new AR.AnimationGroup(AR.CONST.ANIMATION_GROUP_TYPE.PARALLEL, [showIdleDrawableAnimation, hideSelectedDrawableAnimation, idleDrawableResizeAnimation, selectedDrawableResizeAnimation]);
    }

    // sets the click trigger function for the idle state question
    question.idleDrawable.onClick = QuestionObject.prototype.getOnClickTrigger(question);
    // removes function that is set on the onClick trigger of the selected-state marker
    question.selectedDrawable.onClick = null;

    // starts the idle-state animation
    question.animationGroup_idle.start();
};

QuestionObject.prototype.isAnyAnimationRunning = function(question) {

    if (question.animationGroup_idle === null || question.animationGroup_selected === null) {
        return false;
    } else {
        if ((question.animationGroup_idle.isRunning() === true) || (question.animationGroup_selected.isRunning() === true)) {
            return true;
        } else {
            return false;
        }
    }
};