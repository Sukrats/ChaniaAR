package tuc.christos.chaniacitywalk2.wikitude;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

//import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
//import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.StartupConfiguration;
//import com.wikitude.common.camera.CameraSettings;

import org.json.JSONArray;

import java.io.IOException;
import java.util.List;

import tuc.christos.chaniacitywalk2.mInterfaces.IServiceListener;
import tuc.christos.chaniacitywalk2.R;
import tuc.christos.chaniacitywalk2.collection.SceneDetailActivity;
import tuc.christos.chaniacitywalk2.collection.SceneDetailFragment;
import tuc.christos.chaniacitywalk2.data.DataManager;
import tuc.christos.chaniacitywalk2.locationService.LocationService;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.utils.Constants;
import tuc.christos.chaniacitywalk2.utils.JsonHelper;

/**
 * Created by Christos on 19-May-17.
 * Augmented Reality Native Activity
 */

public class ArNavigationActivity extends Activity {

    protected final String TAG = "ARNav";

    protected DataManager mDataManager = DataManager.getInstance();

    /**
     * holds the Wikitude SDK AR-View, this is where camera, markers, compass, 3D models etc. are rendered
     */
    protected ArchitectView architectView;

    /**
     * sensor accuracy listener in case you want to display calibration hints
     */
    protected ArchitectView.SensorAccuracyChangeListener sensorAccuracyListener;

    /**
     * last known location of the user, used internally for content-loading after user location was fetched
     */
    protected Location lastKnownLocation;
    /*
     * JS interface listener handling e.g. 'AR.platform.sendJSONObject({foo:"bar", bar:123})' calls in JavaScript
     */
    //protected ArchitectJavaScriptInterfaceListener mArchitectJavaScriptInterfaceListener;

    /**
     * worldLoadedListener receives calls when the AR WorldToLoad is finished loading or when it failed to laod.
     */
    protected ArchitectView.ArchitectWorldLoadedListener worldLoadedListener;

    protected ArchitectView.ArchitectUrlListener urlListener;

    private long lastCalibrationToastShownTimeMillis = System.currentTimeMillis();

    protected String[] args;

    protected boolean isLoading = false;

    protected LocationService mService;
    final ServiceConnection mConnection =  new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.mIBinder binder = (LocationService.mIBinder) service;
            mService = binder.getService();
            mService.registerServiceListener(mLocationServiceListener);
            mService.isFenceTriggered();
            mBount = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBount = false;
            mService.removeListener(mLocationServiceListener);
        }
    };
    final IServiceListener mLocationServiceListener = new IServiceListener() {
        @Override
        public void drawGeoFences(String[] areaIds, int radius) {

        }

        @Override
        public void userEnteredArea(String areaID) {
            callJavaScript("World.userEnteredArea",new String[]{areaID});
            Log.i("GeoFence","Fence Triggered: "+areaID);
        }

        @Override
        public void userLeftArea(String areaID) {
            callJavaScript("World.userLeftArea",new String[]{areaID});
            Log.i("GeoFence","Fence Removed: "+areaID);
        }

        @Override
        public void handleNewLocation(Location location) {
            ArNavigationActivity.this.lastKnownLocation = location;
            //update JS Location
            ArNavigationActivity.this.architectView.setLocation(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        }
    };

    protected boolean mBount = false;
    protected  String WorldToLoad = "";
    protected String scene_id = "";



    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_architect);
        architectView = (ArchitectView) findViewById(R.id.architectView);
        WorldToLoad = getIntent().getStringExtra(Constants.ARCHITECT_WORLD_KEY);
        scene_id = getIntent().getStringExtra(Constants.ARCHITECT_AR_SCENE_KEY);
        /*final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
        config.setFeatures(1);
        config.setLicenseKey(Constants.WIKITUDE_SDK_KEY);
        config.setCameraResolution(CameraSettings.CameraResolution.AUTO);
        config.setCameraPosition(CameraSettings.CameraPosition.DEFAULT);*/
        final StartupConfiguration config = new StartupConfiguration(Constants.WIKITUDE_SDK_KEY, 1, StartupConfiguration.CameraPosition.DEFAULT);

        //architectView.setCameraLifecycleListener(null);

        try {
            /* first mandatory life-cycle notification */
            architectView.onCreate(config);
        } catch (RuntimeException rex) {
            architectView = null;
            Toast.makeText(getApplicationContext(), "can't create Architect View", Toast.LENGTH_SHORT).show();
            Log.e(this.getClass().getName(), "Exception in ArchitectView.onCreate()", rex);
        }

        urlListener = new ArchitectView.ArchitectUrlListener() {
            @Override
            public boolean urlWasInvoked(String s) {
                Uri invokedUri = Uri.parse(s);
                switch (invokedUri.getHost()){
                    case "details":
                        Intent intent = new Intent(getApplicationContext(), SceneDetailActivity.class);
                        intent.putExtra(SceneDetailFragment.ARG_ITEM_ID, String.valueOf(invokedUri.getQueryParameter("id")));
                        startActivity(intent);
                        return true;
                    case "map":
                        Intent mapIntent = NavUtils.getParentActivityIntent(ArNavigationActivity.this);
                        mapIntent.putExtra(SceneDetailFragment.ARG_ITEM_ID, String.valueOf(invokedUri.getQueryParameter("id")));
                        NavUtils.navigateUpTo(ArNavigationActivity.this, mapIntent);
                        return true;
                    case "mark":
                        try {
                            architectView.load("ModelAtGeoLocation/index.html");
                            callJavaScript("World.ShowBackBtn",new String[]{});
                            injectArgs("World.getScene",new String[]{JsonHelper.sceneToJson(mDataManager.getScene(String.valueOf(invokedUri.getQueryParameter("id")))).toString()});
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                        break;
                    case "arNav":
                        try {
                            architectView.load("ArNavigation/index.html");
                            injectData(mDataManager.getScenes());
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                        break;
                    case "score":
                        if(invokedUri.getBooleanQueryParameter("success",false)){
                            Log.i("Answer","correct");
                            mDataManager.getActivePlayer().updateScore(true);
                            mDataManager.addVisit(Long.valueOf(invokedUri.getQueryParameter("id")));
                        }else{
                            Log.i("Answer","wrong");
                            mDataManager.getActivePlayer().updateScore(false);
                        }
                        break;

                    default:
                        Toast.makeText(getApplicationContext(),"Got url: "+invokedUri.getHost(), Toast.LENGTH_SHORT).show();
                        return true;
                }
                return true;
            }
        };
        // register valid urlListener in architectView, ensure this is set before content is loaded to not miss any event
        if (architectView != null) {
            architectView.registerUrlListener(urlListener);
        }

        worldLoadedListener = getWorldLoadedListener();
        if (worldLoadedListener != null && architectView != null) {
            architectView.registerWorldLoadedListener(worldLoadedListener);
        }

        sensorAccuracyListener = getSensorAccuracyListener();
        architectView.registerSensorAccuracyChangeListener(sensorAccuracyListener);

        WebView.setWebContentsDebuggingEnabled(true);
        Log.i(TAG,"World: "+WorldToLoad);
    }

    private void injectArgs(final String method,final String[] args) {
        if (!isLoading) {
            final Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    isLoading = true;
                    final int WAIT_FOR_LOCATION_STEP_MS = 2000;

                    while (lastKnownLocation == null ) {

                        try {
                            Thread.sleep(WAIT_FOR_LOCATION_STEP_MS);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (lastKnownLocation != null) {
                        callJavaScript(method, args);
                    }
                    isLoading = false;
                }
            });
            t.start();
        }
    }

    private void injectData(final List<Scene> scenes) {
        if (!isLoading) {
            final Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    isLoading = true;
                    final int WAIT_FOR_LOCATION_STEP_MS = 2000;

                    while (lastKnownLocation == null ) {

                        try {
                            Thread.sleep(WAIT_FOR_LOCATION_STEP_MS);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (lastKnownLocation != null) {
                        args = new String[]{scenesToJson(scenes).toString()};
                        callJavaScript("World.loadPoisFromJsonData", args);
                    }
                    isLoading = false;
                }
            });
            t.start();
        }
    }

    private JSONArray scenesToJson(List<Scene> scenes) {
        JSONArray array = new JSONArray();
        for (Scene scene : scenes) {
            array.put(JsonHelper.sceneToJson(scene));
        }
        return array;
    }


    private void callJavaScript(final String methodName, final String[] arguments) {
        final StringBuilder argumentsString = new StringBuilder("");
        Log.i("CallJavaScript", "Arguments Length: " + arguments.length);
        for (int i = 0; i < arguments.length; i++) {
            argumentsString.append(arguments[i]);
            if (i < arguments.length - 1) {
                argumentsString.append(", ");
            }
        }
        if (this.architectView != null) {
            final String js = (methodName + "( " + argumentsString.toString() + " );");
            Log.i("GeoFence", js);
            this.architectView.callJavascript(js);
        }
    }

    public ArchitectView.ArchitectWorldLoadedListener getWorldLoadedListener() {
        return new ArchitectView.ArchitectWorldLoadedListener() {
            @Override
            public void worldWasLoaded(String url) {
                Log.i(TAG, "worldWasLoaded: url: " + url);
            }

            @Override
            public void worldLoadFailed(int errorCode, String description, String failingUrl) {
                Log.e(TAG, "worldLoadFailed: url: " + failingUrl + " " + description);
            }
        };
    }

    public ArchitectView.SensorAccuracyChangeListener getSensorAccuracyListener() {
        return new ArchitectView.SensorAccuracyChangeListener() {
            @Override
            public void onCompassAccuracyChanged(int accuracy) {
                /* UNRELIABLE = 0, LOW = 1, MEDIUM = 2, HIGH = 3 */
                if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_HIGH && System.currentTimeMillis() - ArNavigationActivity.this.lastCalibrationToastShownTimeMillis >= 5000  ) {
                    Toast.makeText(ArNavigationActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG).show();
                    ArNavigationActivity.this.lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
                }
            }
        };
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (architectView != null) {
            // call mandatory live-cycle method of architectView
            this.architectView.onPostCreate();
            try {
                // load content via url in architectView, ensure '<script src="architect://architect.js"></script>' is part of this HTML file,
                // have a look at wikitude.com's developer section for API references
                architectView.load(WorldToLoad);
                if(WorldToLoad.contains("ArNav"))   injectData(mDataManager.getScenes());
                else injectArgs("World.getScene",new String[]{JsonHelper.sceneToJson(mDataManager.getScene(scene_id)).toString()});

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if( !mBount ){
            startService(new Intent(this, LocationService.class));
            bindService(new Intent(this,LocationService.class),mConnection,Context.BIND_NOT_FOREGROUND);
            mBount = true;
        }
        // call mandatory live-cycle method of architectView
        if (architectView != null) {
            architectView.onResume();

            // register accuracy listener in architectView, if set
            if (sensorAccuracyListener != null) {
                architectView.registerSensorAccuracyChangeListener(sensorAccuracyListener);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mBount ){
            mService.removeListener(mLocationServiceListener);
            unbindService(mConnection);
            mBount = false;
        }
        // call mandatory live-cycle method of architectView
        if (architectView != null) {
            architectView.onPause();

            // unregister accuracy listener in architectView, if set
            if (sensorAccuracyListener != null) {
                architectView.unregisterSensorAccuracyChangeListener(sensorAccuracyListener);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // call mandatory live-cycle method of architectView
        if (architectView != null) {
            //architectView.clearCache();
            architectView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (architectView != null) {
            architectView.onLowMemory();
        }
    }


}
