package tuc.christos.chaniacitywalk2.locationService;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import tuc.christos.chaniacitywalk2.mInterfaces.LocationCallback;
import tuc.christos.chaniacitywalk2.mInterfaces.LocationEventsListener;
import tuc.christos.chaniacitywalk2.model.Scene;

class LocationEventHandler implements LocationCallback {

    private Location lastKnownLocation = new Location("");
    private ArrayList<LocationEventsListener> iLocationEventListener = new ArrayList<>();

    static int MIN_RADIUS = 20;
    private static long COVER_RADIUS = 200;

    private HashMap<String,Scene> scenes = new HashMap<>();
    private ArrayList<GeoFence> GeoFences = new ArrayList<>();

    private Location activeFenceLocation = new Location("");
    private String activeFenceID;
    private boolean fenceTriggered = false;

    /**
     * public EventHandler Constructor
     * Creator activity context for Toasts
     * Can be removed
     */
    LocationEventHandler(ArrayList<Scene> scenes) {
        for(Scene temp: scenes)
            this.scenes.put(String.valueOf(temp.getId()),temp);
    }


    /**
     * Register Listeners to the EventHandler
     *
     * @param listener Activity that implements LocationEventListener Interface to Listen to LocationEvents
     */

    void setLocationEventListener(LocationEventsListener listener) {

        this.iLocationEventListener.add(listener);
        Log.i("Event Handler", "Listeners Registered: " + this.iLocationEventListener);

    }

    /**
     * Remove Listener From EventHandler
     *
     * @param listener Listener to be removed from the EventHandler
     */
    void removeLocationEventListener(LocationEventsListener listener) {
        iLocationEventListener.remove(listener);
        Log.i("Event Handler", "Listeners Removed: " + listener);
    }


    /**
     * Called when Location Provider fires a new location
     *
     * @param location new location
     */
    public void handleNewLocation(Location location) {
        /*
         *      Set GeoFences based on User Location
         *COVER_RADIUS is the distance in which we enable the fences
         */
        this.lastKnownLocation = location;
       // if (location.distanceTo(lastKnownLocation) >= (COVER_RADIUS - COVER_RADIUS / 2)) {
            //setGeoFences(location);
        //}
        /*
         *      Check GeoFence Status
         *  if a fence is triggered we fire the event on the Listeners
         *  else we check when the user leaves the area to fire the event
         */
        if (!fenceTriggered) {
            for (GeoFence temp : GeoFences) {
                if (location.distanceTo(temp.getLocation()) <= MIN_RADIUS) {
                    activeFenceLocation = temp.getLocation();
                    activeFenceID = temp.getID();
                    fenceTriggered = true;
                    triggerUserEnteredArea(activeFenceID);
                }
            }
        } else if (location.distanceTo(activeFenceLocation) >= MIN_RADIUS) {
            fenceTriggered = false;
            triggerUserLeftArea(activeFenceID);
        }
    }

    public void updateSceneList(ArrayList<Scene> list){
        HashMap<String,Scene> scenes = new HashMap<>();
        for(Scene s: list){
            scenes.put(String.valueOf(s.getId()),s);
        }
        this.scenes = scenes;
        setGeoFences(lastKnownLocation);
    }
    /**
     * Fired when the user left the Cover Radius
     * Initiates new GeoFences based on the new Location
     *
     * @param location current user location
     */
    private void setGeoFences(Location location) {
        GeoFences.clear();
        /*
         *      A Fence is made for each scene in the database acquired from the data Manager
         */
        if(!scenes.values().isEmpty())
        for (Scene scene : scenes.values()) {
            String id = Long.toString(scene.getId());
            Location loc = new Location("");
            loc.setLatitude(scene.getLatitude());
            loc.setLongitude(scene.getLongitude());
            if (location.distanceTo(loc) <= COVER_RADIUS) {
                GeoFences.add(new GeoFence(loc, id));
            }
        }

    /*
         * If no fence is active we show the corresponding message
         */
        if (!GeoFences.isEmpty()) {
            /*
             * Else we prepare the data for the map display
             * and trigger the event
             */
            final int size = GeoFences.size();
            String[] areaIds = new String[size];
            int i = 0;
            for (GeoFence fence : GeoFences) {
                areaIds[i] = fence.getID();
                i++;
            }
            //triggerDrawGeoFences(areaIds);
            //Toast.makeText(mContext,"GEO FENCES ACTIVE: " + GeoFences.size(), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Fire User Entered GeoFence Event for every Listener
     *
     * @param id id of triggered GeoFence
     */

    private void triggerUserEnteredArea(String id) {
        Log.i("EventHandler", "GeoFenceTriggered: " + this.scenes.get(id).getName());
        for (LocationEventsListener temp : iLocationEventListener)
            temp.userEnteredArea(id);
    }

    /**
     * Fire User Left GeoFence Event for every Listener
     *
     * @param id id for GeoFence left
     */
    private void triggerUserLeftArea(String id) {
        Log.i("EventHandler", "GeoFenceClosed: " + this.scenes.get(id).getName());
        for (LocationEventsListener temp : iLocationEventListener)
            temp.userLeftArea(id);
    }

 /*   /**
     * Fired when the GeoFences are updated
     *
     * @param areaIds IDs of the scenes that are active
     */
/*    private void triggerDrawGeoFences(String[] areaIds) {
        Log.i("EventHandler", iLocationEventListener + " Draw GeoFences Called");
        for (LocationEventsListener temp : iLocationEventListener)
            temp.drawGeoFences(areaIds, MIN_RADIUS);
    }
*/
    String getTriggeredArea() {
        return activeFenceID;
    }

    String[] getActiveFences() {
        String[] fences = new String[GeoFences.size()];
        for (int i = 0; i < GeoFences.size(); i++)
            fences[i] = GeoFences.get(i).ID;
        return fences;
    }

    /**
     * Private GeoFence Implementation
     */
    private class GeoFence {
        private Location location;
        private String ID;

        GeoFence(Location location, String id) {
            this.location = location;
            this.ID = id;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        String getID() {
            return ID;
        }
    }
}