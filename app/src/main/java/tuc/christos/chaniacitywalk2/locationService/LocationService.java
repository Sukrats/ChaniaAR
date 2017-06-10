package tuc.christos.chaniacitywalk2.locationService;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import tuc.christos.chaniacitywalk2.MapsActivity;
import tuc.christos.chaniacitywalk2.R;
import tuc.christos.chaniacitywalk2.SettingsActivity;
import tuc.christos.chaniacitywalk2.mInterfaces.IServiceListener;
import tuc.christos.chaniacitywalk2.mInterfaces.LocationCallback;
import tuc.christos.chaniacitywalk2.mInterfaces.LocationEventsListener;
import tuc.christos.chaniacitywalk2.utils.Constants;

public class LocationService extends Service implements LocationCallback, LocationEventsListener {
    final String TAG = "myLocationService";

    Looper mThreadLooper;
    ServiceHandler mServiceHandler;
    String areaid;

    LocationProvider mLocationProvider;
    LocationEventHandler mEventHandler;
    boolean fenceTriggered = false;
    ArrayList<IServiceListener> listeners = new ArrayList<>();

    static boolean isRunning = false;

    /**
     * Called when the service is being created.
     */
    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        if (!isRunning) {
            HandlerThread thread = new HandlerThread("LocationProviderThread",
                    Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();

            mThreadLooper = thread.getLooper();
            mServiceHandler = new ServiceHandler(mThreadLooper);

            Toast.makeText(LocationService.this, "Service Started", Toast.LENGTH_LONG).show();
            mLocationProvider.connect();
            mLocationProvider.setLocationCallbackListener(this);
            mEventHandler.setLocationEventListener(this);
            isRunning = true;
        }
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

            if (intent.getStringExtra("mode") != null)
                mLocationProvider.setMode(intent.getStringExtra("mode"));
            if (intent.getStringExtra("toggle") != null)
                switch (intent.getStringExtra("toggle")) {
                    case "start":
                        break;
                    case "stop":
                        stopSelf();
                        break;
                    default:
                }

        }
        return START_STICKY;
    }

    public static boolean isServiceRunning(){
        return isRunning;
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
        mLocationProvider.removeLocationCallbackListener(this);
        mLocationProvider.disconnect();
        mEventHandler.removeLocationEventListener(this);
        isRunning =false;
    }



    /**
     * Handler that receives messages from the thread
     */
    private final class ServiceHandler extends Handler {

        ServiceHandler(Looper looper) {
            super(looper);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String mode = sharedPreferences.getString(SettingsActivity.pref_key_location_update_interval, "");

            mLocationProvider = new LocationProvider(LocationService.this, mode);
            mEventHandler = new LocationEventHandler();

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

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class mIBinder extends Binder {
        public LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationService.this;
        }
    }

    /**
     * A client is binding to the service with bindService()
     */
    @Override
    public IBinder onBind(Intent intent) {
        hideServiceNotification();
        Log.i("Binder", "onBind called");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mode = sharedPreferences.getString(SettingsActivity.pref_key_location_update_interval, "");
        mLocationProvider.setMode(mode);
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

        if (!keepRunning) {
            isRunning = false;
            Log.i(TAG, "Suspended");
            stopSelf();
            NotificationManager mNotificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(Constants.PERMA_NOTIFICATION_ID);
            return true;
        }

        if(!IsApplicationInForeground()) {
            showServiceNotification();
        }
        //TODO:SET TO BACKGROUND ACCURACY
        mLocationProvider.setMode(LocationProvider.MODE_HIGH_ACCURACY);
        return true;
    }

    /**
     * Called when a client is binding to the service with bindService()
     */
    @Override
    public void onRebind(Intent intent) {
        hideServiceNotification();
        Log.i("Binder", "OnRebind called");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mode = sharedPreferences.getString(SettingsActivity.pref_key_location_update_interval, "");
        Log.i("Binder", mode);

        mLocationProvider.setMode(mode);
    }

    public void showServiceNotification(){
        Intent dialogIntent = new Intent(this, NotificationResult.class);
        dialogIntent.setAction(Constants.ACTION_STOP);
        PendingIntent piStop = PendingIntent.getActivity(this, 0, dialogIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.angry_thor_024px)
                .setContentTitle("Chania AR Location")
                .setContentText("Running! Tap o Pause...")
                .setVibrate(new long[]{ 0L,0L,0L,0L})
                .setSound(Uri.EMPTY)
                .setOngoing(true)
                .setContentIntent(piStop);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Constants.PERMA_NOTIFICATION_ID, mBuilder.build());
    }

    public void hideServiceNotification(){
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
        if (fenceTriggered)
            for (IServiceListener i : listeners) {
                i.userEnteredArea(mEventHandler.getTriggeredArea());
                i.drawGeoFences(mEventHandler.getActiveFences(), LocationEventHandler.MIN_RADIUS);
            }
        return fenceTriggered;
    }

    public void registerServiceListener(IServiceListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(IServiceListener listener) {
        listeners.remove(listener);
    }


    @Override
    public void handleNewLocation(Location location) {
        mEventHandler.handleNewLocation(location);
        for (IServiceListener l : listeners)
            l.handleNewLocation(location);
    }

    @Override
    public void drawGeoFences(String[] areaIds, int radius) {
        for (IServiceListener l : listeners)
            l.drawGeoFences(areaIds, radius);
    }

    @Override
    public void userEnteredArea(String areaID) {
        fenceTriggered = true;
        areaid = areaID;
        Toast.makeText(this, "Entered Area: " + areaID, Toast.LENGTH_SHORT).show();
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
            mNotificationManager.notify(Integer.valueOf(areaID), notification);
        }
    }

    @Override
    public void userLeftArea(String areaID) {
        Toast.makeText(this, "Left Area: " + areaID, Toast.LENGTH_SHORT).show();
        areaid = "";
        fenceTriggered = false;
        if (IsApplicationInForeground())
            for (IServiceListener l : listeners)
                l.userLeftArea(areaID);
        else {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.cancel(Integer.valueOf(areaID));
        }
    }
}