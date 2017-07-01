package tuc.christos.chaniacitywalk2.WikiduteUtils;

/**
 * Created by Christos on 26-Jun-17.
 /**
 * Interface for a location-provider implementation
 * feel free to implement your very own Location-Service, that handles GPS/Network positions more sophisticated but still takes care of
 * life-cycle events
 */
public interface ILocationProvider {

    /**
     * Call when host-activity is resumed (usually within systems life-cycle method)
     */
    public void onResume();

    /**
     * Call when host-activity is paused (usually within systems life-cycle method)
     */
    public void onPause();

}