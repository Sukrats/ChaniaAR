package tuc.christos.chaniacitywalk2.collectionActivity;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tuc.christos.chaniacitywalk2.utils.DataManager;
import tuc.christos.chaniacitywalk2.R;
import tuc.christos.chaniacitywalk2.model.Scene;

/**
 * A fragment representing a single Scene detail screen.
 * This fragment is either contained in a {@link CollectionActivity}
 * in two-pane mode (on tablets) or a {@link SceneDetailActivity}
 * on handsets.
 */
public class SceneDetailFragment extends Fragment {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataManager mDataManager = DataManager.getInstance();

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-WorldToLoad scenario, use a Loader
            // to load content from a content provider.
            Scene tmpScene = mDataManager.getScene(Long.parseLong(getArguments().getString(ARG_ITEM_ID)));
            if(tmpScene != null)
                mItem = tmpScene;

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getName());
                //appBarLayout.setBackgroundResource(R.drawable.unknown);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.scene_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.scene_detail)).setText(mItem.getDescription());
        }

        return rootView;
    }
}
