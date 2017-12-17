package tuc.christos.chaniacitywalk2.locationService;

import android.location.Location;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import tuc.christos.chaniacitywalk2.mInterfaces.LocationCallback;
import tuc.christos.chaniacitywalk2.mInterfaces.LocationEventsListener;
import tuc.christos.chaniacitywalk2.model.Scene;

class LocationEventHandler implements LocationCallback {

    private Location lastKnownLocation = new Location("");
    private Location lastUpdatedLocation = new Location("");
    private ArrayList<LocationEventsListener> iLocationEventListener = new ArrayList<>();

    static int MIN_RADIUS = 25;
    private static long COVER_RADIUS = 80;

    private HashMap<Long, Scene> scenes = new HashMap<>();
    private ArrayList<GeoFence> GeoFences = new ArrayList<>();

    private Location activeFenceLocation = new Location("");
    private long activeFenceID;
    private boolean fenceTriggered = false;

    /**
     * public EventHandler Constructor
     * Creator activity context for Toasts
     * Can be removed
     */
    LocationEventHandler(ArrayList<Scene> scenes) {
        for (Scene temp : scenes)
            this.scenes.put(temp.getId(), temp);
        setGeoFencesWithDistance(lastKnownLocation);
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
        this.lastKnownLocation = location;
        /*
         *      Set GeoFences based on User Location
         *COVER_RADIUS is the distance in which we enable the fences
         */
        if (!fenceTriggered && !fencesDistance.isEmpty() && location.distanceTo(lastUpdatedLocation) >= fencesDistance.get(0).getDistance() - COVER_RADIUS -4 ) {
            //TODO TESTING
            //setGeoFences(location);
            setGeoFencesWithDistance(location);
        }

        //checkFenceTriggers(location);
        checkFenceTriggersWithDistance(location);
    }

    /*
     *      Check GeoFence Status
     *  if a fence is triggered we fire the event on the Listeners
     *  else we check when the user leaves the area to fire the event
     */
    public void checkFenceTriggers(Location location) {
        if (!fenceTriggered) {
            for (GeoFence temp : GeoFences) {
                if (location.distanceTo(temp.getLocation()) <= MIN_RADIUS) {
                    activeFenceLocation = temp.getLocation();
                    activeFenceID = temp.getID();
                    fenceTriggered = true;
                    triggerUserEnteredArea(activeFenceID);
                    return;
                }
            }
        } else if (location.distanceTo(activeFenceLocation) >= MIN_RADIUS + 5) {
            fenceTriggered = false;
            triggerUserLeftArea(activeFenceID);
        }
    }


    public void updateSceneList(ArrayList<Scene> list) {
        HashMap<Long, Scene> scenes = new HashMap<>();
        for (Scene s : list) {
            scenes.put(s.getId(), s);
        }
        this.scenes = scenes;

        //TODO TESTING
        //setGeoFences(location);
        setGeoFencesWithDistance(lastKnownLocation);
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
        if (!scenes.values().isEmpty())
            for (Scene scene : scenes.values()) {
                long id = scene.getId();
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
            long[] areaIds = new long[size];
            int i = 0;
            for (GeoFence fence : GeoFences) {
                areaIds[i] = fence.getID();
                i++;
            }
            triggerDrawGeoFences(areaIds);
        }

    }

    private ArrayList<GeoFence> fencesDistance = new ArrayList<>();

    private void setGeoFencesWithDistance(Location location) {
        this.lastUpdatedLocation = location;
        fencesDistance.clear();
        if (!scenes.values().isEmpty()) {
            for (Scene scene : scenes.values()) {
                long id = scene.getId();
                Location loc = new Location("");
                loc.setLatitude(scene.getLatitude());
                loc.setLongitude(scene.getLongitude());
                fencesDistance.add(new GeoFence(loc, id, location.distanceTo(loc)));
            }
            fencesDistance = sortDistances(fencesDistance);

        }
        if (!fencesDistance.isEmpty()) {
            /*
             * Else we prepare the data for the map display
             * and trigger the event
             */
            final int size = fencesDistance.size();
            long[] areaIds = new long[size];
            int i = 0;
            for (GeoFence fence : fencesDistance) {
                areaIds[i] = fence.getID();
                i++;
            }
            triggerDrawGeoFences(areaIds);
        }
    }

    public void checkFenceTriggersWithDistance(Location location) {
        if (!fenceTriggered && !fencesDistance.isEmpty()) {
            GeoFence temp = fencesDistance.get(0);
            if (location.distanceTo(temp.getLocation()) <=  MIN_RADIUS - 3) {
                activeFenceLocation = temp.getLocation();
                activeFenceID = temp.getID();
                fenceTriggered = true;
                triggerUserEnteredArea(activeFenceID);
            }
        } else if (fenceTriggered && location.distanceTo(activeFenceLocation) >= MIN_RADIUS + 3) {
            fenceTriggered = false;
            triggerUserLeftArea(activeFenceID);
            setGeoFencesWithDistance(location);
        }
    }

    private ArrayList<GeoFence> sortDistances(ArrayList<GeoFence> list) {
        Collections.sort(list, new Comparator<GeoFence>() {
            @Override
            public int compare(GeoFence lhs, GeoFence rhs) {
                float cur = lhs.getDistance();
                if (cur == 0)
                    return -1;

                float next = rhs.getDistance();
                if (next == 0)
                    return 1;
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return cur > next ? 1 : (cur < next) ? -1 : 0;
            }
        });
        return list;
    }

    /**
     * Fire User Entered GeoFence Event for every Listener
     *
     * @param id id of triggered GeoFence
     */

    private void triggerUserEnteredArea(long id) {
        Log.i("EventHandler", "GeoFenceTriggered: " + this.scenes.get(id).getName());
        for (LocationEventsListener temp : iLocationEventListener)
            temp.userEnteredArea(id);
    }

    /**
     * Fire User Left GeoFence Event for every Listener
     *
     * @param id id for GeoFence left
     */
    private void triggerUserLeftArea(long id) {
        Log.i("EventHandler", "GeoFenceClosed: " + this.scenes.get(id).getName());
        for (LocationEventsListener temp : iLocationEventListener)
            temp.userLeftArea(id);
    }

    /**
     * Fire Draw Geo Fences for every Listener
     *
     * @param ids id for GeoFence left
     */
    private void triggerDrawGeoFences(long[] ids) {
        for (LocationEventsListener temp : iLocationEventListener)
            temp.drawGeoFences(ids, MIN_RADIUS);
    }

    public void requestFences() {
        //TODO change fencesDistance to GeoFences
        if (!fencesDistance.isEmpty()) {
            final int size = fencesDistance.size();
            long[] areaIds = new long[size];
            int i = 0;
            for (GeoFence fence : fencesDistance) {
                areaIds[i] = fence.getID();
                i++;
            }
            triggerDrawGeoFences(areaIds);
        }
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
    long getTriggeredArea() {
        return activeFenceID;
    }

    long[] getActiveFences() {
        long[] fences = new long[GeoFences.size()];
        for (int i = 0; i < GeoFences.size(); i++)
            fences[i] = GeoFences.get(i).getID();
        return fences;
    }

    /**
     * Private GeoFence Implementation
     */
    private class GeoFence {
        private Location location;
        private long ID;
        private float distance;

        GeoFence(Location location, long id) {
            this.location = location;
            this.ID = id;
        }

        GeoFence(Location location, long id, float distance) {
            this.location = location;
            this.ID = id;
            this.distance = distance;
        }

        public float getDistance() {
            return this.distance;
        }

        public void setDistance(float dist) {
            this.distance = dist;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        long getID() {
            return ID;
        }

    }
}