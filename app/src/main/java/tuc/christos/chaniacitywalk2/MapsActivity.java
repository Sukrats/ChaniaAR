package tuc.christos.chaniacitywalk2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.Collection.SceneListActivity;
import tuc.christos.chaniacitywalk2.Data.dataManager;
import tuc.christos.chaniacitywalk2.Utils.PermissionUtils;
import tuc.christos.chaniacitywalk2.Utils.Tags;


public class MapsActivity extends AppCompatActivity implements
        LocationCallback,
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback {


    private dataManager mDataManager;

    private GoogleMap mMap;

    private UiSettings mUiSettings;

    private LocationProvider mLocationProvider;

    private Marker mSelectedMarker = null;

    protected static final String TAG = "MAPS ACTIVITY";

    protected Location mCurrentLocation;
	
    protected String mLastUpdateTime;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private CameraPosition defaultCameraPosition = new CameraPosition.Builder()
            .target(new LatLng(35.514388, 24.020335)).zoom(17.0f).bearing(0).tilt(50).build();
			
    private boolean camToStart = false;

    BottomNavigationView.OnNavigationItemSelectedListener mItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            buildIntentForActivity(item.getItemId());
            return false;
        }
    };
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

		//initiate location provider
        mLocationProvider = new LocationProvider( this, this);
        //get data Manager instance and read from db
		mDataManager = dataManager.getInstance();
        mDataManager.init(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(mItemSelectedListener);

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
                Scene scene = mDataManager.getSceneFromMarker(marker);
                if (scene.isHasAR()) {
                    Polyline line = mDataManager.getLineFromScene(scene);
                    if (line.getColor() == Color.BLUE) {
                        line.setColor(Color.GREEN);
                    } else if (line.getColor() == Color.GREEN) {
                        line.setColor(Color.BLUE);
                    }
                }
                if(marker.equals(mSelectedMarker)){
                    mSelectedMarker = null;
                    return true;
                }
                mSelectedMarker = marker;
                // We have handled the click, so don't centre again and return true
                return false;
            }

        });

        setupMapOptions();
        drawMap();
        enableMyLocation();

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
                PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);

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
	
	private void buildIntentForActivity(int id){
        Intent intent;
        switch(id) {
            case R.id.action_camera:
                intent = new Intent(this, SceneListActivity.class);
            case R.id.action_collection:
                intent = new Intent(this, SceneListActivity.class);
            case R.id.action_profile:
                intent = new Intent(this, SceneListActivity.class);
            default:
                intent = new Intent(this, SceneListActivity.class);
        }
        startActivity(intent);
    }

   public void setCameraPosition(double latitude, double longitude) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).zoom(17.0f).bearing(0).tilt(50).build();
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
        super.onStart();
    }

    protected void onStop() {
        mLocationProvider.disconnect();
        super.onStop();
    }

	@Override
    public void onMapClick(final LatLng point){
        mSelectedMarker = null;
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

    public void handleNewLocation(Location location){
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
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
