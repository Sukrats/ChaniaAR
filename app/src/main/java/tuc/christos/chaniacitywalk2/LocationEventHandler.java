package tuc.christos.chaniacitywalk2;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import tuc.christos.chaniacitywalk2.data.dataManager;
import tuc.christos.chaniacitywalk2.model.Scene;

/**
 * Created by Christos on 16-Mar-17.
 *
 */

public class LocationEventHandler implements LocationCallback {

    private dataManager mDataManager;
    private Location lastKnownLocation = new Location("");
    private ArrayList<LocationEventsListener> iLocationEventListener = new ArrayList<>();
    private Context mContext;
    private long MIN_RADIUS = 15;
    private long COVER_RADIUS = 100;

    private ArrayList<GeoFence> GeoFences = new ArrayList<>();
    private Location activeFenceLocation = new Location("");
    private String activeFenceID;
    private boolean fenceTriggered = false;


    public LocationEventHandler(Context context){
        this.mContext = context;
        mDataManager = dataManager.getInstance();

    }

    public void setLocationEventListener(LocationEventsListener listener){
        this.iLocationEventListener.add(listener);
        Log.i("Event Handler","Listeners Registered: " + this.iLocationEventListener );

    }

    public void removeLocationEventListener(LocationEventsListener listener){
        iLocationEventListener.remove(listener);
        Log.i("Event Handler","Listeners Removed: " + listener );
    }

    public void handleNewLocation(Location location){
        /*
         *      Set GeoFences based on User Location
         *COVER_RADIUS is the distance in which we enable the fences
         */
        if( location.distanceTo(lastKnownLocation) >= ( COVER_RADIUS - MIN_RADIUS ) ) {
            this.lastKnownLocation = location;
            setGeoFences(location);
        }
        /*
         *      Check GeoFence Status
         *  if a fence is triggered we fire the event on the Listeners
         */
        if(!fenceTriggered){
            for(GeoFence temp: GeoFences) {
                if (location.distanceTo(temp.getLocation()) <= MIN_RADIUS ){
                    activeFenceLocation = temp.getLocation();
                    activeFenceID = temp.getID();
                    fenceTriggered = true;
                    triggerUserEnteredArea(activeFenceID);
                }
            }
        }else
        if (location.distanceTo(activeFenceLocation) >= MIN_RADIUS ){
            fenceTriggered = false;
            triggerUserLeftArea(activeFenceID);
        }
    }

    private void setGeoFences(Location location){
        GeoFences.clear();
        for(Scene scene: mDataManager.getScenes()){
            String id = Integer.toString(scene.getId());
            Location loc = new Location("");
            loc.setLatitude(scene.getLatitude());
            loc.setLongitude(scene.getLongitude());

            if(location.distanceTo(loc) <= COVER_RADIUS) {
                GeoFences.add(new GeoFence(loc, id));
            }
        }
        if(GeoFences.isEmpty()){
            Toast.makeText(mContext,"NO GEO FENCE ACTIVE ", Toast.LENGTH_LONG).show();

        }else{
            Toast.makeText(mContext,"GEO FENCES ACTIVE: " + GeoFences.size(), Toast.LENGTH_LONG).show();

        }

    }

    private void triggerUserEnteredArea(String id){
        Log.i("EventHandler","GeoFenceTriggered: " + mDataManager.getScene(id).getName());
        for(LocationEventsListener temp: iLocationEventListener)
            temp.userEnteredArea(id);
    }

    private void triggerUserLeftArea(String id){
        Log.i("EventHandler","GeoFenceClosed: " + mDataManager.getScene(id).getName());
        for(LocationEventsListener temp: iLocationEventListener)
            temp.userLeftArea(id);
    }

    private class GeoFence{
        private Location location;
        private String ID;

        public GeoFence (Location location, String id){
            this.location = location;
            this.ID = id;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }
    }
}
