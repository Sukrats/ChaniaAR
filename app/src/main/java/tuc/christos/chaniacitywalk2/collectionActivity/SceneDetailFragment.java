package tuc.christos.chaniacitywalk2.collectionActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import tuc.christos.chaniacitywalk2.MyApp;
import tuc.christos.chaniacitywalk2.mInterfaces.ContentListener;
import tuc.christos.chaniacitywalk2.utils.DataManager;
import tuc.christos.chaniacitywalk2.R;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.utils.JsonHelper;
import tuc.christos.chaniacitywalk2.utils.RestClient;

/**
 * A fragment representing a single Scene detail screen.
 * This fragment is either contained in a {@link CollectionActivity}
 * in two-pane mode (on tablets) or a {@link SceneDetailActivity}
 * on handsets.
 */
public class SceneDetailFragment extends Fragment implements OnMapReadyCallback {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Scene mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SceneDetailFragment() {
    }

    ArrayList<Uri> images = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataManager mDataManager = DataManager.getInstance();
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-WorldToLoad scenario, use a Loader
            // to load content from a content provider.
            if (!mDataManager.getActivePlayer().getUsername().contains("Guest")) {
                Scene tmpScene = mDataManager.getScene(Long.parseLong(getArguments().getString(ARG_ITEM_ID)));
                if (tmpScene != null)
                    mItem = tmpScene;
            } else {
                Scene tmpScene = mDataManager.getArScene(getArguments().getString(ARG_ITEM_ID));
                if (tmpScene != null)
                    mItem = tmpScene;
            }
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(" ");
            }
        }
        if (savedInstanceState == null)
            setRetainInstance(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getActivity().getApplicationContext());
        // Add a marker for this item and set the camera
        LatLng data = new LatLng(mItem.getLatitude(), mItem.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(data, 15f));
        googleMap.addMarker(new MarkerOptions().position(data)).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.lite_marker));

        // Set the map type back to normal.
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.scene_detail_test, container, false);
        final ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            MapView mapView = (MapView) rootView.findViewById(R.id.lite_map);
            if (mapView != null) {
                mapView.onCreate(null);
                mapView.getMapAsync(this);
            }
            if (DataManager.getInstance().getActivePlayer().hasVisited(mItem.getId()))
                ((TextView) rootView.findViewById(R.id.description)).setText(mItem.getDescription());
            else {
                rootView.findViewById(R.id.locked).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.description).setVisibility(View.GONE);
            }
            //((TextView) rootView.findViewById(R.id.name)).setText(mItem.getName());
            //((TextView) rootView.findViewById(R.id.visits)).setText("Visits:"+String.valueOf(mItem.getNumOfVisits())+"!");
            //((TextView) rootView.findViewById(R.id.saves)).setText("Marked:"+String.valueOf(mItem.getNumOfSaves())+"!");
            ((TextView) rootView.findViewById(R.id.images)).setText(mItem.getUriImages().toString());

            final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.gallery_container);

            String ms = "No images to display";
            if(mItem.getUriImages().toString().isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                rootView.findViewById(R.id.images).setVisibility(View.VISIBLE);
                ((TextView) rootView.findViewById(R.id.images)).setText(ms);
                rootView.findViewById(R.id.right_icon).setVisibility(View.GONE);
                rootView.findViewById(R.id.left_icon).setVisibility(View.GONE);

                return rootView;
            }

            if (images.isEmpty()) {
                progressBar.setVisibility(View.VISIBLE);
                RestClient.getInstance().getImagesUris(mItem.getUriImages(), new ContentListener() {
                    @Override
                    public void downloadComplete(boolean success, int httpCode, String TAG, String msg) {
                        if (success) {
                            images = JsonHelper.parseImagesJsonArray(msg);
                            if (images.isEmpty()) {
                                progressBar.setVisibility(View.GONE);
                                ((TextView) rootView.findViewById(R.id.images)).setText(msg);
                                return;
                            }
                            rootView.findViewById(R.id.images).setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setAdapter(new ImageRecyclerViewAdapter(images));
                        } else {
                            progressBar.setVisibility(View.GONE);
                            ((TextView) rootView.findViewById(R.id.images)).setText(msg);
                        }
                    }
                });

            } else {
                recyclerView.setAdapter(new ImageRecyclerViewAdapter(images));
            }
        }

        return rootView;
    }


    static class ImageRecyclerViewAdapter extends RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder> {

        final ArrayList<Uri> images;

        ImageRecyclerViewAdapter(ArrayList<Uri> links) {
            images = links;
        }

        @Override
        public ImageRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scene_gallery_item, parent, false);
            return new ImageRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ImageRecyclerViewAdapter.ViewHolder holder, int position) {
            Glide.with(MyApp.getAppContext())
                    .load(images.get(position))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .placeholder(R.drawable.empty_photo)
                    .into(holder.image);
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    DataManager.getInstance().setImages(images);
                    Intent intent = new Intent(MyApp.getAppContext(), FullscreenActivity.class);
                    intent.putExtra("position", holder.getAdapterPosition());
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView image;

            ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.gallery_image);

            }

            @Override
            public String toString() {
                return super.toString();// + " '" + mContentView.getText() + "'";
            }
        }
    }
}
