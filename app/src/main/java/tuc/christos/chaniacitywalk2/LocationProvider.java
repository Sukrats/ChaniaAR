package tuc.christos.chaniacitywalk2;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationProvider implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener  {

    private static final String TAG = "LocationProvider";

    private static final long DEFAULT_INTERVAL = 1000;
    private static final long DEFAULT_FASTEST_INTERVAL = 1000;
    private static final int DEFAULT_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;

    private static final long MEDIUM_INTERVAL = 1000;
    private static final long MEDIUM_FASTEST_INTERVAL = 1000;
    private static final int MEDIUM_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

    private static final long SLOW_INTERVAL = 1000;
    private static final long SLOW_FASTEST_INTERVAL = 1000;
    private static final int SLOW_PRIORITY = LocationRequest.PRIORITY_LOW_POWER;

    private ArrayList<LocationCallback> mLocationCallback = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest = new LocationRequest();


    private Location mLastKnownLocation;


    //private boolean mRequestingLocationUpdates = true;


    public LocationProvider (Context context){
        this.mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest(DEFAULT_INTERVAL,DEFAULT_FASTEST_INTERVAL,DEFAULT_PRIORITY);
    }

    public LocationProvider (Context context, String mode) {
        this.mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        if (mode.equals("High Accuracy")) {
            createLocationRequest(DEFAULT_INTERVAL, DEFAULT_FASTEST_INTERVAL, DEFAULT_PRIORITY);
            Toast.makeText(context,"High Accuracy",Toast.LENGTH_SHORT).show();
        }
        else if (mode.equals("Balanced Power Accuracy")){
            createLocationRequest(MEDIUM_INTERVAL, MEDIUM_FASTEST_INTERVAL, MEDIUM_PRIORITY);
            Toast.makeText(context,"Balanced Accuracy",Toast.LENGTH_SHORT).show();
        }
        else if(mode.equals("Battery Saver")) {
            createLocationRequest(SLOW_INTERVAL, SLOW_FASTEST_INTERVAL, SLOW_PRIORITY);
            Toast.makeText(context,"Battery Saver",Toast.LENGTH_SHORT).show();
        }
    }

    public void setLocationCallbackListener(LocationCallback callback){
        this.mLocationCallback.add(callback);
        Log.i("Location Provider","Listeners Registered: " + mLocationCallback );
    }

    public void removeLocationCallbackListener(LocationCallback callback){
        this.mLocationCallback.remove(callback);
        Log.i("Location Provider","Listeners Removed: " + callback );
    }

    /**
     * TODO
     * may return null loc
     * not needed??
     */
    public Location getLastKnownLocation(){
        Location loc = new Location("");
        try {
            loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch(SecurityException e){
            Log.i(TAG, e.getMessage());
        }

        if (loc == null) {
            return mLastKnownLocation;
        }else{
            return loc;
        }
    }
    /**
     * LOCATION REQUEST VARIABLES
     * **/
    private void createLocationRequest(long interval,long fastestInterval, int priority) {
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setPriority(priority);
    }

    /**             GOOGLE API CLIENT CALLBACKS
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        Log.i(TAG, "CONNECTION SUCCESSFUL");
        //if(mRequestingLocationUpdates)
            startLocationUpdates();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }



    /**
     * LOCATION UPDATES LIFECYCLE
     */

    private void startLocationUpdates(){
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

        }catch(SecurityException e){
            Log.i(TAG, "PERMISSION EXCEPTION :" + e.getMessage());
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Location From Google Play Services API
     * @param location
     * feeds location to maps activity
     */
    @Override
    public void onLocationChanged(Location location){
        mLastKnownLocation = location;
        for(LocationCallback temp: mLocationCallback) {
            temp.handleNewLocation(location);
        }
        //Update Location for the provider
        //mListener.onLocationChanged(location);
    }

    void connect(){
        mGoogleApiClient.connect();
    }

    void disconnect(){
        mGoogleApiClient.disconnect();
        //mListener = null;
    }


   /* @Override
    public void onPause(){
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createLocationRequest();
        buildGoogleApiClient();

        //defineLocationSettings();

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }*/
}
