package tuc.christos.chaniacitywalk2;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import tuc.christos.chaniacitywalk2.collectionActivity.SceneDetailActivity;
import tuc.christos.chaniacitywalk2.collectionActivity.SceneDetailFragment;
import tuc.christos.chaniacitywalk2.model.Period;
import tuc.christos.chaniacitywalk2.model.Place;
import tuc.christos.chaniacitywalk2.model.Visit;
import tuc.christos.chaniacitywalk2.utils.DataManager;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Scene;

public class ProfileActivity extends AppCompatActivity {

    static Player mPlayer;
    DataManager mDataManager;

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
        mPlayer = mDataManager.getActivePlayer();

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
            String page;
            switch (position) {
                case 0:
                    page = "INFO";
                    break;
                case 1:
                    page = "LOCAL PROGRESS";
                    break;
                case 2:
                    page = "SAVED PLACES";
                    break;
                case 3:
                    page = "VISITED";
                    break;
                default:
                    page = "unknown";
                    break;
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
            if (index == 2) {
                ArrayList<Scene> items = new ArrayList<>();
                for (Place p : mPlayer.getPlaces().values()) {
                    Scene temp = new Scene(p.getScene_id(), p.getScene_name(), p.getThumb());
                    temp.setComment(p.getComment());
                    temp.setCreated(p.getCreated());
                    temp.setCountry(p.getCountry());
                    temp.setRegion(p.getRegion());
                    temp.setNumOfVisits(p.getScene_visits());
                    temp.setNumOfSaves(p.getScene_saves());
                    items.add(temp);
                }
                rootView = inflater.inflate(R.layout.fragment_profile_siblings, container, false);
                RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.scene_list);
                recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(items, 1));

            } else if (index == 3) {
                ArrayList<Scene> items = new ArrayList<>();
                for (Visit v : mPlayer.getVisited().values()) {
                    Scene temp = new Scene(v.getScene_id(), v.getScene_name(), v.getThumb());
                    temp.setCreated(v.getCreated());
                    temp.setCountry(v.getCountry());
                    temp.setRegion(v.getRegion());
                    temp.setNumOfVisits(v.getScene_visits());
                    temp.setNumOfSaves(v.getScene_saves());
                    items.add(temp);
                }
                rootView = inflater.inflate(R.layout.fragment_profile_siblings, container, false);
                RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.scene_list);
                recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(items));
            } else if (index == 0) {
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
                score.setText(String.valueOf(mPlayer.getScore()));

            } else {
                rootView = inflater.inflate(R.layout.progress_details, container, false);

                ProgressBar overall = (ProgressBar) rootView.findViewById(R.id.overall);
                ProgressBar venetian = (ProgressBar) rootView.findViewById(R.id.venetian);
                ProgressBar ottoman = (ProgressBar) rootView.findViewById(R.id.ottoman);
                ProgressBar modern = (ProgressBar) rootView.findViewById(R.id.modern);

                TextView overall_tx = (TextView) rootView.findViewById(R.id.overall_tx);
                TextView venetian_tx = (TextView) rootView.findViewById(R.id.venetian_tx);
                TextView ottoman_tx = (TextView) rootView.findViewById(R.id.ottoman_tx);
                TextView modern_tx = (TextView) rootView.findViewById(R.id.modern_tx);
                int overProg;
                float overControl = 0;
                List<Scene> o = DataManager.getInstance().getScenes();
                for (Scene scene : o) {
                    if (mPlayer.hasVisited(scene.getId()))
                        overControl++;
                }
                overall_tx.setText((int) overControl + "/" + o.size());
                Log.i("PROGRESS", "overall: " + overControl);
                Log.i("PROGRESS", "overall: " + o.size());


                float t = overControl / o.size();
                overProg = Math.round(t * 100);

                int ottoProg = 0;
                int modernProg = 0;
                int venProg = 0;

                for (int i = 3; i <= 5; i++) {
                    List<Scene> scenes = DataManager.getInstance().getPeriodScenes(i);
                    float control = 0;
                    for (Scene temp : scenes) {
                        if (mPlayer.hasVisited(temp.getId())) {
                            control++;
                            if (temp.getPeriod_id() == 3) {
                                venProg = Math.round(control / scenes.size() * 100);
                                venetian_tx.setText((int) control + "/" + scenes.size());
                            } else if (temp.getPeriod_id() == 4) {
                                ottoProg = Math.round(control / scenes.size() * 100);
                                ottoman_tx.setText((int) control + "/" + scenes.size());
                            } else if (temp.getPeriod_id() == 5) {
                                modernProg = Math.round(control / scenes.size() * 100);
                                modern_tx.setText((int) control + "/" + scenes.size());
                            }
                        }
                    }
                }
                overall.setMax(100);
                overall.setProgress(overProg);

                venetian.setMax(100);
                venetian.setProgress(venProg);

                ottoman.setMax(100);
                ottoman.setProgress(ottoProg);

                modern.setMax(100);
                modern.setProgress(modernProg);
            }

            return rootView;
        }

        static class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

            private final List<Scene> mVisits;
            private final List<Scene> mPlaces;

            SimpleItemRecyclerViewAdapter(List<Scene> items) {
                mVisits = items;
                mPlaces = null;
            }

            SimpleItemRecyclerViewAdapter(List<Scene> items, int dummy) {
                mVisits = null;
                mPlaces = items;
            }

            @Override
            public SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.profile_list_content, parent, false);

                return new SimpleItemRecyclerViewAdapter.ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final SimpleItemRecyclerViewAdapter.ViewHolder holder, int position) {
                if (mVisits != null) {
                    holder.created.setVisibility(View.VISIBLE);
                    final Scene item = mVisits.get(position);
                    holder.mView.setText(item.getName());
                    //holder.marks.setText(String.valueOf(item.getNumOfSaves()));
                    //holder.visits.setText(String.valueOf(item.getNumOfVisits()));
                    holder.region.setText(item.getRegion());
                    holder.created.setText("Visited " + computeTimeDiff(item.getCreated()) + " ago");
                    holder.cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Context context = v.getContext();
                            Intent intent = new Intent(context, SceneDetailActivity.class);
                            intent.putExtra(SceneDetailFragment.ARG_ITEM_ID, Long.toString(item.getId()));
                            if (DataManager.getInstance().getScene(item.getId()) != null)
                                context.startActivity(intent);
                            else
                                Toast.makeText(context, "Scene Data Not Loaded :(", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                if (mPlaces != null) {
                    holder.created.setVisibility(View.GONE);
                    final Scene item = mPlaces.get(position);
                    holder.mView.setText(item.getName());
                    //holder.marks.setText(String.valueOf(item.getNumOfSaves()));
                    //holder.visits.setText(String.valueOf(item.getNumOfVisits()));
                    holder.region.setText(item.getRegion());
                    holder.cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Context context = v.getContext();
                            Intent intent = new Intent(context, SceneDetailActivity.class);
                            intent.putExtra(SceneDetailFragment.ARG_ITEM_ID, Long.toString(item.getId()));
                            if (DataManager.getInstance().getScene(item.getId()) != null)
                                context.startActivity(intent);
                            else
                                Toast.makeText(context, "Scene Data Not Loaded :(", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public int getItemCount() {
                if (mVisits != null)
                    return mVisits.size();
                else if (mPlaces != null)
                    return mPlaces.size();
                else
                    return 0;
            }


            class ViewHolder extends RecyclerView.ViewHolder {
                final TextView mView;
                final TextView region;
                //final TextView marks;
                //final TextView visits;
                final TextView created;
                final CardView cardView;


                ViewHolder(View view) {
                    super(view);
                    mView = (TextView) view.findViewById(R.id.content);
                    region = (TextView) view.findViewById(R.id.region);
                    //marks = (TextView) view.findViewById(R.id.marked);
                    created = (TextView) view.findViewById(R.id.created);
                    //visits = (TextView) view.findViewById(R.id.num);
                    cardView = (CardView) view.findViewById(R.id.card_view);
                }

                @Override
                public String toString() {
                    return super.toString();// + " '" + mContentView.getText() + "'";
                }
            }
        }


    }

    static class SimpleProgressRecyclerViewAdapter extends RecyclerView.Adapter<SimpleProgressRecyclerViewAdapter.ViewHolder> {

        private static ArrayList<Period> periods;

        SimpleProgressRecyclerViewAdapter(ArrayList<Period> items) {
            periods = items;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(final SimpleProgressRecyclerViewAdapter.ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return periods.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ViewHolder(View view) {
                super(view);
            }

            @Override
            public String toString() {
                return super.toString();// + " '" + mContentView.getText() + "'";

            }

        }
    }

    static String computeTimeDiff(java.sql.Date created) {
        String toShow = "...";
        Long time = created.getTime();
        java.util.Date curr = new java.util.Date();

        Long current = curr.getTime() - time;
        Long currentInSeconds = current / 1000;
        Long currentInMinutes = currentInSeconds / 60;
        Long currentInHours = currentInMinutes / 60;
        Long currentInDays = currentInHours / 24;
        Long currentInMonths = currentInDays / 30;
        Long currentInYears = currentInMonths / 12;

        if (currentInYears >= 1) {
            return currentInYears + " years";
        } else if (currentInMonths >= 1) {
            return currentInMonths + " months";
        } else if (currentInDays >= 1) {
            return currentInDays + " days";
        } else if (currentInHours >= 1) {
            return currentInHours + " hours";
        } else if (currentInMinutes >= 1) {
            return currentInMinutes + " min";
        } else if (currentInSeconds >= 1) {
            return currentInSeconds + " sec";
        }
        return toShow;
    }
}
