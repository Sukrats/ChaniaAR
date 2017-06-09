package tuc.christos.chaniacitywalk2;

import android.Manifest;
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
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import tuc.christos.chaniacitywalk2.collection.SceneDetailActivity;
import tuc.christos.chaniacitywalk2.collection.SceneDetailFragment;
import tuc.christos.chaniacitywalk2.data.RestClient;
import tuc.christos.chaniacitywalk2.locationService.IServiceListener;
import tuc.christos.chaniacitywalk2.locationService.LocationService;
import tuc.christos.chaniacitywalk2.model.ArScene;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.utils.Constants;
import tuc.christos.chaniacitywalk2.utils.PermissionUtils;
import tuc.christos.chaniacitywalk2.wikitude.ArNavigationActivity;
import tuc.christos.chaniacitywalk2.collection.CollectionActivity;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.data.DataManager;


public class MapsActivity extends AppCompatActivity implements
        IServiceListener,
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback {

    protected static final String TAG = "MAPS ACTIVITY";

    private final int MY_PERMISSION_REQUEST_CAMERA = 1;

    private DataManager mDataManager;
    private GoogleMap mMap;
    //private LocationProvider mLocationProvider;
    //private LocationEventHandler mEventHandler;

    private HashMap<Scene, Marker> sceneToMarkerMap = new HashMap<>();
    private HashMap<Marker, Scene> markerToSceneMap = new HashMap<>();

    private Marker mSelectedMarker = null;

    protected Location mCurrentLocation;
    protected String mLastUpdateTime;
    private Marker mLocationMarker = null;

    private HashMap<String, Circle> circleMap = new HashMap<>();

    private boolean camToStart = false;
    private boolean camFollow = false;
    private final float DEFAULT_ZOOM_LEVEL = 17.0f;
    private CameraPosition defaultCameraPosition = new CameraPosition.Builder()
            .target(new LatLng(35.514388, 24.020335)).zoom(DEFAULT_ZOOM_LEVEL).bearing(0).tilt(50).build();

    private ImageButton pushButton;
    private boolean isFenceTriggered = false;
    private String fenceTriggered = "";
    public OnInfoWindowElemTouchListener InfoButtonListener;
    MapWrapperLayout mapWrapperLayout;

    boolean mBount = false;
    private LocationService mService;
    final ServiceConnection mConnection =  new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.mIBinder binder = (LocationService.mIBinder) service;
            mService = binder.getService();
            mService.registerServiceListener(MapsActivity.this);
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
        startService(new Intent(this, LocationService.class));
        Log.i("ACTIVITY", "CREATED MAPS ACTIVITY");
        //setContentView(R.layout.activity_maps_custom);
        setContentView(R.layout.custom_map_layout_test);
        bindService(new Intent(this, LocationService.class),mConnection, Context.BIND_NOT_FOREGROUND);

        //PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //get data Manager instance and read from db
        mDataManager = DataManager.getInstance();
        mDataManager.init(this);
        RestClient mRestClient = RestClient.getInstance();
        if (!mDataManager.isInitialised()) {
            mRestClient.getInitialContent(new ClientListener() {
                @Override
                public void onCompleted(boolean success, int httpCode, String msg) {
                    MapsActivity.this.drawMap();
                }

                @Override
                public void onUpdate(int progress, String msg) {
                    Toast.makeText(MapsActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (savedInstanceState == null) {
            mapFragment.setRetainInstance(true);
            camToStart = true;
        } else camToStart = false;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(this, 1, Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_REQUEST_CAMERA);
        else {
            pushButton = (ImageButton) findViewById(R.id.round_button);
            pushButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                /*
                mDataManager.printModsTable();
                Player player = mDataManager.getPlayer();
                player.setFirstname("Christos");
                mDataManager.updatePlayer(player , getApplicationContext());
                mDataManager.printPlaces();*/
                    Intent intent = new Intent(getApplicationContext(), ArNavigationActivity.class);
                    intent.putExtra(Constants.ARCHITECT_WORLD_KEY, "ArNavigation/index.html");
                    aSyncActivity(intent);
                }
            });
        }
        Button myloc = (Button) findViewById(R.id.my_location_btn);
        myloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentLocation != null) {
                    setCameraPosition(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), mMap.getCameraPosition().zoom);
                } else {
                    Toast.makeText(MapsActivity.this.getApplicationContext(), "Please enable Location", Toast.LENGTH_LONG).show();
                }
            }
        });

        if (mCurrentLocation != null) {
            defaultCameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).zoom(DEFAULT_ZOOM_LEVEL)
                    .bearing(0).tilt(50).build();
            camToStart = true;
        }

        String camToItem = getIntent().getStringExtra(SceneDetailFragment.ARG_ITEM_ID);
        if (camToItem != null) {
            Scene temp = mDataManager.getScene(camToItem);
            defaultCameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(temp.getLatitude(), temp.getLongitude())).zoom(DEFAULT_ZOOM_LEVEL).bearing(0).tilt(50).build();
            if (sceneToMarkerMap.containsKey(temp)) sceneToMarkerMap.get(temp).showInfoWindow();
            camToStart = true;
        }
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CAMERA:
                pushButton = (ImageButton) findViewById(R.id.round_button);
                pushButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                /*
                mDataManager.printModsTable();
                Player player = mDataManager.getPlayer();
                player.setFirstname("Christos");
                mDataManager.updatePlayer(player , getApplicationContext());
                mDataManager.printPlaces();*/
                        Intent intent = new Intent(getApplicationContext(), ArNavigationActivity.class);
                        intent.putExtra(Constants.ARCHITECT_WORLD_KEY, "ArNavigation/index.html");
                        startActivity(intent);
                    }
                });
                Toast.makeText(this, "Permission Granted AR experiences are on the ready!", Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(this, "You wont be able to access the AR experiences", Toast.LENGTH_LONG).show();
                break;
        }
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

            }
        });
        mMap.setPadding(0, 0, 0, 100);

        //mUiSettings = mMap.getUiSettings();
        //mUiSettings.setZoomControlsEnabled(true);
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        //mMap.setOnCameraMoveCanceledListener(this);
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                // Since we return true, we have to show the info window manually
                //marker.showInfoWindow();
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
        drawMap();
        //enableMyLocation();
    }


    public void moveMyLocationMarker(Location location) {
        if (mLocationMarker == null || !mLocationMarker.isVisible()) {

            Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.angry_thor_512px);
            mLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("mLocationMarker")
                    .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bm, 64, 64, false))));
        } else {
            Location loc = latLngToLoc(mLocationMarker.getPosition());
            final Handler handler = new Handler();
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
            });

            if (loc.distanceTo(location) >= 5 && camFollow) {
                setCameraPosition(location.getLatitude(), location.getLongitude(), mMap.getCameraPosition().zoom);
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

    private void drawMap() {
        if (mMap != null) {
            mMap.clear();
            sceneToMarkerMap.clear();
            markerToSceneMap.clear();
            if (mLocationMarker != null) mLocationMarker.setVisible(false);
            if (camToStart)
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(defaultCameraPosition));
            Player player = mDataManager.getActivePlayer();
            if (player.getScore() <= 1000 || player.getScore() == null) {
                Log.i("Score", player.getScore() + " < 1000");
                for (ArScene temp : mDataManager.getRoute().values()) {
                    Marker marker;
                    LatLng pos = new LatLng(temp.getLatitude(), temp.getLongitude());
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(temp.getName())
                            .snippet(temp.getTAG()));
                    if (temp.getId() == 36)
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.giali_thumb)));
                    else if (temp.getId() == 37)
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.byzantine_thumb)));
                    else if (temp.getId() == 38)
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.kasteli_thumb)));
                    else if (temp.getId() == 39)
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.rocco_thumb)));
                    else
                        marker = null;
                    if (marker != null) {
                        sceneToMarkerMap.put(temp, marker);
                        markerToSceneMap.put(marker, temp);
                    }
                }
            } else {
                Log.i("Score", player.getScore() + " > 1000");
                for (Scene temp : mDataManager.getScenes()) {
                    if (temp.isSaved() && !temp.isHasAR()) {
                        LatLng pos = new LatLng(temp.getLatitude(), temp.getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(temp.getName())
                                .snippet(temp.getTAG()));
                        if (temp.isVisited()) {
                            if (temp.getTAG() != null && temp.getTAG().equals("Ottoman")) {
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_otto));
                            } else if (temp.getTAG() != null && temp.getTAG().equals("Venetian")) {
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_venetian));
                            } else if (temp.getTAG() != null && temp.getTAG().equals("Modern")) {
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_modern));
                            } else if (temp.getTAG() != null && temp.getTAG().equals("NeoGreek")) {
                                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_ruins));
                            } else {
                                marker = mMap.addMarker(new MarkerOptions().position(pos).title(temp.getName()));
                            }
                        } else {
                            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.unknown_32));
                        }
                        sceneToMarkerMap.put(temp, marker);
                        markerToSceneMap.put(marker, temp);
                    }
                }
            /*else if (temp.isHasAR()){
                    Marker marker;

                    LatLng pos = new LatLng(temp.getLatitude(), temp.getLongitude());
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(temp.getName())
                            .snippet(temp.getTAG()));

                    if (temp.getId() == 1)
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.giali_thumb)));
                    else if (temp.getId() == 2)
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.byzantine_thumb)));
                    else if (temp.getId() == 3)
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.kasteli_thumb)));
                    else if (temp.getId() == 4)
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.rocco_thumb)));
                    else
                        marker = null;


                    if(marker != null){
                        sceneToMarkerMap.put(temp, marker);
                        markerToSceneMap.put(marker, temp);
                    }

                    Polyline line;
                    PolylineOptions options = new PolylineOptions()
                            .width(20)
                            .color(ContextCompat.getColor(this,R.color.lineColor))
                            .geodesic(true);

                    for(LatLng ll: mDataManager.getPolyPoints(temp)){
                        options.add(ll);
                    }
                    line = mMap.addPolyline(options);

                    mDataManager.mapLineToScene(line, temp);
                    mDataManager.mapScenetoLine(temp, line);
                }*/
            }

            //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentCameraPosition));
            //mMap.setMinZoomPreference(DEFAULT_MIN_ZOOM);
            //mMap.setLatLngBoundsForCameraTarget(CHANIA);

        }
    }

    public void handleBottomBarSelection(View view) {
        Intent intent = null;

        switch (view.getId()) {
            case R.id.profile_activity:
                intent = new Intent(this, ProfileActivity.class);
                break;
            case R.id.activity_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.collection_activity:
                intent = new Intent(this, CollectionActivity.class);
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

        }, 200L);
    }

    public void setCameraPosition(double latitude, double longitude, float zoomf) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).zoom(zoomf).bearing(0).tilt(50).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }

    public void setCameraPosition(double latitude, double longitude) {
        Log.i(TAG, String.valueOf(latitude) + String.valueOf(longitude));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).bearing(0).zoom(DEFAULT_ZOOM_LEVEL).tilt(50).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    @Override
    public void onPause() {
        super.onPause();
        if(mBount){
            unbindService(mConnection);
            mBount = false;
        }
        //mLocationProvider.removeLocationCallbackListener(this);
        //mLocationProvider.removeLocationCallbackListener(mEventHandler);
        //mLocationProvider.disconnect();
        //mEventHandler.removeLocationEventListener(this);

    }


    @Override
    public void onResume() {
        super.onResume();


        //Check Preferences File and Update locally
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String mapStyle = sharedPreferences.getString(SettingsActivity.pref_key_map_type, "");
        Boolean follow = sharedPreferences.getBoolean(SettingsActivity.pref_key_camera_follow, false);
        setMapStyle(mapStyle);
        camFollow = follow;

        if(!mBount) {
            bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_NOT_FOREGROUND);
            mBount = true;
        }
        //Resume Location Provider and register
        //the Map activity for location and locationEvent updates
        //mLocationProvider.connect(this);
        //mLocationProvider.setLocationCallbackListener(this);
        //mLocationProvider.setLocationCallbackListener(mEventHandler);
        //mEventHandler.setLocationEventListener(this);
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

    public void drawGeoFences(String[] areaIds, int radius) {
        for (String key : circleMap.keySet()) {
            circleMap.get(key).remove();
        }
        circleMap.clear();
        for (String areaID : areaIds) {
            Scene scene = mDataManager.getScene(areaID);

            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(scene.getLatitude(), scene.getLongitude()))
                    .radius(radius)
                    .strokeWidth(2)
                    .strokeColor(ContextCompat.getColor(this, R.color.circleStroke))
                    .fillColor(ContextCompat.getColor(this, R.color.circleFill)));

            circleMap.put(areaID, circle);
        }
        //Toast.makeText(this, "GeoFences Drawn: " + areaIds.length, Toast.LENGTH_LONG).show();
    }

    public void userEnteredArea(String areaID) {
        Scene scene = mDataManager.getRoute().get(areaID);
        isFenceTriggered = true;
        fenceTriggered = areaID;
        sceneToMarkerMap.get(scene).showInfoWindow();
        setCameraPosition(scene.getLatitude(), scene.getLongitude(), 20.0f);
    }

    public void userLeftArea(String areaID) {
        isFenceTriggered = false;
        fenceTriggered = "";
        //if (circleMap.containsKey(areaID)){
        //Toast.makeText(this, "User Left Area:" + mDataManager.getScene(areaID).getName(), Toast.LENGTH_LONG).show();
        //Circle circle = circleMap.get(areaID);
        //circle.remove();
        //circleMap.remove(areaID);
        //}

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
                ImageButton ar_button = (ImageButton) mContents.findViewById(R.id.ar_button);
                ImageButton det_button = (ImageButton) mContents.findViewById(R.id.details_button);

                final Scene scene = markerToSceneMap.get(marker);
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

                boolean isfence = String.valueOf(markerToSceneMap.get(marker).getId()).equals(fenceTriggered);
                if (!scene.isVisited() && isFenceTriggered && isfence) {
                    mContents.findViewById(R.id.ar_panel).setVisibility(View.VISIBLE);
                    InfoButtonListener = new OnInfoWindowElemTouchListener(ar_button,
                            ResourcesCompat.getDrawable(getResources(), R.color.transparent, null),
                            ResourcesCompat.getDrawable(getResources(), R.color.textBodyColorSecondary, null)) {
                        @Override
                        protected void onClickConfirmed(View v, Marker marker) {
                            Intent intent = new Intent(mContents.getContext(), ArNavigationActivity.class);
                            intent.putExtra(Constants.ARCHITECT_WORLD_KEY, "ModelAtGeoLocation/index.html");
                            intent.putExtra(Constants.ARCHITECT_AR_SCENE_KEY, String.valueOf(scene.getId()));
                            startActivity(intent);
                        }
                    };
                    ar_button.setOnTouchListener(InfoButtonListener);

                    mContents.findViewById(R.id.detail_panel).setVisibility(View.VISIBLE);
                    OnInfoWindowElemTouchListener infoTouchListener = new OnInfoWindowElemTouchListener(det_button,
                            ResourcesCompat.getDrawable(getResources(), R.color.transparent, null),
                            ResourcesCompat.getDrawable(getResources(), R.color.textBodyColorSecondary, null)) {
                        @Override
                        protected void onClickConfirmed(View v, Marker marker) {
                            Intent intent = new Intent(mContents.getContext(), SceneDetailActivity.class);
                            Log.i("", String.valueOf(scene.getId()));
                            intent.putExtra(SceneDetailFragment.ARG_ITEM_ID, String.valueOf(scene.getId()));
                            startActivity(intent);

                        }
                    };
                    det_button.setOnTouchListener(infoTouchListener);
                } else {
                    mContents.findViewById(R.id.ar_panel).setVisibility(View.GONE);
                    mContents.findViewById(R.id.detail_panel).setVisibility(View.GONE);
                }

                mapWrapperLayout.setMarkerWithInfoWindow(marker, mContents);
                return mContents;
            } else {
                mContents.findViewById(R.id.ar_panel).setVisibility(View.GONE);
                mContents.findViewById(R.id.detail_panel).setVisibility(View.GONE);

                Player player = mDataManager.getActivePlayer();
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


}