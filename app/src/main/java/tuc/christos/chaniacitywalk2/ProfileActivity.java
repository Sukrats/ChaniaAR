package tuc.christos.chaniacitywalk2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import tuc.christos.chaniacitywalk2.collectionActivity.SceneDetailActivity;
import tuc.christos.chaniacitywalk2.collectionActivity.SceneDetailFragment;
import tuc.christos.chaniacitywalk2.mInterfaces.ContentListener;
import tuc.christos.chaniacitywalk2.model.Level;
import tuc.christos.chaniacitywalk2.model.Period;
import tuc.christos.chaniacitywalk2.model.Place;
import tuc.christos.chaniacitywalk2.model.Visit;
import tuc.christos.chaniacitywalk2.utils.Constants;
import tuc.christos.chaniacitywalk2.utils.DataManager;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.utils.JsonHelper;
import tuc.christos.chaniacitywalk2.utils.RestClient;

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
            final View rootView;
            if (index == 2) {
                ArrayList<Scene> items = new ArrayList<>();
                for (Place p : DataManager.getInstance().getActivePlayer().getPlaces().values()) {
                    Scene temp = new Scene(p.getScene_id(), p.getScene_name(), p.getThumb());
                    temp.setUriThumb(Constants.URL_SCENES + "/" + p.getScene_id() + "/" + "images/thumb.jpg");
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
                    temp.setUriThumb(Constants.URL_SCENES + "/" + v.getScene_id() + "/" + "images/thumb.jpg");
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
                final LinearLayout infoForm = (LinearLayout) rootView.findViewById(R.id.info_form);
                final LinearLayout editForm = (LinearLayout) rootView.findViewById(R.id.edit_form);

                AppCompatButton editButton = (AppCompatButton) rootView.findViewById(R.id.edit_prof_but);
                AppCompatButton saveButton = (AppCompatButton) rootView.findViewById(R.id.save_changes_but);
                AppCompatButton cancelButton = (AppCompatButton) rootView.findViewById(R.id.cancel_button);

                final TextView fName = (TextView) rootView.findViewById(R.id.tx1);
                final TextView lName = (TextView) rootView.findViewById(R.id.tx2);
                final TextView created = (TextView) rootView.findViewById(R.id.tx3);
                final TextView recentActivity = (TextView) rootView.findViewById(R.id.tx4);
                final TextView score = (TextView) rootView.findViewById(R.id.tx5);

                final EditText fNameEdit = (EditText) rootView.findViewById(R.id.first_name_edit);
                final EditText lNameEdit = (EditText) rootView.findViewById(R.id.last_name_edit);
                final EditText oldPassword = (EditText) rootView.findViewById(R.id.enter_password);
                final EditText newPassword = (EditText) rootView.findViewById(R.id.new_password);
                final EditText confirmPassword = (EditText) rootView.findViewById(R.id.confirm_new_password);

                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swapForms(infoForm, editForm);
                    }
                });

                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String password = oldPassword.getText().toString();
                        final String newPass = newPassword.getText().toString();
                        String confirmPass = confirmPassword.getText().toString();
                        final String firstName = fNameEdit.getText().toString();
                        final String lastName = lNameEdit.getText().toString();
                        if (password.equals(DataManager.getInstance().getActivePlayer().getPassword())) {
                            if (newPass.equals(confirmPass)) {
                                Player playerToPut = DataManager.getInstance().getActivePlayer();
                                playerToPut.setLastname(lastName);
                                playerToPut.setFirstname(firstName);
                                playerToPut.setNewPassword(newPass);
                                RestClient.getInstance().putPlayerInfo(playerToPut, getActivity().getApplicationContext(), new ContentListener() {
                                    @Override
                                    public void downloadComplete(boolean success, int httpCode, String TAG, String msg) {
                                        if (success) {
                                            try {
                                                JSONObject json = new JSONObject(msg);

                                                Player newPlayer = JsonHelper.parsePlayerFromJson(json);
                                                DataManager.getInstance().insertPlayer(newPlayer);
                                                Player mPlayer = DataManager.getInstance().getActivePlayer();
                                                final String fNameConcut = "First Name: "+ mPlayer.getFirstname();
                                                fName.setText(fNameConcut);
                                                final String lNameConcut = "Last Name: "+mPlayer.getLastname() ;
                                                lName.setText(lNameConcut);
                                                final String createdS = "Member Since: "+mPlayer.getCreated().toString();
                                                created.setText(createdS);
                                                final String activeTime = "Last Activity " + computeTimeDiff(mPlayer.getRecentActivity()) + " ago";
                                                recentActivity.setText(activeTime);
                                                final String scoreTx = "Score: "+String.valueOf(mPlayer.getScore());
                                                score.setText(scoreTx);
                                                swapForms(editForm, infoForm);
                                            } catch (JSONException ex) {
                                                Log.i("JSON", ex.getMessage());
                                            }
                                        }else{
                                            Toast.makeText(getActivity().getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                                            swapForms(editForm, infoForm);
                                        }
                                    }
                                });
                            } else {
                                newPassword.setError("Passwords don't Match");
                                newPassword.requestFocus();
                            }
                        } else {
                            oldPassword.setError("Wrong Password");
                            oldPassword.requestFocus();
                        }
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        swapForms(editForm,infoForm);
                    }
                });

                Player mPlayer = DataManager.getInstance().getActivePlayer();
                final String fNameConcut = "First Name: "+ mPlayer.getFirstname();
                fName.setText(fNameConcut);
                final String lNameConcut = "Last Name: "+mPlayer.getLastname() ;
                lName.setText(lNameConcut);
                final String createdS = "Member Since: "+mPlayer.getCreated().toString();
                created.setText(createdS);
                final String activeTime = "Last Activity " + computeTimeDiff(mPlayer.getRecentActivity()) + " ago";
                recentActivity.setText(activeTime);
                final String scoreTx = "Score: "+String.valueOf(mPlayer.getScore());
                score.setText(scoreTx);
                fNameEdit.setText(mPlayer.getFirstname());
                lNameEdit.setText(mPlayer.getLastname());

            } else {
                rootView = inflater.inflate(R.layout.progress_details_test, container, false);
                Level level = DataManager.getInstance().getCurrentLevel();
                if(level != null) {
                    ((TextView) rootView.findViewById(R.id.country)).setText(level.getCountry() + " (" + level.getCountry_code() + ")");
                    ((TextView) rootView.findViewById(R.id.admin_area)).setText(level.getAdminArea());
                    ((TextView) rootView.findViewById(R.id.locality)).setText("Currently playing at " + level.getCity() + "");

                    RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.progress_container);
                    if (!DataManager.getInstance().getPeriods().isEmpty())
                        recyclerView.setAdapter(new SimpleProgressRecyclerViewAdapter(new ArrayList<>(DataManager.getInstance().getPeriods())));
                    else
                        ((TextView) rootView.findViewById(R.id.locality)).setText("No Progress for " + level.getCity() + "");
                }else {
                    ((TextView) rootView.findViewById(R.id.country)).setText("No Content found for your area!");
                    ((TextView) rootView.findViewById(R.id.admin_area)).setText("");
                    ((TextView) rootView.findViewById(R.id.locality)).setText("");
                }
            }

            return rootView;
        }

        public void swapForms(final View from, final View to) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            from.setVisibility(View.GONE);
            from.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    from.setVisibility(View.GONE);
                }
            });

            to.setVisibility(View.VISIBLE);
            to.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    to.setVisibility(View.VISIBLE);
                }
            });

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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_list_content, parent, false);
                return new SimpleItemRecyclerViewAdapter.ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final SimpleItemRecyclerViewAdapter.ViewHolder holder, int position) {
                if (mVisits != null) {
                    holder.created.setVisibility(View.VISIBLE);
                    final Scene item = mVisits.get(position);
                    holder.mView.setText(item.getName());
                    Glide.with(MyApp.getAppContext())
                            .load(item.getUriThumb())
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .placeholder(R.drawable.empty_photo)
                            .into(holder.imageView);
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

                    Glide.with(MyApp.getAppContext())
                            .load(item.getUriThumb())
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .placeholder(R.drawable.empty_photo)
                            .into(holder.imageView);

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
                final TextView created;
                final CardView cardView;
                final ImageView imageView;

                ViewHolder(View view) {
                    super(view);
                    mView = (TextView) view.findViewById(R.id.content);
                    region = (TextView) view.findViewById(R.id.region);
                    created = (TextView) view.findViewById(R.id.created);
                    cardView = (CardView) view.findViewById(R.id.card_view);
                    imageView = (ImageView) view.findViewById(R.id.img);
                }

                @Override
                public String toString() {
                    return super.toString();// + " '" + mContentView.getText() + "'";
                }
            }
        }


    }

    static class SimpleProgressRecyclerViewAdapter extends RecyclerView.Adapter<SimpleProgressRecyclerViewAdapter.ViewHolder> {

        private final ArrayList<Period> periods;

        SimpleProgressRecyclerViewAdapter(ArrayList<Period> items) {
            periods = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_details_row, parent, false);
            return new SimpleProgressRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final SimpleProgressRecyclerViewAdapter.ViewHolder holder, int position) {
            if (periods.isEmpty()) {
                return;
            }
            holder.title.setText(periods.get(position).getName());

            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Bitmap temp;
                    try {
                        temp = Glide.with(MyApp.getAppContext())
                                .load(periods.get(holder.getAdapterPosition()).getUriLogo())
                                .asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                .into(100, 100)
                                .get();
                    } catch (ExecutionException | InterruptedException e) {
                        Log.i("bitmap", e.getMessage());
                        temp = BitmapFactory.decodeResource(MyApp.getAppContext().getResources(), R.drawable.period_bg_1);
                    }

                    setImageDrawable(temp, holder.logo);
                    return null;
                }
            };
            task.execute();
            int prog = 0;
            List<Scene> scenes = new ArrayList<>(periods.get(position).getScenes().values());
            float control = 0;
            for (Scene temp : scenes) {
                if (mPlayer.hasVisited(temp.getId())) {
                    control++;
                    prog = Math.round(control / scenes.size() * 100);
                    holder.ratio.setText((int) control + "/" + scenes.size());
                }
            }

            if (prog == 100) {
                holder.progressBar.setVisibility(View.GONE);
                holder.completed.setVisibility(View.VISIBLE);
            } else {
                holder.progressBar.setMax(100);
                holder.progressBar.setProgress(prog);
            }
        }

        @Override
        public int getItemCount() {
            return periods.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView ratio;
            final ProgressBar progressBar;
            final ImageView logo;
            final FrameLayout completed;

            ViewHolder(View view) {
                super(view);
                title = (TextView) view.findViewById(R.id.title);
                ratio = (TextView) view.findViewById(R.id.ratio);
                progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
                logo = (ImageView) view.findViewById(R.id.logo);
                completed = (FrameLayout) view.findViewById(R.id.completed_container);
            }

            @Override
            public String toString() {
                return super.toString();// + " '" + mContentView.getText() + "'";

            }

        }
    }

    private static void setImageDrawable(final Bitmap bitmap, final ImageView imgView) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                RoundedBitmapDrawable bm = RoundedBitmapDrawableFactory.create(MyApp.getAppContext().getResources(), bitmap);
                bm.setCircular(true);
                imgView.setImageDrawable(bm);
            }
        });
    }

    static String computeTimeDiff(java.util.Date created) {
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
