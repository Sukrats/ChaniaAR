package tuc.christos.chaniacitywalk2.collection;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.w3c.dom.Text;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import tuc.christos.chaniacitywalk2.ContentListener;
import tuc.christos.chaniacitywalk2.data.DataManager;
import tuc.christos.chaniacitywalk2.R;
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
    static int mImageSize;

    public static List<Period> periods;
    public final String PAGER_POSITION_KEY = "position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_siblings);

        mImageSize = getResources().getDimensionPixelSize(R.dimen.image_size)*2;
        //empty on click listener so that no accidental clicks occur


        final ImageView imgView = (ImageView)findViewById(R.id.logo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this,R.color.colorPrimary));
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        final SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        final ProgressBar progressbar = (ProgressBar) findViewById(R.id.progress);

        final ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Glide.with(getApplicationContext())
                        .load(periods.get(position).getUriLogo())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .placeholder(R.drawable.empty_photo)
                        .override(mImageSize, mImageSize)
                        .into(imgView);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if(mDataManager.isPeriodsEmpty()) {
            mDataManager.downloadPeriods(new ContentListener() {
                @Override
                public void downloadComplete(boolean success, int code) {
                    if(success) {
                        periods = sortPeriodsList(mDataManager.getPeriods());
                        progressbar.setVisibility(View.GONE);
                        mViewPager.setAdapter(mSectionsPagerAdapter);
                    }
                }
            });
        }else{
            periods = sortPeriodsList(mDataManager.getPeriods());
            progressbar.setVisibility(View.GONE);
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }
    }

    public List<Period> sortPeriodsList(List<Period> list){
        Collections.sort(list, new Comparator<Period>() {
            @Override
            public int compare(Period lhs, Period rhs) {
                String dateCur = lhs.getStarted();
                if(dateCur == null)
                    return 1;
                int cur = Integer.valueOf(dateCur);

                String dateNext = rhs.getStarted();
                if(dateNext == null)
                    return -1;
                int next = Integer.valueOf(dateNext);
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return cur > next ? -1 : (cur < next ) ? 1 : 0;
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
        }else if (id == android.R.id.home) {
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
            return periods.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
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
        public static PlaceholderFragment newInstance(int section ) {
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
            Period period = periods.get(index);

            TextView body = (AppCompatTextView) rootView.findViewById(R.id.body);
            body.setText(period.getDescription());

            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.scene_list);
            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(period.getScenesAsList()));
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
            holder.mIdView.setText(String.valueOf(position+1));

            //holder.mContentView.setText(mValues.get(position).getTAG());

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

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id);
                mView = (TextView)view.findViewById(R.id.content);
                cardView = (CardView)view.findViewById(R.id.card_view);
                 ln = (LinearLayout) view.findViewById(R.id.btn_holder);
            }

            @Override
            public String toString() {
                return super.toString();// + " '" + mContentView.getText() + "'";
            }
        }
    }


}
