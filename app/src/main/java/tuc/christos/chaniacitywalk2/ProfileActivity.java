package tuc.christos.chaniacitywalk2;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import tuc.christos.chaniacitywalk2.collection.CollectionActivity;
import tuc.christos.chaniacitywalk2.collection.SceneDetailActivity;
import tuc.christos.chaniacitywalk2.collection.SceneDetailFragment;
import tuc.christos.chaniacitywalk2.data.DataManager;
import tuc.christos.chaniacitywalk2.model.Period;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Scene;

public class ProfileActivity extends AppCompatActivity {

    DataManager mDataManager;
    Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mDataManager = DataManager.getInstance();
        mPlayer = mDataManager.getPlayer();

        TextView name = (TextView) findViewById(R.id.user_profile_name);
        TextView email = (TextView) findViewById(R.id.user_profile_short_bio);
        name.setText(mPlayer.getUsername());
        email.setText(mPlayer.getEmail());

        final SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        final ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    private class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show total pages depending on Periods
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        String page = "";
            switch (position){
                case 0: page ="DETAILS"; break;
                case 1: page ="PROGRESS"; break;
                case 2: page ="SAVED PLACES"; break;
                case 3: page ="VISITED"; break;
                default: page = "unknown"; break;
            }
            return page;
        }
    }


    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int section) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, section);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int index = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView;
            if(index == 2){
                rootView = inflater.inflate(R.layout.fragment_collection_siblings, container, false);
                RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.scene_list);
                recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(
                        DataManager.getInstance().getPlaces(
                                DataManager.getInstance().getActivePlayer().getUsername()
                        )
                ));

            }else if(index == 3){
                rootView = inflater.inflate(R.layout.fragment_collection_siblings, container, false);
                RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.scene_list);
                recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(
                        DataManager.getInstance().getVisits(
                                DataManager.getInstance().getActivePlayer().getUsername()
                        )
                ));
            }else if(index == 0){
                rootView = inflater.inflate(R.layout.profile_details, container, false);

                TextView fName = (TextView) rootView.findViewById(R.id.tx1);
                TextView lName = (TextView) rootView.findViewById(R.id.tx2);
                TextView created = (TextView) rootView.findViewById(R.id.tx3);
                TextView recentActivity = (TextView) rootView.findViewById(R.id.tx4);
                TextView score = (TextView) rootView.findViewById(R.id.tx5);

                Player mPlayer = DataManager.getInstance().getActivePlayer();

                fName.setText(mPlayer.getFirstname());
                lName.setText(mPlayer.getLastname());
                created.setText(mPlayer.getCreated().toString());
                final String activeTime = "Last Active on: " + mPlayer.getRecentActivity();
                recentActivity.setText(activeTime);
                score.setText(mPlayer.getScore().toString());
            }else{
                rootView = inflater.inflate(R.layout.progress_details, container, false);
                ProgressBar overall =(ProgressBar) rootView.findViewById(R.id.overall);
                ProgressBar venetian =(ProgressBar) rootView.findViewById(R.id.venetian);
                ProgressBar ottoman =(ProgressBar) rootView.findViewById(R.id.ottoman);
                ProgressBar modern =(ProgressBar) rootView.findViewById(R.id.modern);

                long overProg = 0;
                int overControl = 0;
                List<Scene> o = DataManager.getInstance().getScenes();
                for(Scene scene: o){
                    if(scene.isVisited())
                        overControl++;
                }
                overProg = overControl/o.size()*100;

                long ottoProg = 0;
                long modernProg = 0;
                long venProg = 0;

                for(int i =3; i<=5;i++){
                    List<Scene> scenes =DataManager.getInstance().getPeriodScenes(i);
                    int control = 0;
                    for(Scene temp: scenes){
                        if(temp.isVisited()){
                            control ++;
                            if(temp.getPeriod_id()==3)
                                venProg = control/scenes.size()*100;
                            else if(temp.getPeriod_id() == 4)
                                ottoProg = control/scenes.size()*100;
                            else if(temp.getPeriod_id() == 5)
                                modernProg = control/scenes.size()*100;
                        }
                    }
                }
                overall.setMax(100);
                overall.setProgress((int)overProg);

                venetian.setMax(100);
                venetian.setProgress((int)venProg);

                ottoman.setMax(100);
                ottoman.setProgress((int)ottoProg);

                modern.setMax(100);
                modern.setProgress((int)modernProg);
            }

            return rootView;
        }

        static class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

            private final List<Scene> mValues;

            SimpleItemRecyclerViewAdapter(List<Scene> items) {
                mValues = items;
            }

            @Override
            public SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.scene_list_content, parent, false);

                return new SimpleItemRecyclerViewAdapter.ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final SimpleItemRecyclerViewAdapter.ViewHolder holder, int position) {
                final Scene item = mValues.get(position);
                DataManager dm = DataManager.getInstance();

            }

            @Override
            public int getItemCount() {
                return mValues.size();
            }


            class ViewHolder extends RecyclerView.ViewHolder {
                final TextView mView;
                final TextView mIdView;
                final CardView cardView;
                final LinearLayout ln;
                final ToggleButton save;

                ViewHolder(View view) {
                    super(view);
                    mIdView = (TextView) view.findViewById(R.id.id);
                    mView = (TextView) view.findViewById(R.id.content);
                    cardView = (CardView) view.findViewById(R.id.card_view);
                    ln = (LinearLayout) view.findViewById(R.id.btn_holder);
                    save = (ToggleButton) view.findViewById(R.id.save_btn);
                }

                @Override
                public String toString() {
                    return super.toString();// + " '" + mContentView.getText() + "'";
                }
            }
        }


    }
}
