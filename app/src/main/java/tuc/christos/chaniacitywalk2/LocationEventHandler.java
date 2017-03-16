package tuc.christos.chaniacitywalk2;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import java.util.ArrayList;

import tuc.christos.chaniacitywalk2.data.dataManager;

/**
 * Created by Christos on 16-Mar-17.
 *
 */

public class LocationEventHandler implements LocationCallback {
    private dataManager mDataManager;
    private Location lastKnownLocation;
    private ArrayList<LocationEventsListener> iLocationEventListener = new ArrayList<>();
    private Context mContext;
    private long MIN_RADIUS = 10;
    private long COVER_RADIUS = 10;


    public LocationEventHandler(Context context){
        this.mContext = context;
        mDataManager = dataManager.getInstance();
    }

    public void registerLocationEventListener(LocationEventsListener listener){
        this.iLocationEventListener.add(listener);
    }

    public void handleNewLocation(Location location){
        this.lastKnownLocation = location;
        for(LocationEventsListener temp: iLocationEventListener)
            temp.userAtLocation(location);
        //Toast.makeText(mContext,"LOCATION READY FOR EVENT", Toast.LENGTH_LONG).show();
    }

}
