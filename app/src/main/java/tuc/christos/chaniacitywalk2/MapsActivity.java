package tuc.christos.chaniacitywalk2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.support.design.widget.BottomNavigationView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import tuc.christos.chaniacitywalk2.collection.CollectionActivity;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.data.dataManager;
import tuc.christos.chaniacitywalk2.utils.PermissionUtils;


public class MapsActivity extends AppCompatActivity implements
        LocationCallback,
        LocationEventsListener,
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback {


    private dataManager mDataManager;

    private GoogleMap mMap;

    private UiSettings mUiSettings;

    private LocationProvider mLocationProvider;

    private LocationEventHandler mEventHandler;

    private Marker mSelectedMarker = null;

    protected static final String TAG = "MAPS ACTIVITY";

    private final float DEFAULT_ZOOM_LEVEL = 17.0f;

    protected Location mCurrentLocation;

    protected String mLastUpdateTime;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private HashMap<String,Circle> circleMap =new HashMap<>();

    private CameraPosition defaultCameraPosition = new CameraPosition.Builder()
            .target(new LatLng(35.514388, 24.020335)).zoom(DEFAULT_ZOOM_LEVEL).bearing(0).tilt(50).build();

    private boolean camToStart = false;

    private Marker mLocationMarker = null;

    /*BottomNavigationView.OnNavigationItemSelectedListener mItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            buildIntentForActivity(item.getItemId());
            return true;
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_custom);

        //get data Manager instance and read from db
        mDataManager = dataManager.getInstance();
        if(!mDataManager.isInstantiated())
            mDataManager.init(this);

        mEventHandler = new LocationEventHandler(this);
        mLocationProvider = new LocationProvider(this);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            //actionBar.setDisplayHomeAsUpEnabled(true);
        }*/
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        //BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        //bottomNavigationView.setOnNavigationItemSelectedListener(mItemSelectedListener);

        if (savedInstanceState == null) {
            mapFragment.setRetainInstance(true);
            camToStart = true;
        }else camToStart = false;

        mapFragment.getMapAsync(this);
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
        mMap = googleMap;
        mMap.setPadding(0,0,0,100);

        mUiSettings = mMap.getUiSettings();
        //mUiSettings.setZoomControlsEnabled(true);
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.normal_map_json));

            if (!success) {
                Log.i(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.i(TAG, "Can't find style. Error: ", e);
        }

        //mMap.setOnCameraMoveCanceledListener(this);
        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                // Since we return true, we have to show the info window manually
                //marker.showInfoWindow();
                if(!marker.equals(mLocationMarker)) {
                    Scene scene = mDataManager.getSceneFromMarker(marker);
                    if (scene.isHasAR()) {
                        Polyline line = mDataManager.getLineFromScene(scene);
                        if (line.getColor() == Color.BLUE) {
                            line.setColor(Color.GREEN);
                        } else if (line.getColor() == Color.GREEN) {
                            line.setColor(Color.BLUE);
                        }
                    }
                    if (marker.equals(mSelectedMarker)) {
                        mSelectedMarker = null;
                        return true;
                    }
                    mSelectedMarker = marker;
                    // We have handled the click, so don't centre again and return true
                    return false;
                }
                return true;
            }

        });

        setupMapOptions();
        drawMap();
        //enableMyLocation();

    }


    public void moveMyLocationMarker(Location location){
        if(mLocationMarker != null) {

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

                    double x = currentX + t*(targetX - currentX);
                    double y = currentY + t*(targetY - currentY);

                    mLocationMarker.setPosition(new LatLng( x, y));

                    if (t > 0.0 && t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }

                }
            });

        }else{

            Bitmap bm = BitmapFactory.decodeResource(this.getResources(),R.drawable.angry_thor_512px);
            mLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("mLocationMarker")
                    .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bm,64,64,false))));
        }

    }

    public void setupMapOptions() {
    }

    /**
     * INITIALISE MAP STATE BASED ON USER PROGRESS
     */
    private void drawMap() {
        mMap.clear();
        mDataManager.clearMaps();

        for (Scene temp : mDataManager.getScenes()) {

            if (temp.isVisible() && !temp.isHasAR()) {
                LatLng pos = new LatLng(temp.getLatitude(), temp.getLongitude());
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(temp.getName())
                        .snippet(temp.getTAG()));

                if(temp.isVisited()){

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

                }else {
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.unknown_32));
                }

                mDataManager.mapScenetoMarker(temp, marker);
                mDataManager.mapMarkerToScene(marker, temp);
            }else if (temp.isHasAR()){
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
                    mDataManager.mapScenetoMarker(temp, marker);
                    mDataManager.mapMarkerToScene(marker, temp);
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
            }
        }

        if(camToStart)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(defaultCameraPosition));
        //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentCameraPosition));
        //mMap.setMinZoomPreference(DEFAULT_MIN_ZOOM);
        //mMap.setLatLngBoundsForCameraTarget(CHANIA);

    }

    /*private void buildIntentForActivity(int id){
        Intent intent = null;
        switch(id) {
            case R.id.action_profile:
                break;
            //case R.id.action_camera:
              //  break;
            case R.id.action_collection:
                intent = new Intent(this, CollectionActivity.class);
                break;
        }
        if(intent!=null){
            aSyncActivity(intent);
        }

    }*/

    public void handleBottomBarSelection(View view){
        Intent intent = null;
        switch(view.getId()) {
            case R.id.profile_activity:
                break;
            case R.id.collection_activity:
                intent = new Intent(this, CollectionActivity.class);
                break;
        }
        if(intent!=null){
            aSyncActivity(intent);
        }
    }

    public void aSyncActivity(final Intent intent){
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
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).bearing(0).tilt(50).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }
    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {

            mMap.setMyLocationEnabled(true);
            mUiSettings.setMyLocationButtonEnabled(true);
            //mUiSettings.setMapToolbarEnabled(true);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    protected void onStart() {
        mLocationProvider.connect();
        mLocationProvider.setLocationCallbackListener(this);
        mLocationProvider.setLocationCallbackListener(mEventHandler);
        mEventHandler.setLocationEventListener(this);
        super.onStart();
    }

    protected void onStop() {
        mLocationProvider.disconnect();
        mLocationProvider.removeLocationCallbackListener(this);
        mLocationProvider.removeLocationCallbackListener(mEventHandler);
        mEventHandler.removeLocationEventListener(this);
        super.onStop();
    }

    @Override
    public void onMapClick(final LatLng point){
        mSelectedMarker = null;
    }


    public void handleNewLocation(Location location){
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        moveMyLocationMarker(location);
    }

    public void drawGeoFences(String[] areaIds,int radius){
        for(String key: circleMap.keySet()){
                circleMap.get(key).remove();
        }
        circleMap.clear();
        for(String areaID: areaIds){
            Scene scene = mDataManager.getScene(areaID);

            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(scene.getLatitude(),scene.getLongitude()))
                    .radius(radius)
                    .strokeWidth(2)
                    .strokeColor(ContextCompat.getColor(this,R.color.circleStroke))
                    .fillColor(ContextCompat.getColor(this,R.color.circleFill)));

            circleMap.put(areaID,circle);
        }
        Toast.makeText(this, "GeoFences Drawn: " + areaIds.length, Toast.LENGTH_LONG).show();
    }

    public void userEnteredArea(String areaID){
        Scene scene = mDataManager.getScene(areaID);
        Toast.makeText(this,"User Entered Area: "+ scene.getName(), Toast.LENGTH_LONG).show();

        setCameraPosition(scene.getLatitude(), scene.getLongitude(), 20.0f);


    }

    public void userLeftArea(String areaID) {

        if (circleMap.containsKey(areaID)){
            Toast.makeText(this, "User Left Area:" + mDataManager.getScene(areaID).getName(), Toast.LENGTH_LONG).show();
            //Circle circle = circleMap.get(areaID);
            //circle.remove();
            //circleMap.remove(areaID);
        }

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


    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{

        private final View mContents;

        CustomInfoWindowAdapter(){
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents,null);
        }

        @Override
        public View getInfoWindow(Marker marker) {

            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            Scene scene = mDataManager.getSceneFromMarker(marker);
            if (scene.getId() == 1)
                ((ImageView) mContents.findViewById(R.id.badge)).setImageResource(R.drawable.giali_thumb);
            else if (scene.getId() == 2)
                ((ImageView) mContents.findViewById(R.id.badge)).setImageResource(R.drawable.byzantine_thumb);
            else if (scene.getId() == 3)
                ((ImageView) mContents.findViewById(R.id.badge)).setImageResource(R.drawable.kasteli_thumb);
            else if (scene.getId() == 4)
                ((ImageView) mContents.findViewById(R.id.badge)).setImageResource(R.drawable.rocco_thumb);
            else
                ((ImageView) mContents.findViewById(R.id.badge)).setImageResource(0);

            TextView titleUi = ((TextView) mContents.findViewById(R.id.title));
            titleUi.setText(marker.getTitle());

            TextView snippetUi = ((TextView) mContents.findViewById(R.id.snippet));
            snippetUi.setText(marker.getSnippet());

            return mContents;
        }
    }

}