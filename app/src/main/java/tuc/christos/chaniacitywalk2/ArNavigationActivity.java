package tuc.christos.chaniacitywalk2;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.GeomagneticField;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

//import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
//import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.camera.CameraSettings;
//import com.wikitude.common.camera.CameraSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tuc.christos.chaniacitywalk2.mInterfaces.ContentListener;
import tuc.christos.chaniacitywalk2.mInterfaces.IServiceListener;
import tuc.christos.chaniacitywalk2.collectionActivity.SceneDetailActivity;
import tuc.christos.chaniacitywalk2.collectionActivity.SceneDetailFragment;
import tuc.christos.chaniacitywalk2.model.ArScene;
import tuc.christos.chaniacitywalk2.model.Level;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Viewport;
import tuc.christos.chaniacitywalk2.testSensorService.SensorService;
import tuc.christos.chaniacitywalk2.testSensorService.SensorServiceListener;
import tuc.christos.chaniacitywalk2.utils.DataManager;
import tuc.christos.chaniacitywalk2.locationService.LocationService;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.utils.Constants;
import tuc.christos.chaniacitywalk2.utils.JsonHelper;
import tuc.christos.chaniacitywalk2.utils.RestClient;

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
    protected boolean onlyOnce = false;
    protected float magneticDeclination = 0.0f;
    /**
     * sensorservice listener to simulate instant tracking mechanics
     */
    protected SensorServiceListener mSensorServiceListener = new SensorServiceListener() {
        @Override
        public void updateData(double bearing, double theta, double distance) {

            if (ArNavigationActivity.this.lastKnownLocation != null && !onlyOnce){
                onlyOnce = true;
                Location location = ArNavigationActivity.this.lastKnownLocation;
                GeomagneticField field = new GeomagneticField((float)location.getLatitude(), (float)location.getLatitude(), (float)location.getAltitude(), System.currentTimeMillis());
                magneticDeclination = field.getDeclination();
                Toast.makeText(MyApp.getAppContext(),"Declination: "+magneticDeclination,Toast.LENGTH_SHORT).show();
            }

            //bearing += magneticDeclination;
            callJavaScript("World.OnCrosshairPositionChange", new String[]{String.valueOf((float) bearing), String.valueOf((float) distance)});
        }
    };

    /**
     * last known location of the user, used internally for content-loading after user location was fetched
     */
    protected Location lastKnownLocation;
    /*
     * JS interface listener handling e.g. 'AR.platform.sendJSONObject({foo:"bar", bar:123})' calls in JavaScript
     */
    protected ArchitectJavaScriptInterfaceListener mArchitectJavaScriptInterfaceListener;

    /**
     * worldLoadedListener receives calls when the AR WorldToLoad is finished loading or when it failed to laod.
     */
    protected ArchitectView.ArchitectWorldLoadedListener worldLoadedListener;

    protected ArchitectView.ArchitectUrlListener urlListener;

    private long lastCalibrationToastShownTimeMillis = System.currentTimeMillis();

    protected String[] args;

    protected boolean isLoading = false;

    protected LocationService mService;
    final ServiceConnection mConnection = new ServiceConnection() {
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
    boolean mSensorBount = false;
    SensorService service1;
    final ServiceConnection mSensorConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorService.mIBinder binder = (SensorService.mIBinder) service;
            service1 = binder.getService();
            service1.registerServiceListener(mSensorServiceListener);
            mSensorBount = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service1.removeListener();
            mSensorBount = false;
        }
    };
    private Location instantModelLocation;
    private Location instantTrackingLocation;
    private long updatedInMillis = System.currentTimeMillis();
    final IServiceListener mLocationServiceListener = new IServiceListener() {
        @Override
        public void drawGeoFences(long[] areas, int radius) {

        }

        @Override
        public void regionChanged(String areaIds, String radius) {
            //reload?
        }

        @Override
        public void userEnteredArea(long areaID) {
            if (!mDataManager.getActivePlayer().hasVisited(areaID))
                callJavaScript("World.userEnteredArea", new String[]{String.valueOf(areaID)});
            Log.i("GeoFence", "Fence Triggered: " + areaID);
        }

        @Override
        public void userLeftArea(long areaID) {
            callJavaScript("World.userLeftArea", new String[]{String.valueOf(areaID)});
            Log.i("GeoFence", "Fence Removed: " + areaID);
        }

        @Override
        public void handleNewLocation(Location location) {
            ArNavigationActivity.this.lastKnownLocation = location;
            //update JS Location
            ArNavigationActivity.this.architectView.setLocation(location.getLatitude(), location.getLongitude(), location.getAccuracy());

            if(WorldToLoad.contains("Instant")) {
                //if (instantTrackingLocation == null)
                    //instantTrackingLocation = location;

                //if (instantTrackingLocation.getAccuracy() >= location.getAccuracy() || System.currentTimeMillis() - updatedInMillis > 10000) {
                    instantTrackingLocation = location;
                    double lat1 = location.getLatitude();
                    double lng1 = location.getLongitude();

                    double lat2 = instantModelLocation.getLatitude();
                    double lng2 = instantModelLocation.getLongitude();

                    double distance = instantModelLocation.distanceTo(location);

                    double dLon = (lng2-lng1);
                    double y = Math.sin(dLon) * Math.cos(lat2);
                    double x = Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
                    double bearing = Math.toDegrees((Math.atan2(y, x)));
                    bearing = (360 - ((bearing + 360) % 360));

                    callJavaScript("World.UpdateUserPosition", new String[]{String.valueOf(bearing), String.valueOf(distance)});
                //}
            }
        }
    };

    protected boolean mBount = false;
    protected String WorldToLoad = "";
    protected long scene_id = 0;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_architect);
        architectView = (ArchitectView) findViewById(R.id.architectView);
        WorldToLoad = getIntent().getStringExtra(Constants.ARCHITECT_WORLD_KEY);
        scene_id = getIntent().getLongExtra(Constants.ARCHITECT_AR_SCENE_KEY, 0);


        final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
        config.setLicenseKey(Constants.WIKITUDE_SDK_KEY);
        config.setFeatures(1);
        config.setCameraPosition(CameraSettings.CameraPosition.DEFAULT);
        config.setCameraResolution(CameraSettings.CameraResolution.AUTO);
        config.setCamera2Enabled(false);

        //final StartupConfiguration config = new StartupConfiguration(Constants.WIKITUDE_SDK_KEY, 1, StartupConfiguration.CameraPosition.DEFAULT);

        architectView.setCameraLifecycleListener(null);
        try {
            /* first mandatory life-cycle notification */
            architectView.onCreate(config);
        } catch (RuntimeException rex) {
            architectView = null;
            Toast.makeText(getApplicationContext(), "can't create Architect View", Toast.LENGTH_SHORT).show();
            Log.e(this.getClass().getName(), "Exception in ArchitectView.onCreate()", rex);
        }

        worldLoadedListener = getWorldLoadedListener();
        if (worldLoadedListener != null && architectView != null) {
            architectView.registerWorldLoadedListener(worldLoadedListener);
        }
        // register valid urlListener in architectView, ensure this is set before content is loaded to not miss any event
        //if (architectView != null) {
        //    architectView.registerUrlListener(urlListener);
        //}
        this.mArchitectJavaScriptInterfaceListener = this.getArchitectJavaScriptInterfaceListener();
        if (this.mArchitectJavaScriptInterfaceListener != null && this.architectView != null) {
            this.architectView.addArchitectJavaScriptInterfaceListener(mArchitectJavaScriptInterfaceListener);
        }
        sensorAccuracyListener = getSensorAccuracyListener();
        //architectView.registerSensorAccuracyChangeListener(sensorAccuracyListener);

        WebView.setWebContentsDebuggingEnabled(true);
        Log.i(TAG, "World: " + WorldToLoad);
    }


    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (architectView != null) {
            // call mandatory live-cycle method of architectView
            this.architectView.onPostCreate();
            try {
                architectView.load(WorldToLoad);
                if (WorldToLoad.contains("ArNavigation")) {
                    injectData(mDataManager.getActiveMapContent());
                    injectPlayer(mDataManager.getActivePlayer());
                } else if (WorldToLoad.contains("ModelAtGeoLocation")) {
                    if (!mDataManager.getActivePlayer().getUsername().contains("Guest") && !mDataManager.getActivePlayer().hasVisited(scene_id)) {
                        mDataManager.updatePlayer(true, this);
                        mDataManager.addVisit(scene_id, this);
                    } else {
                        mDataManager.addGuestVisit(scene_id);
                    }
                    injectArgs("World.getScene", new String[]{JsonHelper.arSceneToJson(mDataManager.getArScene(String.valueOf(scene_id))).toString()});
                } else if (WorldToLoad.contains("Instant")) {
                    startService(new Intent(this, SensorService.class));
                    String origin = getIntent().getStringExtra(Constants.ARCHITECT_ORIGIN);
                    Scene scene = mDataManager.getArScene(String.valueOf(scene_id));
                    instantModelLocation = new Location("");
                    ArrayList<ArScene> l = scene.getArScene();
                    instantModelLocation.setLatitude(l.get(0).getLatitude());
                    instantModelLocation.setLongitude(l.get(0).getLongitude());
                    injectArgs("World.getInstantiation", new String[]{JsonHelper.sceneWithViewportToJSON(scene, scene.getViewport(origin)).toString()});

                }
            } catch (IOException e1) {
                Log.i("ARNAV", e1.getMessage());
            }
        }
    }

    private void injectInstant(String method, float[] args) {
        JSONObject json = new JSONObject();
        try {
            json.put("posx", args[0]);
            json.put("posy", args[1]);
            Log.i("JSON", json.toString());
        } catch (JSONException e) {
            Log.i("JSON", e.getMessage());
        }
        injectArgs(method, new String[]{json.toString()});
    }

    private void injectPlayer(Player player) {
        injectArgs("World.InjectPlayer", new String[]{JsonHelper.playerToJson(player).toString()});
    }

    private void injectArgs(final String method, final String[] args) {
        if (!isLoading) {
            final Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    isLoading = true;
                    final int WAIT_FOR_LOCATION_STEP_MS = 2000;

                    while (lastKnownLocation == null) {

                        try {
                            Thread.sleep(WAIT_FOR_LOCATION_STEP_MS);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    if (lastKnownLocation != null) {
                        Log.i(TAG, "Injected Single Scene: " + args[0]);
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

                    while (lastKnownLocation == null) {

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
        Player player = mDataManager.getActivePlayer();
        for (Scene scene : scenes) {
            try {
                JSONObject json = JsonHelper.sceneToJson(scene);
                json.put("hasAR", scene.hasAR());
                json.put("saved", player.hasPlaced(scene.getId()));
                json.put("visited", player.hasVisited(scene.getId()));
                array.put(json);
            } catch (JSONException e) {
                Log.i(TAG, e.getMessage());
            }
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
            Log.i("CallJavaScript", js);
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
                if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_HIGH && System.currentTimeMillis() - ArNavigationActivity.this.lastCalibrationToastShownTimeMillis >= 10000) {
                    Toast.makeText(ArNavigationActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG).show();
                    ArNavigationActivity.this.lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
                }
            }
        };
    }

    public ArchitectJavaScriptInterfaceListener getArchitectJavaScriptInterfaceListener() {
        return new ArchitectJavaScriptInterfaceListener() {
            @Override
            public void onJSONObjectReceived(JSONObject jsonObject) {
                try {
                    switch (jsonObject.getString("action")) {
                        case "details":
                            Intent intent = new Intent(getApplicationContext(), SceneDetailActivity.class);
                            intent.putExtra(SceneDetailFragment.ARG_ITEM_ID, jsonObject.getString("id"));
                            startActivity(intent);
                            break;
                        case "map":
                            Intent mapIntent = NavUtils.getParentActivityIntent(ArNavigationActivity.this);
                            mapIntent.putExtra(SceneDetailFragment.ARG_ITEM_ID, jsonObject.getString("id"));
                            NavUtils.navigateUpTo(ArNavigationActivity.this, mapIntent);
                            break;
                        case "GEOAR":
                            loadWorld("GEOAR", jsonObject.getString("id"));
                            break;
                        case "arNav":
                            loadWorld("arNav", null);
                            break;
                        case "score":
                            if (jsonObject.getBoolean("success")) {
                                Log.i("Answer", "correct" + jsonObject.getString("id"));
                                mDataManager.updatePlayer(true, getApplicationContext());
                                mDataManager.addVisit(jsonObject.getLong("id"), getApplicationContext());
                                injectPlayer(mDataManager.getActivePlayer());
                            } else {
                                Log.i("Answer", "wrong");
                                mDataManager.updatePlayer(false, getApplicationContext());
                                injectPlayer(mDataManager.getActivePlayer());
                            }
                            break;
                        case "mark":
                            if (mDataManager.getActivePlayer().hasPlaced(jsonObject.getLong("id"))) {
                                mDataManager.clearPlace(jsonObject.getLong("id"), getApplicationContext());
                            } else {
                                mDataManager.savePlace(jsonObject.getLong("id"), getApplicationContext());
                            }
                            break;

                        default:
                            Log.i("Got url", jsonObject.getString("action"));
                            break;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "onJSONObjectReceived: ", e);
                }
            }
        };
    }

    public void loadWorld(final String world, final String id) {
        ArNavigationActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (world) {
                        case "GEOAR":
                            architectView.load("ModelAtGeoLocation/index.html");
                            callJavaScript("World.ShowBackBtn", new String[]{});
                            injectArgs("World.getScene", new String[]{JsonHelper.arSceneToJson(mDataManager.getArScene(id)).toString()});
                            break;
                        case "arNav":
                            architectView.load("ArNavigation/index.html");
                            injectData(mDataManager.getActiveMapContent());
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBount) {
            startService(new Intent(this, LocationService.class));
            bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_NOT_FOREGROUND);
            mBount = true;
        }
        if (!mSensorBount) {
            bindService(new Intent(this, SensorService.class), mSensorConnection, Context.BIND_NOT_FOREGROUND);
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
        if (mBount) {
            mService.removeListener(mLocationServiceListener);
            unbindService(mConnection);
            mBount = false;
        }
        if (mSensorBount) {
            unbindService(mSensorConnection);
            mSensorBount = false;
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
