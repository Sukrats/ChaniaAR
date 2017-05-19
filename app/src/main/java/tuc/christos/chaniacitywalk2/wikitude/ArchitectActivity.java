package tuc.christos.chaniacitywalk2.wikitude;

import android.app.Activity;
import android.graphics.Camera;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

//import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
//import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.StartupConfiguration;
//import com.wikitude.common.camera.CameraSettings;

import org.json.JSONArray;

import java.io.IOException;

import tuc.christos.chaniacitywalk2.LocationCallback;
import tuc.christos.chaniacitywalk2.LocationProvider;
import tuc.christos.chaniacitywalk2.R;
import tuc.christos.chaniacitywalk2.utils.Constants;

/**
 * Created by Christos on 19-May-17.
 * Augmented Reality Native Activity
 */

public class ArchitectActivity extends Activity {

    protected final String TAG = "Architect Activity";


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
    protected Location lastKnownLocaton;
    /**
     * location listener receives location updates and must forward them to the architectView
     */
    protected LocationCallback LocationListener;
    /**
     * sample location strategy, you may implement a more sophisticated approach too
     */
    protected LocationProvider locationProvider;

    /**
     * JS interface listener handling e.g. 'AR.platform.sendJSONObject({foo:"bar", bar:123})' calls in JavaScript
     */
    //protected ArchitectJavaScriptInterfaceListener mArchitectJavaScriptInterfaceListener;

    /**
     * worldLoadedListener receives calls when the AR world is finished loading or when it failed to laod.
     */
    protected ArchitectView.ArchitectWorldLoadedListener worldLoadedListener;

    protected ArchitectView.ArchitectUrlListener urlListener;

    private long lastCalibrationToastShownTimeMillis = System.currentTimeMillis();

    protected JSONArray poiData;

    protected boolean isLoading = false;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_architect);
        architectView = (ArchitectView) findViewById(R.id.architectView);

        /*final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
        config.setFeatures(1);
        config.setLicenseKey(Constants.WIKITUDE_SDK_KEY);
        config.setCameraResolution(CameraSettings.CameraResolution.AUTO);
        config.setCameraPosition(CameraSettings.CameraPosition.DEFAULT);*/
        final StartupConfiguration config = new StartupConfiguration( Constants.WIKITUDE_SDK_KEY, 1, StartupConfiguration.CameraPosition.FRONT);

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
                return false;
            }
        };
        // register valid urlListener in architectView, ensure this is set before content is loaded to not miss any event
        if (architectView != null) {
            architectView.registerUrlListener( urlListener );
        }

        worldLoadedListener = getWorldLoadedListener();
        if (worldLoadedListener != null && architectView != null) {
            architectView.registerWorldLoadedListener(worldLoadedListener);
        }

        sensorAccuracyListener = getSensorAccuracyListener();
        LocationListener = new LocationCallback() {
            @Override
            public void handleNewLocation(Location location) {
                lastKnownLocaton = location;
                //update JS Location
                architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAccuracy());
            }
        };
        locationProvider = new LocationProvider(this, LocationListener);


    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(architectView != null){
            // call mandatory live-cycle method of architectView
            this.architectView.onPostCreate();
            try {
                // load content via url in architectView, ensure '<script src="architect://architect.js"></script>' is part of this HTML file,
                // have a look at wikitude.com's developer section for API references
                architectView.load( "" );
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

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
                if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && !ArchitectActivity.this.isFinishing() && System.currentTimeMillis() - ArchitectActivity.this.lastCalibrationToastShownTimeMillis > 5 * 1000) {
                    Toast.makeText(ArchitectActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG).show();
                    ArchitectActivity.this.lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
                }
            }
        };
    }

}
