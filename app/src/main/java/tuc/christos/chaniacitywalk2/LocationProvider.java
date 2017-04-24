package tuc.christos.chaniacitywalk2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;

import tuc.christos.chaniacitywalk2.utils.PermissionUtils;

public class LocationProvider implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener  {

    private static final String TAG = "LocationProvider";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private ArrayList<LocationCallback> mLocationCallback = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest = new LocationRequest();
    private Context mContext;

    /**
     * public constructor
     * @param context
     * context of calling activity
     */
    public LocationProvider (Context context){
        this.mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mContext = context;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String mode = sharedPreferences.getString(SettingsActivity.pref_key_location_update_interval,"");
        setLocationMode(mode);

    }
    /**
     * PUBLIC METHODS TO START AND STOP THE LOCATION PROVIDER
     */

    public void connect(Context context){
        //Change Location Provider Settings and then connect to the client
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String mode = sharedPreferences.getString(SettingsActivity.pref_key_location_update_interval,"");
        setLocationMode(mode);
        /*if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) context, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }*/
        mGoogleApiClient.connect();

    }
    public void disconnect(){
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    /**
     * Set Location Updates mode based on user settings
     * @param mode
     * param to select between the 3 available modes
     */
    private void setLocationMode(String mode){
        long HIGH_INTERVAL = 1000;
        long HIGH_FASTEST_INTERVAL = HIGH_INTERVAL /2;
        int HIGH_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;

        long MEDIUM_INTERVAL = 5000;
        long MEDIUM_FASTEST_INTERVAL = MEDIUM_INTERVAL/2;
        int MEDIUM_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

        long SLOW_INTERVAL = 10000;
        long SLOW_FASTEST_INTERVAL = SLOW_INTERVAL/2;
        int SLOW_PRIORITY = LocationRequest.PRIORITY_LOW_POWER;

        switch(mode){
            case "High Accuracy":
                createLocationRequest(HIGH_INTERVAL, HIGH_FASTEST_INTERVAL, HIGH_PRIORITY);
                break;
            case "Balanced Power Accuracy":
                createLocationRequest(MEDIUM_INTERVAL, MEDIUM_FASTEST_INTERVAL, MEDIUM_PRIORITY);
                break;
            case "Battery Saver":
                createLocationRequest(SLOW_INTERVAL, SLOW_FASTEST_INTERVAL, SLOW_PRIORITY);
                break;
            default:
                createLocationRequest(MEDIUM_INTERVAL, MEDIUM_FASTEST_INTERVAL, MEDIUM_PRIORITY);
        }
    }
    /**
     * LOCATION REQUEST VARIABLES
     * **/
    private void createLocationRequest(long interval,long fastestInterval, int priority) {
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setPriority(priority);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    (Activity)mContext,
                                    1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

    }

    /**
     * Methods to register and remove Listeners from the Location Provider
     * @param callback
     * listener registered to receive location updates
     */
    public void setLocationCallbackListener(LocationCallback callback){
        this.mLocationCallback.add(callback);
        Log.i("Location Provider","Listeners Registered: " + mLocationCallback );
    }

    public void removeLocationCallbackListener(LocationCallback callback){
        this.mLocationCallback.remove(callback);
        Log.i("Location Provider","Listeners Removed: " + callback );
    }

    /**
     * Invokes listener methods to handle the new locations
     * called periodically from Google Play Services Location Provider
     * @param location
     * new location to be fed to the listeners
     */
    @Override
    public void onLocationChanged(Location location){
        for(LocationCallback temp: mLocationCallback) {
            temp.handleNewLocation(location);
        }
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
        Log.i(TAG, "Connection suspended.Reconnecting...");
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

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

}
