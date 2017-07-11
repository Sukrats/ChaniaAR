package tuc.christos.chaniacitywalk2;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import tuc.christos.chaniacitywalk2.testSensorService.SensorCheckActivity;
import tuc.christos.chaniacitywalk2.collectionActivity.SceneDetailActivity;
import tuc.christos.chaniacitywalk2.collectionActivity.SceneDetailFragment;
import tuc.christos.chaniacitywalk2.leaderboards.LeaderBoardActivity;
import tuc.christos.chaniacitywalk2.model.Period;
import tuc.christos.chaniacitywalk2.model.Viewport;
import tuc.christos.chaniacitywalk2.mInterfaces.IServiceListener;
import tuc.christos.chaniacitywalk2.locationService.LocationService;
import tuc.christos.chaniacitywalk2.mapCustomUiHelperClasses.MapWrapperLayout;
import tuc.christos.chaniacitywalk2.model.Level;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.utils.Constants;
import tuc.christos.chaniacitywalk2.collectionActivity.CollectionActivity;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.utils.DataManager;


public class MapsActivity extends AppCompatActivity implements
        IServiceListener,
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback {

    protected static final String TAG = "MAPS ACTIVITY";

    private DataManager mDataManager;
    private GoogleMap mMap;

    private HashMap<String, Scene> scenesShown = new HashMap<>();
    private HashMap<Scene, Marker> sceneToMarkerMap = new HashMap<>();
    private HashMap<Marker, Scene> markerToSceneMap = new HashMap<>();

    private Marker mSelectedMarker = null;

    protected Location mCurrentLocation;
    protected String mLastUpdateTime;
    private Marker mLocationMarker = null;
    private Circle mLocationAccuracyCircle = null;

    private HashMap<Long, Circle> circleMap = new HashMap<>();

    private boolean camToStart = false;
    private boolean camFollow = false;
    private final float DEFAULT_ZOOM_LEVEL = 17.0f;
    private CameraPosition defaultCameraPosition = new CameraPosition.Builder()
            .target(new LatLng(35.514388, 24.020335)).zoom(DEFAULT_ZOOM_LEVEL).bearing(0).tilt(50).build();

    private boolean isFenceTriggered = false;
    private long fenceTriggered;
    MapWrapperLayout mapWrapperLayout;
    private Player activePlayer;

    boolean mBount = false;
    private LocationService.mIBinder mBinder;
    private LocationService mService;
    final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.mIBinder binder = (LocationService.mIBinder) service;
            mBinder = binder;
            mService = binder.getService();
            binder.setResultActivity(MapsActivity.this);
            mService.checkLocationSettings();
            mService.updateEventHandlerList(mDataManager.getActiveMapContent());
            mService.registerServiceListener(MapsActivity.this);
            mService.requestFences();
            isFenceTriggered = mService.isFenceTriggered();
            mBount = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService.removeListener(MapsActivity.this);
            mBount = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ACTIVITY", "CREATED MAPS ACTIVITY");
        //setContentView(R.layout.activity_maps_custom);
        setContentView(R.layout.activity_maps);
        //PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //get data Manager instance and read from db
        mDataManager = DataManager.getInstance();
        mDataManager.init(this);
        activePlayer = mDataManager.getActivePlayer();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (savedInstanceState == null) {
            mapFragment.setRetainInstance(true);
            camToStart = true;
        } else camToStart = false;

        ImageButton pushButton = (ImageButton) findViewById(R.id.round_button);
        pushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ArNavigationActivity.class);
                if (isFenceTriggered && mDataManager.getScene(fenceTriggered).hasAR()) {
                    intent.putExtra(Constants.ARCHITECT_WORLD_KEY, "ModelAtGeoLocation/index.html");
                    intent.putExtra(Constants.ARCHITECT_AR_SCENE_KEY, fenceTriggered);
                } else {
                    intent.putExtra(Constants.ARCHITECT_WORLD_KEY, "ArNavigation/index.html");
                }
                aSyncActivity(intent);
            }
        });
        ImageButton myloc = (ImageButton) findViewById(R.id.my_location_btn);
        myloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentLocation != null) {
                    setCameraPosition(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), mMap.getCameraPosition().zoom);
                } else {
                    Toast.makeText(MapsActivity.this.getApplicationContext(), "Please enable Location", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ImageButton sensors = (ImageButton) findViewById(R.id.sensor_button);
        sensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, SensorCheckActivity.class));
            }
        });

        if (mCurrentLocation != null) {
            defaultCameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).zoom(DEFAULT_ZOOM_LEVEL)
                    .bearing(0).tilt(50).build();
        }

        String camToItem = getIntent().getStringExtra(SceneDetailFragment.ARG_ITEM_ID);
        if (camToItem != null) {
            Scene temp;
            if (!activePlayer.getUsername().contains("Guest"))
                temp = mDataManager.getScene(Long.parseLong(camToItem));
            else temp = mDataManager.getArScene(camToItem);
            defaultCameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(temp.getLatitude(), temp.getLongitude())).zoom(DEFAULT_ZOOM_LEVEL).bearing(0).tilt(50).build();
            if (sceneToMarkerMap.containsKey(temp)) sceneToMarkerMap.get(temp).showInfoWindow();
            camToStart = true;
        }
        mapFragment.getMapAsync(this);
        mDataManager.printPlaces();
        mDataManager.printVisits();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_container);
        mapWrapperLayout.init(googleMap, getPixelsFromDp(this, 39 + 20));

        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (!marker.equals(mLocationMarker)) {
                    Scene scene = markerToSceneMap.get(marker);
                    Intent intent = new Intent(getApplicationContext(), SceneDetailActivity.class);
                    Log.i("window", String.valueOf(scene.getId()));
                    intent.putExtra(SceneDetailFragment.ARG_ITEM_ID, String.valueOf(scene.getId()));
                    startActivity(intent);
                }
            }
        });

        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            Log.i("Padding", "Resolved");
            mMap.setPadding(0, 0, 0, TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics()));
        } else {
            Log.i("Padding", "HardCoded");
            mMap.setPadding(0, 0, 0, 120);
        }
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        //mMap.setOnCameraMoveCanceledListener(this);
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                // Since we return true, we have to show the info window manually
                //marker.showInfoWindow();
                if (markerToViewport.containsKey(marker)) {
                    Intent intent = new Intent(getApplicationContext(), ArNavigationActivity.class);
                    intent.putExtra(Constants.ARCHITECT_WORLD_KEY, "InstantExamples/index.html");
                    intent.putExtra(Constants.ARCHITECT_AR_SCENE_KEY, fenceTriggered);
                    intent.putExtra(Constants.ARCHITECT_ORIGIN, markerToViewport.get(marker).getId());
                    startActivity(intent);
                    return true;
                }
                if (!marker.equals(mLocationMarker)) {
                    if (marker.equals(mSelectedMarker)) {
                        mSelectedMarker = null;
                        return true;
                    }
                    mSelectedMarker = marker;
                    // We have handled the click, so don't centre again and return true
                    return false;
                } else {
                    return false;
                }
            }

        });
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String mapStyle = sharedPreferences.getString(SettingsActivity.pref_key_map_type, "");
        setMapStyle(mapStyle);
        setupMapOptions();

        if (!mDataManager.isScenesEmpty()) {
            for (Scene temp : mDataManager.getActiveMapContent()) {
                scenesShown.put(String.valueOf(temp.getId()), temp);
            }
            drawMap();
        } else {
            for (Scene temp : mDataManager.getActiveMapContent()) {
                scenesShown.put(String.valueOf(temp.getId()), temp);
            }
            drawMap();
        }
    }

    private void drawMap() {
        if (mMap != null) {
            mMap.clear();
            sceneToMarkerMap.clear();
            markerToSceneMap.clear();
            if (mLocationMarker != null) {
                mLocationMarker.setVisible(false);
                //defaultCameraPosition = new CameraPosition.Builder()
                //        .target(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).zoom(DEFAULT_ZOOM_LEVEL)
                //        .bearing(0).tilt(50).build();
            }
            if (mLocationAccuracyCircle != null)
                mLocationAccuracyCircle.remove();
            if (camToStart) {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(defaultCameraPosition));
                camToStart = false;
            }

            if (!scenesShown.isEmpty())
                for (Scene temp : scenesShown.values()) {
                    LatLng pos = new LatLng(temp.getLatitude(), temp.getLongitude());
                    Period currentPeriod = mDataManager.getPeriod(String.valueOf(temp.getPeriod_id()));
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(temp.getName())
                            .snippet(currentPeriod.getName()));

                    if (temp.hasAR()) {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ar_photo)));
                    } else {

                        if (activePlayer.hasVisited(temp.getId())) {
                            switch ((int) temp.getPeriod_id()) {
                                case 4:
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_otto));
                                    break;
                                case 3:
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_venetian));
                                    break;
                                case 5:
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_modern));
                                    break;
                                case 2:
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_ruins));
                                    break;
                                case 1:
                                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_ruins));
                                    break;
                                default:
                                    break;
                            }

                        } else {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.unknown_32));
                        }
                    }
                    sceneToMarkerMap.put(temp, marker);
                    markerToSceneMap.put(marker, temp);
                }
        }
    }

    Location updated = new Location("");

    public void moveMyLocationMarker(Location location) {
        if (mLocationMarker == null || !mLocationMarker.isVisible()) {

            Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.angry_thor_512px);
            mLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("mLocationMarker")
                    .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bm, 64, 64, false))));
            mLocationAccuracyCircle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(location.getLatitude(), location.getLongitude()))
                    .radius(location.getAccuracy())
                    .strokeWidth(2)
                    .strokeColor(ContextCompat.getColor(this, R.color.circleStroke))
                    .fillColor(ContextCompat.getColor(this, R.color.circleFill)));
            //setCameraPosition(location.getLatitude(), location.getLongitude(), DEFAULT_ZOOM_LEVEL);
        } else {
            Location loc = latLngToLoc(mLocationMarker.getPosition());
            /*final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long duration = 999;
            final double currentX = mLocationMarker.getPosition().latitude, targetX = location.getLatitude();
            final double currentY = mLocationMarker.getPosition().longitude, targetY = location.getLongitude();
            final Interpolator interpolator = new FastOutSlowInInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.max(interpolator.getInterpolation((float) elapsed / duration), 0);

                    double x = currentX + t * (targetX - currentX);
                    double y = currentY + t * (targetY - currentY);

                    mLocationMarker.setPosition(new LatLng(x, y));

                    if (t > 0.0 && t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }

                }
            });*/
            mLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            mLocationAccuracyCircle.setCenter(new LatLng(location.getLatitude(), location.getLongitude()));
            mLocationAccuracyCircle.setRadius(location.getAccuracy());

            if (updated.distanceTo(location) > 10 && camFollow) {
                setCameraPosition(location.getLatitude(), location.getLongitude(), mMap.getCameraPosition().zoom);
                updated = location;
            }
        }
    }


    public Location latLngToLoc(LatLng latLng) {
        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    //EMPTY FOR THE MOMENT ?????
    public void setupMapOptions() {
    }

    /**
     * INITIALISE MAP STATE BASED ON USER PROGRESS
     * And Preferences
     */

    public void setMapStyle(String mapStyle) {
        // Customise the styling of the base map using a JSON object defined
        // in a raw resource file.
        if (mMap != null)
            try {
                boolean success;
                switch (mapStyle) {
                    case "Night Mode":
                        success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_map_json));
                        break;
                    case "Retro Mode":
                        success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.retro_map_json));
                        break;
                    case "Simple Retro Mode":
                        success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.retro_map_json_simple));
                        break;
                    default:
                        success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.normal_map_json));
                }

                if (!success) {
                    Log.i(TAG, "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.i(TAG, "Can't find style. Error: ", e);
            }
    }


    public void handleBottomBarSelection(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.profile_activity:
                if (!mDataManager.getActivePlayer().getUsername().contains("Guest")) {
                    intent = new Intent(this, ProfileActivity.class);
                } else
                    Toast.makeText(this, "Guest Session", Toast.LENGTH_SHORT).show();
                break;
            case R.id.activity_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.collection_activity:
                intent = new Intent(this, CollectionActivity.class);
                break;
            case R.id.pending_activity:
                intent = new Intent(this, LeaderBoardActivity.class);
                break;
        }
        if (intent != null) {
            aSyncActivity(intent);
        }
    }


    public void aSyncActivity(final Intent intent) {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //start your activity here
                startActivity(intent);
            }
        }, 20L);
    }

    public void setCameraPosition(double latitude, double longitude, float zoomf) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).zoom(zoomf).bearing(0).tilt(50).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void drawGeoFences(long[] areaIds, int radius) {
        for (Long key : circleMap.keySet()) {
            circleMap.get(key).remove();
        }
        circleMap.clear();
        for (long areaID : areaIds) {
            Scene scene = mDataManager.getScene(areaID);
            Log.i("FENCES", "DRAW GEOFENCE MAP+" + scene.getId());
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(scene.getLatitude(), scene.getLongitude()))
                    .radius(radius)
                    .strokeWidth(2)
                    .strokeColor(ContextCompat.getColor(this, R.color.circleStroke))
                    .fillColor(ContextCompat.getColor(this, R.color.circleFill)));

            circleMap.put(areaID, circle);
        }
    }
    /*
    public void setCameraPosition(double latitude, double longitude) {
        Log.i(TAG, String.valueOf(latitude) + String.valueOf(longitude));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).bearing(0).zoom(DEFAULT_ZOOM_LEVEL).tilt(50).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }
    */

    @Override
    public void onPause() {
        super.onPause();
        if (mBount) {
            mService.removeListener(this);
            unbindService(mConnection);
            mBount = false;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        for (Scene scene : mDataManager.getActiveMapContent()) {
            scenesShown.put(String.valueOf(scene.getId()), scene);
        }
        drawMap();
        //Check Preferences File and Update locally
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String mapStyle = sharedPreferences.getString(SettingsActivity.pref_key_map_type, "");
        Boolean follow = sharedPreferences.getBoolean(SettingsActivity.pref_key_camera_follow, false);
        setMapStyle(mapStyle);
        camFollow = follow;

        if (!mBount && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startService(new Intent(this, LocationService.class));
            bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_NOT_FOREGROUND);
        }
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onMapClick(final LatLng point) {
        mSelectedMarker = null;
    }


    public void handleNewLocation(Location location) {
        mCurrentLocation = location;
        //Toast.makeText(this,"Interval: "+ mLastUpdateTime +"\n"+ DateFormat.getTimeInstance().format(new Date()),Toast.LENGTH_SHORT).show();
        //Toast.makeText(this,"Latitude:"+location.getLatitude()+"\nLongitude:"+location.getLongitude(),Toast.LENGTH_SHORT).show();
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        moveMyLocationMarker(location);
    }

    private HashMap<String, Viewport> viewports = new HashMap<>();
    private HashMap<Viewport, Circle> circles = new HashMap<>();
    private HashMap<Marker, Viewport> markerToViewport = new HashMap<>();
    private HashMap<Viewport, Marker> viewportToMarker = new HashMap<>();

    public void userEnteredArea(long areaID) {
        if (isFenceTriggered) {
            Scene scene = scenesShown.get(String.valueOf(areaID));
            //setCameraPosition(scene.getLatitude(), scene.getLongitude(), 20.0f);
            sceneToMarkerMap.get(scene).showInfoWindow();
        }
        isFenceTriggered = true;
        fenceTriggered = areaID;
        Scene scene = scenesShown.get(String.valueOf(areaID));
        if (scene.hasAR() && !scene.getViewports().isEmpty()) {
            for (Viewport view : scene.getViewports()) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(view.getLatitude(), view.getLongitude()))
                        .title(scene.getName())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_video)));
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(view.getLatitude(), view.getLongitude()))
                        .radius(view.getRadius())
                        .strokeWidth(2)
                        .strokeColor(ContextCompat.getColor(this, R.color.circleStroke))
                        .fillColor(ContextCompat.getColor(this, R.color.circleFill)));

                viewports.put(view.getId(), view);
                circles.put(view, circle);

                markerToViewport.put(marker, view);
                viewportToMarker.put(view, marker);
            }
        }
    }

    public void userLeftArea(long areaID) {
        isFenceTriggered = false;
        fenceTriggered = -1;

        if (!circles.isEmpty()) {
            for (Viewport key : circles.keySet()) {
                circles.get(key).remove();
            }
            circles.clear();
            viewports.clear();
        }
        if (!viewportToMarker.isEmpty()) {
            for (Viewport key : viewportToMarker.keySet()) {
                viewportToMarker.get(key).remove();
            }
            viewportToMarker.clear();
            markerToViewport.clear();
            viewports.clear();
        }
        //if (circleMap.containsKey(areaID)){
        //Toast.makeText(this, "User Left Area:" + mDataManager.getScene(areaID).getName(), Toast.LENGTH_LONG).show();
        //Circle circle = circleMap.get(areaID);
        //circle.remove();
        //circleMap.remove(areaID);
        //}

    }

    public void regionChanged(String region, String Country) {
        Toast.makeText(this, "Redraw Map Called", Toast.LENGTH_SHORT).show();
        scenesShown.clear();
        if (!mDataManager.getActiveMapContent().isEmpty()) {
            for (Scene scene : mDataManager.getActiveMapContent()) {
                scenesShown.put(String.valueOf(scene.getId()), scene);
            }
        }
        drawMap();
    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {


        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }


    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mContents;


        CustomInfoWindowAdapter() {
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {

            if (!marker.equals(mLocationMarker)) {
                final Scene scene = markerToSceneMap.get(marker);
                /*ImageButton ar_button = (ImageButton) mContents.findViewById(R.id.ar_button);
                if(mDataManager.getActivePlayer().hasPlaced(scene.getId())){
                    ar_button.setImageResource(R.drawable.ic_check_box_outline_blank_white_24dp);
                }else ar_button.setImageResource(R.drawable.ic_check_box_white_24dp);
                */
                //ImageButton det_button = (ImageButton) mContents.findViewById(R.id.details_button);

                final FrameLayout thumb = (FrameLayout) mContents.findViewById(R.id.thumb);
                final ImageView thumbnail = (ImageView) mContents.findViewById(R.id.thumbnail);
                mContents.findViewById(R.id.locality).setVisibility(View.GONE);

                Glide.with(getApplicationContext())
                        .load(scene.getUriThumb())
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .placeholder(R.drawable.empty_photo)
                        .override(164, 164)
                        .into(thumbnail);
                thumb.setVisibility(View.VISIBLE);

                TextView titleUi = ((TextView) mContents.findViewById(R.id.title));
                titleUi.setText(marker.getTitle());

                TextView snippetUi = ((TextView) mContents.findViewById(R.id.snippet));
                LatLng markerPosition = marker.getPosition();
                Location markerLocation = new Location("");
                markerLocation.setLatitude(markerPosition.latitude);
                markerLocation.setLongitude(markerPosition.longitude);
                String distance = "";
                if (mCurrentLocation != null)
                    distance = "Distance: " + (int) mCurrentLocation.distanceTo(markerLocation) + "m";
                snippetUi.setText(distance);

                /*//if ((!scene.isVisited() || scene.hasAR()) && isFenceTriggered && scene.getId() == fenceTriggered) {
                OnInfoWindowElemTouchListener InfoButtonListener = new OnInfoWindowElemTouchListener(ar_button, marker,
                        ResourcesCompat.getDrawable(getResources(), R.color.transparent, null),
                        ResourcesCompat.getDrawable(getResources(), R.color.textBodyColorSecondary, null)) {
                    @Override
                    protected void onClickConfirmed(View v, Marker marker) {
                        if(activePlayer.hasPlaced(scene.getId())) {
                            mDataManager.clearPlace(scene.getId(),MyApp.getAppContext());
                        }else{
                            mDataManager.savePlace(scene.getId(), MyApp.getAppContext());
                        }
                        scheduleHideAndShow(marker);
                    }
                };
                ar_button.setOnTouchListener(InfoButtonListener);*/


                mContents.findViewById(R.id.controls_panel).setVisibility(View.VISIBLE);
                mapWrapperLayout.setMarkerWithInfoWindow(marker, mContents);

                if (!reshowFlag)
                    scheduleHideAndShow(marker);
                else
                    reshowFlag = false;
                return mContents;
            } else {
                mContents.findViewById(R.id.thumb).setVisibility(View.GONE);
                mContents.findViewById(R.id.controls_panel).setVisibility(View.GONE);
                TextView locality = (TextView) mContents.findViewById(R.id.locality);

                Player player = mDataManager.getActivePlayer();
                Level level = mDataManager.getCurrentLevel();

                String local = level.getCity() + ", (" + level.getCountry_code() + ")" + level.getCountry()
                        + "\nAdmin Area: " + level.getAdminArea()
                        + "\nSub Admin Area: " + level.getSubAdminArea()
                        + "!";

                locality.setText(local);

                TextView titleUi = ((TextView) mContents.findViewById(R.id.title));
                titleUi.setText(player.getUsername());

                String score = "Score: " + player.getScore();
                TextView snippetUi = ((TextView) mContents.findViewById(R.id.snippet));
                snippetUi.setText(score);
                return mContents;
            }
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public boolean reshowFlag = false;

    public void scheduleHideAndShow(final Marker marker) {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (reshowFlag) {
                    marker.showInfoWindow();
                } else {
                    marker.hideInfoWindow();
                    reshowFlag = true;
                    handler.postDelayed(this, 500);
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case 1:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }
    }
}