package tuc.christos.chaniacitywalk2.locationService;

/**Created by Christos on 07-Jun-17.
 * asdas
 *
 */

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import tuc.christos.chaniacitywalk2.SettingsActivity;

public class LocationService extends Service implements LocationCallback, LocationEventsListener {
    final String TAG = "myServiceDebug";

    Looper mThreadLooper;
    ServiceHandler mServiceHandler;

    LocationProvider mLocationProvider;
    LocationEventHandler mEventHandler;

    ArrayList<IServiceListener> listeners = new ArrayList<>();

    boolean isRunning = false;

    boolean processForeground = false;
    long lastCheck = System.currentTimeMillis();

    /** indicates whether onRebind should be used */
    boolean mAllowRebind;

    /** Called when the service is being created. */
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
            isRunning = true;
        }
    }

    /** The service is starting, due to a call to startService() */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            mServiceHandler.sendMessage(msg);
            isRunning = true;
        }
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        mLocationProvider.connect(this);
        mLocationProvider.setLocationCallbackListener(this);
        mLocationProvider.setLocationCallbackListener(mEventHandler);
        mEventHandler.setLocationEventListener(this);

        return START_STICKY;
    }

    /** Called when The service is no longer used and is being destroyed */
    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        mLocationProvider.removeLocationCallbackListener(this);
        mLocationProvider.removeLocationCallbackListener(mEventHandler);
        mLocationProvider.disconnect();
        mEventHandler.removeLocationEventListener(this);
    }


    @Override
    public void handleNewLocation(Location location) {
        getProcessStatus();
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
        for (IServiceListener l : listeners)
            l.userEnteredArea(areaID);
    }

    @Override
    public void userLeftArea(String areaID) {
        for (IServiceListener l : listeners)
            l.userLeftArea(areaID);
    }

    /** Handler that receives messages from the thread
     * */
    private final class ServiceHandler extends Handler {

        ServiceHandler(Looper looper) {
            super(looper);
            mLocationProvider = new LocationProvider(LocationService.this);
            mEventHandler = new LocationEventHandler(LocationService.this);

        }

        @Override
        public void handleMessage(Message msg) {


            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }

    /*-*******************************************SERVICE BINDING*************************************************************************/

    /** interface for clients that bind */
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

    public void registerServiceListener(IServiceListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(IServiceListener listener) {
        listeners.remove(listener);
    }

    /** A client is binding to the service with bindService() */
    @Override
    public IBinder onBind(Intent intent) {
        mLocationProvider.connect(this);
        return mBinder;
    }

    //TODO: prefs and service
    /** Called when all clients have unbound with unbindService() */
    @Override
    public boolean onUnbind(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean keepRunning = sharedPreferences.getBoolean(SettingsActivity.pref_key_allow_background_locations, false);

        if(listeners.isEmpty() && !keepRunning) {
            mLocationProvider.disconnect();
            Log.i(TAG,"Suspended");
        }
        Log.i(TAG,"Kept Alive");

        return mAllowRebind;
    }

    /** Called when a client is binding to the service with bindService()*/
    @Override
    public void onRebind(Intent intent) {
        mLocationProvider.connect(this);
    }
    public void getProcessStatus(){
        Log.i(TAG,"Checking Status");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean keepRunning = sharedPreferences.getBoolean(SettingsActivity.pref_key_allow_background_locations, false);

        if(System.currentTimeMillis() - lastCheck > 300000 ){
            lastCheck = System.currentTimeMillis();
            if(!applicationInForeground()){
                if(keepRunning)
                    mLocationProvider.setBackgroundMode();
                else
                    stopSelf();
            }
        }
    }

    private boolean applicationInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> services = activityManager.getRunningAppProcesses();
        boolean isActivityFound = false;

        if (services.get(0).processName
                .equalsIgnoreCase(getPackageName())) {
            isActivityFound = true;
        }

        Log.i(TAG,"Is in Foreground? "+isActivityFound);
        return isActivityFound;
    }

}