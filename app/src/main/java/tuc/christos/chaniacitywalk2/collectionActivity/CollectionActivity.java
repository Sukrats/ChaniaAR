package tuc.christos.chaniacitywalk2.collectionActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import tuc.christos.chaniacitywalk2.MyApp;
import tuc.christos.chaniacitywalk2.locationService.LocationService;
import tuc.christos.chaniacitywalk2.mInterfaces.ClientListener;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.utils.DataManager;
import tuc.christos.chaniacitywalk2.R;
import tuc.christos.chaniacitywalk2.utils.RestClient;
import tuc.christos.chaniacitywalk2.model.Period;
import tuc.christos.chaniacitywalk2.model.Scene;

/**
 * The {@link android.support.v4.view.PagerAdapter} that will provide
 * fragments for each of the sections. We use a
 * {@link FragmentPagerAdapter} derivative, which will keep every
 * loaded fragment in memory. If this becomes too memory intensive, it
 * may be best to switch to a
 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 */

public class CollectionActivity extends AppCompatActivity {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    DataManager mDataManager = DataManager.getInstance();
    RestClient mRestClient = RestClient.getInstance();
    static int mImageSize;
    static int numOfFragments;
    static Player activePlayer;
    static Location location = LocationService.getLastKnownLocation();

    public static List<Period> periods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_siblings);
        setTitle("");
        mImageSize = getResources().getDimensionPixelSize(R.dimen.image_size) * 2;
        //empty on click listener so that no accidental clicks occur
        activePlayer = mDataManager.getActivePlayer();


        final ImageView imgView = (ImageView) findViewById(R.id.logo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.colorPrimary));
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        // Set up the ViewPager with the sections adapter.

        final ProgressBar progressbar = (ProgressBar) findViewById(R.id.progress);

        final SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        final ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                findViewById(R.id.logocont).setVisibility(View.VISIBLE);
                TextView started = (TextView) findViewById(R.id.started);
                TextView ended = (TextView) findViewById(R.id.ended);

                if (position < numOfFragments - 1) {
                    String start = periods.get(position).getStarted();
                    if (start.contains("-")) {
                        start = start.replace("-", "");
                        start += " BC";
                    } else
                        start += " AD";
                    started.setText(start);

                    String end = periods.get(position).getEnded();
                    if (end.contains("-")) {
                        end = end.replace("-", "");
                        end += " BC";
                    } else
                        end += " AD";
                    ended.setText(end);

                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            Bitmap temp;
                            try {
                                temp = Glide.with(getApplicationContext())
                                        .load(periods.get(position).getUriLogo())
                                        .asBitmap()
                                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                        .override(mImageSize, mImageSize)
                                        .into(100, 100)
                                        .get();
                            } catch (ExecutionException | InterruptedException e) {
                                temp = BitmapFactory.decodeResource(getResources(), R.drawable.period_bg_1);
                            }

                            setImageDrawable(temp);
                            return null;
                        }
                    };
                    task.execute();
                } else {
                    findViewById(R.id.logocont).setVisibility(View.GONE);
                    started.setText("");
                    ended.setText("");
                }
            }

            private void setImageDrawable(final Bitmap bitmap) {
                CollectionActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RoundedBitmapDrawable bm = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        bm.setCircular(true);
                        imgView.setImageDrawable(bm);
                    }
                });
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (!mDataManager.isInitialised()) {
            mRestClient.getInitialContent(new ClientListener() {
                @Override
                public void onCompleted(boolean success, int httpCode, String msg) {
                    if (success) {
                        periods = sortPeriodsList(mDataManager.getPeriods());
                        numOfFragments = periods.size() + 1;
                        progressbar.setVisibility(View.GONE);
                        mViewPager.setAdapter(mSectionsPagerAdapter);
                    }
                }

                @Override
                public void onUpdate(int progress, String msg) {

                }
            });

        } else {
            periods = sortPeriodsList(mDataManager.getPeriods());
            numOfFragments = periods.size() + 1;

            progressbar.setVisibility(View.GONE);
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }
    }

    public List<Period> sortPeriodsList(List<Period> list) {
        Collections.sort(list, new Comparator<Period>() {
            @Override
            public int compare(Period lhs, Period rhs) {
                String dateCur = lhs.getStarted();
                if (dateCur == null)
                    return -1;
                int cur = Integer.valueOf(dateCur);

                String dateNext = rhs.getStarted();
                if (dateNext == null)
                    return 1;
                int next = Integer.valueOf(dateNext);
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return cur > next ? 1 : (cur < next) ? -1 : 0;
            }
        });
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_collection_siblings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
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
            return numOfFragments;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == numOfFragments - 1) {
                return "OVERALL";
            }
            return periods.get(position).getName();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
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
            View rootView = inflater.inflate(R.layout.fragment_collection_siblings, container, false);
            int index = getArguments().getInt(ARG_SECTION_NUMBER);
            Log.i("PERIODS", "index: " + index + " Frag:" + (numOfFragments - 1));
            if (index < numOfFragments - 1) {
                Period period = periods.get(index);
                TextView tx = (TextView) rootView.findViewById(R.id.monuments);

                TextView body = (AppCompatTextView) rootView.findViewById(R.id.body);
                body.setText(period.getDescription());

                ArrayList<Scene> content = new ArrayList<>();
                for (Scene scene : period.getScenesAsList()) {
                    if (activePlayer.hasVisited(scene.getId()))
                        content.add(scene);
                }
                if (content.isEmpty()) {
                    tx.setText("You have not unlocked any monuments yet!");
                } else {
                    tx.setText("Monuments: ");
                }
                RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.scene_list);
                recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(content));
                return rootView;
            }
            TextView body = (AppCompatTextView) rootView.findViewById(R.id.body);
            body.setText("AVAILABLE SCENES LIST: ");
            body.setPadding(5, 5, 5, 5);
            rootView.findViewById(R.id.monuments).setVisibility(View.GONE);
            rootView.findViewById(R.id.history).setVisibility(View.GONE);
            rootView.findViewById(R.id.section_div).setVisibility(View.GONE);

            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.scene_list);

            ArrayList<Scene> scenes = new ArrayList<>();
            for (Scene s : DataManager.getInstance().getScenes()) {
                if (!activePlayer.hasVisited(s.getId()))
                    scenes.add(s);
            }
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(scenes));
            return rootView;

        }


    }


    static class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Scene> mValues;

        SimpleItemRecyclerViewAdapter(List<Scene> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.scene_list_content, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Scene item = mValues.get(position);
            holder.mView.setText(item.getName());
            Glide.with(MyApp.getAppContext())
                    .load(item.getUriThumb())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .override(mImageSize, mImageSize)
                    .placeholder(R.drawable.empty_photo)
                    .into(holder.logo);

            Location loc = new Location("");
            loc.setLatitude(item.getLatitude());
            loc.setLongitude(item.getLongitude());
            holder.distance.setText(""+String.valueOf((long)location.distanceTo(loc))+"m");

            if(activePlayer.hasVisited(item.getId())) {
                holder.locked.setVisibility(View.GONE);
                holder.desc.setText(item.getDescription());
            }else {
                holder.desc.setVisibility(View.GONE);
                holder.locked.setVisibility(View.VISIBLE);
            }
            if (activePlayer.hasPlaced(item.getId()))
                holder.save.setChecked(true);
            else
                holder.save.setChecked(false);

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, SceneDetailActivity.class);
                    intent.putExtra(SceneDetailFragment.ARG_ITEM_ID, Long.toString(item.getId()));
                    context.startActivity(intent);

                }
            });
            holder.ln.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            holder.save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToggleButton tg = (ToggleButton) v;
                    DataManager dm = DataManager.getInstance();
                    if (tg.isChecked()) {
                        Log.i("Place", "Save place: " + item.getId());
                        dm.savePlace(item.getId(), MyApp.getAppContext());
                    } else {
                        Log.i("Place", "Delete place: " + item.getId());
                        dm.clearPlace(item.getId(), MyApp.getAppContext());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mView;
            final ImageView logo;
            final ImageView locked;
            final TextView distance;
            final TextView desc;
            final CardView cardView;
            final LinearLayout ln;
            final ToggleButton save;

            ViewHolder(View view) {
                super(view);
                distance = (TextView) view.findViewById(R.id.overlaytext);

                mView = (TextView) view.findViewById(R.id.content);
                desc = (TextView) view.findViewById(R.id.desc);
                logo = (ImageView) view.findViewById(R.id.img);
                locked = (ImageView) view.findViewById(R.id.lock);
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
