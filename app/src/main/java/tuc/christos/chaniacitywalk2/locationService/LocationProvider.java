package tuc.christos.chaniacitywalk2.locationService;

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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;

import tuc.christos.chaniacitywalk2.MyApp;
import tuc.christos.chaniacitywalk2.mInterfaces.LocationCallback;


class LocationProvider implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener  {

    private static final String TAG = "LocationProvider";

    private ArrayList<LocationCallback> mLocationCallback = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest = new LocationRequest();

    static final String MODE_HIGH_ACCURACY = "High Accuracy";
    static final String MODE_BALANCED_POWER_ACCURACY = "Balanced Power Accuracy";
    static final String MODE_BATTERY_SAVER = "Battery Saver";
    static final String MODE_BACKGROUND = "Background";
    //public static final String MODE_GPS_ONLY = "gps_only";
    /**
     * public constructor
     * @param callback
     * context of calling activity
     */
    LocationProvider (LocationCallback callback,String mode){
        this.mGoogleApiClient = new GoogleApiClient.Builder(MyApp.getAppContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        setLocationCallbackListener(callback);
        setLocationMode(mode);

    }
    /**
     * PUBLIC METHODS TO START AND STOP THE LOCATION PROVIDER
     */

    void connect(){
        //Change Location Provider Settings and then connect to the client
        mGoogleApiClient.connect();

    }
    void disconnect(){
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
    }

    /**
     * Set Location Updates mode based on user settings
     * @param mode
     * param to select between the 3 available modes
     */
    private void setLocationMode(String mode){
        Log.i("requestmode",mode);
        switch(mode){
            case MODE_HIGH_ACCURACY:
                Log.i("LocationMode",MODE_HIGH_ACCURACY);
                createLocationRequest(1000, 1000, LocationRequest.PRIORITY_HIGH_ACCURACY);
                break;
            case MODE_BALANCED_POWER_ACCURACY:
                Log.i("LocationMode",MODE_BALANCED_POWER_ACCURACY);
                createLocationRequest(1000, 1000, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            case MODE_BATTERY_SAVER:
                Log.i("LocationMode",MODE_BATTERY_SAVER);
                createLocationRequest(5000, 5000, LocationRequest.PRIORITY_LOW_POWER);
                break;
            case MODE_BACKGROUND:
                Log.i("LocationMode",MODE_BACKGROUND);
                createLocationRequest(60000, 60000, LocationRequest.PRIORITY_NO_POWER);
                break;
            default:
                Log.i("LocationMode","DEFAULT");
                createLocationRequest(1000, 1000, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }
    }

    void setMode(String mode){
        setLocationMode(mode);
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
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        //try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            //status.startResolutionForResult((AppCompatActivity)mContext, 1000);
                        //} catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        //}
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

        if(mGoogleApiClient.isConnected())
            mGoogleApiClient.reconnect();
    }

    /**
     * Methods to register and remove Listeners from the Location Provider
     * @param callback
     * listener registered to receive location updates
     */
    void setLocationCallbackListener(LocationCallback callback){
        this.mLocationCallback.add(callback);
        Log.i("Location Provider","Listeners Registered: " + mLocationCallback );
    }

    void removeLocationCallbackListener(LocationCallback callback){
        this.mLocationCallback.remove(callback);
        Log.i("Location Provider","Listeners Removed: " + callback );
    }

    /**
     * Invokes listener methods to handle the new locations
     * called periodically from Google Play Services Location Provider
     * @param location
     * new location to be fed to the listeners
     */
    private long updated = System.currentTimeMillis();
    @Override
    public void onLocationChanged(Location location){
        if(System.currentTimeMillis() - updated >= 50000) {
            Toast.makeText(MyApp.getAppContext(), "Accuracy: " + location.getAccuracy() +
                    "\nLocation Mode: " + mLocationRequest.getPriority(), Toast.LENGTH_SHORT).show();
            updated = System.currentTimeMillis();
        }
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
            Log.i(TAG, e.getMessage());
        }
    }

    private void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    static LocationRequest getDefaultLocationSettingsRequest(){
        LocationRequest dr =  new LocationRequest();
        dr.setInterval(1000);
        dr.setFastestInterval(1000/2);
        dr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return dr;
    }

    static GoogleApiClient getmGoogleApiClient(){
            return new GoogleApiClient.Builder(MyApp.getAppContext())
                    .addApi(LocationServices.API)
                    .build();
    }

}
