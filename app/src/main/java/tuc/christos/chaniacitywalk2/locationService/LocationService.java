package tuc.christos.chaniacitywalk2.locationService;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tuc.christos.chaniacitywalk2.MapsActivity;
import tuc.christos.chaniacitywalk2.MyApp;
import tuc.christos.chaniacitywalk2.R;
import tuc.christos.chaniacitywalk2.SettingsActivity;
import tuc.christos.chaniacitywalk2.WikiduteUtils.ILocationProvider;
import tuc.christos.chaniacitywalk2.WikiduteUtils.WikitudeLocationProvider;
import tuc.christos.chaniacitywalk2.mInterfaces.ContentListener;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.utils.DataManager;
import tuc.christos.chaniacitywalk2.mInterfaces.IServiceListener;
import tuc.christos.chaniacitywalk2.mInterfaces.LocationCallback;
import tuc.christos.chaniacitywalk2.mInterfaces.LocationEventsListener;
import tuc.christos.chaniacitywalk2.model.Level;
import tuc.christos.chaniacitywalk2.utils.Constants;
import tuc.christos.chaniacitywalk2.utils.RestClient;

public class LocationService extends Service implements LocationCallback, LocationEventsListener {
    final String TAG = "myLocationService";

    private DataManager mDataManager = DataManager.getInstance();

    private Looper mThreadLooper;
    private ServiceHandler mServiceHandler;
    private LocationProvider mLocationProvider;
    private LocationEventHandler mEventHandler;

    private ILocationProvider mWikiProvider;
    private long locApitest = 0;
    protected LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(final Location location) {
            if (location != null) {
                if (System.currentTimeMillis() - locApitest > 50000) {
                    locApitest = System.currentTimeMillis();
                    Toast.makeText(LocationService.this, "Accuracy: " + location.getAccuracy(), Toast.LENGTH_SHORT).show();
                }
                LocationService.this.handleNewLocation(location);
            }
        }
    };

    private ArrayList<IServiceListener> listeners = new ArrayList<>();

    private boolean fenceTriggered = false;
    private boolean checking = false;
    private boolean scheduledRegionUpdate = false;
    private static boolean isRunning = false;
    private long updated = 0;
    private long activeFence;
    private Location lastLocationChecked = null;
    private static Location mLastKnownLocation;
    private int retryCount = 0;

    /**
     * Called when the service is being created.
     */
    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean locApi = sharedPreferences.getBoolean(SettingsActivity.pref_key_google_api_client, true);

        if (!isRunning) {

            if (mDataManager.hasLocality()) {
                java.sql.Date updated = mDataManager.getLastLocalityUpdate();
                scheduledRegionUpdate = mDataManager.getCurrentLevel().getAdminArea().equals("unknown") || !isToday(updated.getTime());
                lastLocationChecked = mDataManager.getLastLocalityLocationUpdate();
                Log.i("Geocoder", lastLocationChecked.getLatitude() + "," + lastLocationChecked.getLongitude());
            } else {
                scheduledRegionUpdate = true;
            }

            HandlerThread thread = new HandlerThread("LocationProviderThread",
                    Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();

            mThreadLooper = thread.getLooper();
            mServiceHandler = new ServiceHandler(mThreadLooper);

            Toast.makeText(LocationService.this, "Service Started", Toast.LENGTH_LONG).show();
            if (locApi) {
                mLocationProvider.connect();
            } else {
                mWikiProvider.onResume();
            }
            mEventHandler.setLocationEventListener(this);
            isRunning = true;

        }
    }

    public void updateEventHandlerList(ArrayList<Scene> scenes) {
        mEventHandler.updateSceneList(scenes);
    }

    /**
     * The service is starting, due to a call to startService()
     */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            mServiceHandler.sendMessage(msg);
            isRunning = true;
        }
        if (intent.getExtras() != null) {
            if (intent.hasExtra("swap")) {
                swapLocationProviders(intent.getBooleanExtra("swap", false));
            }
            if (intent.getStringExtra("mode") != null) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean locApi = sharedPreferences.getBoolean(SettingsActivity.pref_key_google_api_client, true);
                if (locApi)
                    mLocationProvider.setMode(intent.getStringExtra("mode"));
            }
            if (intent.getStringExtra("toggle") != null)
                switch (intent.getStringExtra("toggle")) {
                    case "start":
                        break;
                    case "stop":
                        stopSelf();
                        break;
                    default:
                }
            if (intent.getStringExtra("event") != null && intent.getStringExtra("events").equals("update"))
                mEventHandler.updateSceneList(mDataManager.getActiveMapContent());
        }
        return START_STICKY;
    }

    public static boolean isServiceRunning() {
        return isRunning;
    }

    private void swapLocationProviders(boolean flag) {
        if (!flag) {
            Toast.makeText(LocationService.this, "Android.Location API", Toast.LENGTH_SHORT).show();
            mWikiProvider.onResume();
            if (mLocationProvider != null) {
                //mLocationProvider.removeLocationCallbackListener(this);
                mLocationProvider.disconnect();
            }
        } else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String mode = sharedPreferences.getString(SettingsActivity.pref_key_location_update_interval, "");
            if (mWikiProvider != null) {
                mWikiProvider.onPause();
            }
            Toast.makeText(LocationService.this, "Google Api Client", Toast.LENGTH_SHORT).show();
            mLocationProvider.setMode(mode);
            mLocationProvider.connect();
        }
    }

    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {

        /*NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.cancelAll();*/
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

        if (mWikiProvider != null) {
            mWikiProvider.onPause();
        }
        if (mLocationProvider != null) {
            mLocationProvider.disconnect();
        }
        mEventHandler.removeLocationEventListener(this);
        isRunning = false;
    }


    /**
     * Handler that receives messages from the thread
     */
    private final class ServiceHandler extends Handler {

        ServiceHandler(Looper looper) {
            super(looper);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String mode = sharedPreferences.getString(SettingsActivity.pref_key_location_update_interval, "");

            if (!mDataManager.isInstantiated()) {
                mDataManager.init(LocationService.this);
            }
            mLocationProvider = new LocationProvider(LocationService.this, mode);
            mWikiProvider = new WikitudeLocationProvider(LocationService.this, mLocationListener);
            mEventHandler = new LocationEventHandler(mDataManager.getActiveMapContent());

        }

        @Override
        public void handleMessage(Message msg) {


            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }


    /**
     * interface for clients that bind
     */
    mIBinder mBinder = new mIBinder();
    Activity resultActivity;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class mIBinder extends Binder {
        public LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }

        public void setResultActivity(Activity activity) {
            resultActivity = activity;
        }
    }

    /**
     * A client is binding to the service with bindService()
     */
    @Override
    public IBinder onBind(Intent intent) {

        /*if (mDataManager.hasLocality()) {
            java.sql.Date updated = mDataManager.getLastLocalityUpdate();
            scheduledRegionUpdate = !isToday(updated.getTime());//current.after(updated);
            lastLocationChecked = mDataManager.getLastLocalityLocationUpdate();
        }*/

        hideServiceNotification();
        Log.i("Binder", "onBind called");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mode = sharedPreferences.getString(SettingsActivity.pref_key_location_update_interval, "");
        boolean locApi = sharedPreferences.getBoolean(SettingsActivity.pref_key_google_api_client, true);
        if (!locApi) {
            Toast.makeText(LocationService.this, "Android.Location API", Toast.LENGTH_SHORT).show();
            mWikiProvider.onResume();
            if (mLocationProvider != null) {
                //mLocationProvider.removeLocationCallbackListener(this);
                mLocationProvider.disconnect();
            }
            return mBinder;
        }

        if (mWikiProvider != null) {
            mWikiProvider.onPause();
        }
        Toast.makeText(LocationService.this, "Google Api Client", Toast.LENGTH_SHORT).show();
        mLocationProvider.setMode(mode);
        mLocationProvider.connect();


        return mBinder;
    }

    /**
     * Called when all clients have unbound with unbindService()
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("Binder", "onUnbind called");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean keepRunning = sharedPreferences.getBoolean(SettingsActivity.pref_key_allow_background_locations, false);

        listeners = new ArrayList<>();

        if (!keepRunning && !IsApplicationInForeground()) {
            isRunning = false;
            Log.i(TAG, "Suspended");
            stopSelf();
            NotificationManager mNotificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(Constants.PERMA_NOTIFICATION_ID);
            return true;
        }

        if (!IsApplicationInForeground()) {
            showServiceNotification();
        }
        if (mLocationProvider != null)
            mLocationProvider.setMode(LocationProvider.MODE_HIGH_ACCURACY);
        return true;
    }

    /**
     * Called when a client is binding to the service with bindService()
     */
    @Override
    public void onRebind(Intent intent) {
        /*if (mDataManager.hasLocality()) {
            java.sql.Date updated = mDataManager.getLastLocalityUpdate();
            scheduledRegionUpdate = !isToday(updated.getTime());//current.after(updated);
            lastLocationChecked = mDataManager.getLastLocalityLocationUpdate();
        }*/
        hideServiceNotification();
        Log.i("Binder", "OnRebind called");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mode = sharedPreferences.getString(SettingsActivity.pref_key_location_update_interval, "");
        boolean locApi = sharedPreferences.getBoolean(SettingsActivity.pref_key_google_api_client, true);
        Log.i("Binder", mode);
        if (!locApi) {
            Toast.makeText(LocationService.this, "Android.Location API", Toast.LENGTH_SHORT).show();
            mLocationProvider.disconnect();
            //mLocationProvider.removeLocationCallbackListener(this);
            mWikiProvider.onResume();
        } else {
            Toast.makeText(LocationService.this, "Google Api Client", Toast.LENGTH_SHORT).show();
            mWikiProvider.onPause();
            mLocationProvider.setMode(mode);
            mLocationProvider.connect();
        }
    }

    public void showServiceNotification() {
        Intent dialogIntent = new Intent(this, NotificationResult.class);
        dialogIntent.setAction(Constants.ACTION_STOP);
        PendingIntent piStop = PendingIntent.getActivity(this, 0, dialogIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.angry_thor_024px)
                .setContentTitle("Chania AR Location")
                .setContentText("Running! Tap o Pause...")
                .setVibrate(new long[]{0L, 0L, 0L, 0L})
                .setSound(Uri.EMPTY)
                .setOngoing(true)
                .setContentIntent(piStop);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.PERMA_NOTIFICATION_ID, mBuilder.build());
    }

    public void hideServiceNotification() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    private boolean IsApplicationInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> services = activityManager.getRunningAppProcesses();
        boolean isActivityFound = false;

        if (services.get(0).processName
                .equalsIgnoreCase(getPackageName())) {
            isActivityFound = true;
        }

        Log.i(TAG, "Is in Foreground? " + isActivityFound);
        return isActivityFound;
    }

    public boolean isFenceTriggered() {
        //Toast.makeText(this, "SERVICE: fencetriggered?" + fenceTriggered, Toast.LENGTH_SHORT).show();
        if (fenceTriggered)
            for (IServiceListener l : listeners)
                l.userEnteredArea(activeFence);
        else
            for (IServiceListener l : listeners)
                l.userLeftArea(activeFence);

        return fenceTriggered;
    }

    public void registerServiceListener(IServiceListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(IServiceListener listener) {
        listeners.remove(listener);
    }

    public static Location getLastKnownLocation() {
        return mLastKnownLocation;
    }

    @Override
    public void handleNewLocation(Location location) {
        mLastKnownLocation = location;
        mEventHandler.handleNewLocation(location);
        for (IServiceListener l : listeners)
            l.handleNewLocation(location);

        if (!checking && scheduledRegionUpdate && System.currentTimeMillis() - updated >= 30 * 1000) {
            Log.i("Geocoder", "schedule pnigetai!");
            updated = System.currentTimeMillis();
            checkForRegionChange(location);
        } else if (!checking && mDataManager.hasLocality() && (lastLocationChecked.distanceTo(location) >= mDataManager.getCurrentLevel().getBound()) && !mDataManager.getCurrentLevel().getAdminArea().equals("unknown")) {
            Log.i("Geocoder", "to last location pnigetai!");
            updated = System.currentTimeMillis();
            checkForRegionChange(location);
        }
    }

    public void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(LocationProvider.getDefaultLocationSettingsRequest());

        //SettingsClient client = LocationServices.getSettingsClient(this);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(LocationProvider.getmGoogleApiClient(), builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>()

        {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(resultActivity, 1);

                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, e.getMessage());
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void userEnteredArea(long areaID) {
        fenceTriggered = true;
        activeFence = areaID;
        //Toast.makeText(this, "Entered Area: " + areaID, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "method: " + IsApplicationInForeground());
        if (IsApplicationInForeground())
            for (IServiceListener l : listeners)
                l.userEnteredArea(areaID);
        else {
            Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Chania AR")
                    .setContentText("Landmark Nearby!");

            Intent resultIntent = new Intent(this, MapsActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MapsActivity.class);
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);
            Notification notification = mBuilder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify((int) (areaID), notification);
        }
    }

    @Override
    public void userLeftArea(long areaID) {
        //Toast.makeText(this, "Left Area: " + areaID, Toast.LENGTH_SHORT).show();
        fenceTriggered = false;
        if (IsApplicationInForeground())
            for (IServiceListener l : listeners)
                l.userLeftArea(areaID);
        else {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.cancel((int) areaID);
        }
    }

   /* public void triggerRegionChange(final Level level) {
        //Toast.makeText(this, "Downloading Scenes For Region:\n" + level.getAdminArea(), Toast.LENGTH_LONG).show();
        mDataManager.clearScenes();
        //mEventHandler.updateSceneList(new ArrayList<Scene>());
        //mEventHandler.removeLocationEventListener(this);
        for (IServiceListener i : listeners)
            i.regionChanged(level.getAdminArea(), level.getCountry());

        final RestClient mRestClient = RestClient.getInstance();
        mRestClient.downloadScenesForLocation(level.getCountry(), level.getAdminArea(), new ContentListener() {
            @Override
            public void downloadComplete(boolean success, int httpCode, String TAG, String msg) {
                if (!success) {
                    Log.i("Download", String.valueOf(httpCode));
                    switch (httpCode) {
                        case 404:
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            scheduledRegionUpdate = false;
                            break;

                        default:
                            if (retryCount < 2) {
                                triggerRegionChange(level);
                                retryCount++;
                                scheduledRegionUpdate = false;
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Server seems to be offline, you should manually try again in a few minutes!",
                                        Toast.LENGTH_LONG).show();
                                scheduledRegionUpdate = false;
                                retryCount = 0;
                            }
                            break;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Scenes For Region Downloaded", Toast.LENGTH_SHORT).show();
                    Log.i("Fence", "Scenes For Region Downloaded");
                    //mEventHandler.setLocationEventListener(LocationService.this);
                    mEventHandler.updateSceneList(mDataManager.getActiveMapContent());
                    scheduledRegionUpdate = false;
                    for (IServiceListener i : listeners) {
                        i.regionChanged(level.getAdminArea(), level.getCountry());
                        i.drawGeoFences(mEventHandler.getActiveFences(), LocationEventHandler.MIN_RADIUS);
                    }
                }
            }
        });
    }*/

    public void requestFences() {
        Log.i("FENCES", "REQUEST FENCES");
        mEventHandler.requestFences();
    }

    public void drawGeoFences(long[] area_ids, int radius) {
        Log.i("FENCES", "DRAW GEOFENCES SERVICE");
        if (!listeners.isEmpty()) {
            for (IServiceListener l : listeners) {
                l.drawGeoFences(area_ids, radius);
            }
        }
    }

    private int count = 0;

    /*
    private void checkForRegionChange(Location location) {
        Toast.makeText(this, "checking region for scenes", Toast.LENGTH_SHORT).show();
        AsyncTask<Location, Void, Level> geoCoderTask = new AsyncTask<Location, Void, Level>() {
            @Override
            protected Level doInBackground(Location... location1) {
                if (Geocoder.isPresent()) {
                    Level level = new Level();
                    try {
                        Geocoder coder = new Geocoder(getApplicationContext());
                        List<Address> addresses = coder.getFromLocation(location1[0].getLatitude(), location1[0].getLongitude(), 20);
                        if (addresses.isEmpty())
                            return null;

                        level.setCountry(addresses.get(0).getCountryName());
                        level.setCountry_code(addresses.get(0).getCountryCode());
                        level.setCity(addresses.get(0).getLocality());
                        level.setAdminArea("");
                        for (Address temp : addresses) {
                            if (temp.getAdminArea() != null)
                                level.setAdminArea(temp.getAdminArea());
                            if (temp.getSubAdminArea() != null)
                                level.setSubAdminArea(temp.getSubAdminArea());
                        }
                        level.setLatitude(location1[0].getLatitude());
                        level.setLongitude(location1[0].getLongitude());
                        return level;
                    } catch (IOException e) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Level level) {
                mDataManager.printExistingLocality();
                if (level == null && count < 2) {
                    Toast.makeText(MyApp.getAppContext(), "retrying...", Toast.LENGTH_SHORT).show();
                    scheduledRegionUpdate = true;
                    count++;
                    return;
                }
                else if (level == null && count == 2) {
                    Toast.makeText(LocationService.this, "Could not locate you! Geocoder Seems to be offline! Check your internet connection and restart the app", Toast.LENGTH_SHORT).show();
                    scheduledRegionUpdate = false;
                    count = 0;
                    return;
                }

                Log.i("Geocoder", "Got Level: " + level.getCountry() + ", " + level.getCity());
                mDataManager.setLevelLocality(level);
                triggerRegionChange(level);
                scheduledRegionUpdate = false;
            }
        };
        geoCoderTask.execute(location);
        lastLocationChecked = location;
    }*/
    private void checkForRegionChange(Location location) {
        checking = true;
        lastLocationChecked = location;
        Toast.makeText(this, "checking location for content", Toast.LENGTH_SHORT).show();
        RestClient mClient = RestClient.getInstance();
        mClient.downloadLevel(location, new ContentListener() {
            @Override
            public void downloadComplete(boolean success, int httpCode, String TAG, String msg) {
                Level level = mDataManager.getCurrentLevel();
                if(success) {
                    triggerRegionLevelChange(level);
                }else{
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    scheduledRegionUpdate = true;
                    checking = false;
                    for (IServiceListener i : listeners) {
                        i.regionChanged(level.getAdminArea(), level.getCountry());
                        i.drawGeoFences(mEventHandler.getActiveFences(), LocationEventHandler.MIN_RADIUS);
                    }
                }
            }
        });

    }

    public void triggerRegionChange(final Level level) {
        mDataManager.clearScenes();
        mDataManager.clearPeriods();
        final RestClient mRestClient = RestClient.getInstance();
        mRestClient.downloadScenesForLocation(level.getCountry(), level.getAdminArea(), new ContentListener() {
            @Override
            public void downloadComplete(boolean success, int httpCode, String TAG, String msg) {
                if (!success) {
                    Log.i("Download", String.valueOf(httpCode));
                    switch (httpCode) {
                        case 404:
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            scheduledRegionUpdate = false;
                            checking = false;
                            break;

                        default:
                            if (retryCount < 2) {
                                triggerRegionChange(level);
                                retryCount++;
                                scheduledRegionUpdate = false;
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Server seems to be offline, you should manually try again in a few minutes!",
                                        Toast.LENGTH_LONG).show();
                                scheduledRegionUpdate = false;
                                checking = false;
                                retryCount = 0;
                            }
                            break;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Level Content Downloaded", Toast.LENGTH_SHORT).show();
                    Log.i("Fence", "Scenes For Region Downloaded");
                    //mEventHandler.setLocationEventListener(LocationService.this);
                    mEventHandler.updateSceneList(mDataManager.getActiveMapContent());
                    scheduledRegionUpdate = false;
                    for (IServiceListener i : listeners) {
                        i.regionChanged(level.getAdminArea(), level.getCountry());
                        i.drawGeoFences(mEventHandler.getActiveFences(), LocationEventHandler.MIN_RADIUS);
                    }
                    checking = false;
                }
            }
        });
    }
    public void triggerRegionLevelChange(final Level level) {
        mDataManager.clearScenes();
        mDataManager.clearPeriods();
        final RestClient mRestClient = RestClient.getInstance();
        mRestClient.downloadLevelContent(level.getAdminAreaID(), new ContentListener() {
            @Override
            public void downloadComplete(boolean success, int httpCode, String TAG, String msg) {
                if (!success) {
                    Log.i("Download", String.valueOf(httpCode));
                    switch (httpCode) {
                        case 404:
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            mEventHandler.updateSceneList(mDataManager.getActiveMapContent());
                            scheduledRegionUpdate = false;
                            checking = false;
                            for (IServiceListener i : listeners) {
                                i.regionChanged(level.getAdminArea(), level.getCountry());
                                i.drawGeoFences(mEventHandler.getActiveFences(), LocationEventHandler.MIN_RADIUS);
                            }
                            break;

                        default:
                            if (retryCount < 2) {
                                triggerRegionLevelChange(level);
                                retryCount++;
                                scheduledRegionUpdate = false;
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Server seems to be offline, you should manually try again in a few minutes!",
                                        Toast.LENGTH_LONG).show();
                                scheduledRegionUpdate = false;
                                retryCount = 0;
                                checking = false;
                            }
                            break;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Level Content Downloaded", Toast.LENGTH_SHORT).show();
                    Log.i("Fence", "Scenes For Region Downloaded");
                    //mEventHandler.setLocationEventListener(LocationService.this);
                    mEventHandler.updateSceneList(mDataManager.getActiveMapContent());
                    scheduledRegionUpdate = false;
                    for (IServiceListener i : listeners) {
                        i.regionChanged(level.getAdminArea(), level.getCountry());
                        i.drawGeoFences(mEventHandler.getActiveFences(), LocationEventHandler.MIN_RADIUS);
                    }
                    checking = false;
                }
            }
        });
    }

    public boolean isToday(long when) {
        Time time = new Time();
        time.set(when);

        int thenYear = time.year;
        int thenMonth = time.month;
        int thenMonthDay = time.monthDay;

        time.set(System.currentTimeMillis() - 7200 * 1000);
        return (thenYear == time.year)
                && (thenMonth == time.month)
                && (thenMonthDay == time.monthDay);
    }

}