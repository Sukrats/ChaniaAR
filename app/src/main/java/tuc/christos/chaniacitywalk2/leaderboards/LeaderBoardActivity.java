package tuc.christos.chaniacitywalk2.leaderboards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tuc.christos.chaniacitywalk2.MyApp;
import tuc.christos.chaniacitywalk2.R;
import tuc.christos.chaniacitywalk2.mInterfaces.ContentListener;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.utils.DataManager;
import tuc.christos.chaniacitywalk2.utils.JsonHelper;
import tuc.christos.chaniacitywalk2.utils.RestClient;

public class LeaderBoardActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private RestClient mRestClient = RestClient.getInstance();

    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<Scene> scenes = new ArrayList<>();

    private static int colorCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        colorCode = ContextCompat.getColor(this, R.color.colorAccent);
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lead_list);
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    mTextMessage.setText(R.string.title_leaderboard);
                    if (players.isEmpty()) {
                        swapViews(findViewById(R.id.content), findViewById(R.id.login_progress));
                        mRestClient.downloadPlayers(new ContentListener() {
                            @Override
                            public void downloadComplete(boolean success, int httpCode, String TAG, String msg) {
                                if (success) {
                                    swapViews(findViewById(R.id.login_progress), findViewById(R.id.content));
                                    players = sortPlayers(JsonHelper.parsePlayersFromJson(msg));
                                    recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(players, 1));
                                } else {
                                    mTextMessage.setText(msg);
                                }
                            }
                        });
                    } else
                        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(players, 1));

                    return true;
                case R.id.navigation_dashboard:

                    mTextMessage.setText(R.string.title_landmarks);
                    if (scenes.isEmpty()) {
                        swapViews(findViewById(R.id.content), findViewById(R.id.login_progress));
                        mRestClient.downloadScenesView(new ContentListener() {
                            @Override
                            public void downloadComplete(boolean success, int httpCode, String TAG, String msg) {
                                if (success) {
                                    swapViews(findViewById(R.id.login_progress), findViewById(R.id.content));
                                    scenes = sortScenes(JsonHelper.parseScenesFromJson(msg));
                                    recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(scenes));
                                } else {
                                    mTextMessage.setText(msg);
                                }
                            }
                        });
                    } else
                        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(scenes));
                    return true;
            }
            return false;
        }

    };

    private ArrayList<Scene> sortScenes(ArrayList<Scene> tmp) {
        ArrayList<Scene> temps = new ArrayList<>();
        for (Scene temp : tmp) {
            if (temp.getNumOfVisits() > 0)
                temps.add(temp);
        }
        Collections.sort(temps, new Comparator<Scene>() {
            @Override
            public int compare(Scene lhs, Scene rhs) {
                int cur = lhs.getNumOfVisits();
                int next = rhs.getNumOfVisits();
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return cur > next ? -1 : (cur < next) ? 1 : 0;
            }
        });
        return temps;
    }

    private ArrayList<Player> sortPlayers(ArrayList<Player> tmp) {
        Collections.sort(tmp, new Comparator<Player>() {
            @Override
            public int compare(Player lhs, Player rhs) {
                long cur = lhs.getScore();
                long next = rhs.getScore();
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return cur > next ? -1 : (cur < next) ? 1 : 0;
            }
        });
        return tmp;
    }

    private void swapViews(final View from, final View to) {
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

        private final List<Scene> mScenes;
        private final List<Player> mPlayers;

        SimpleItemRecyclerViewAdapter(List<Scene> items) {
            mScenes = items;
            mPlayers = null;
        }

        SimpleItemRecyclerViewAdapter(List<Player> items, int dummy) {
            mScenes = null;
            mPlayers = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.leaderboards_item, parent, false);
                return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            DataManager mDataManager = DataManager.getInstance();
            if (mPlayers == null) {
                holder.logo.setVisibility(View.VISIBLE);
                holder.mIdView.setVisibility(View.GONE);
                final Scene item = mScenes.get(position);
                Glide.with(MyApp.getAppContext()).load(item.getUriThumb())
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .placeholder(R.drawable.empty_photo)
                        .into(holder.logo);

                holder.name.setText(item.getName());
                String num = "Visited: "+item.getNumOfVisits()+" times!";
                holder.num.setText(num);
                return;

            }
            holder.logo.setVisibility(View.GONE);
            final Player item = mPlayers.get(position);
            if(mDataManager.getActivePlayer().getUsername().equals(item.getUsername()))
                holder.cardView.setCardBackgroundColor(colorCode);
            holder.mIdView.setText(String.valueOf(position+1));
            holder.name.setText(item.getUsername());
            String num = "Score: "+item.getScore();
            holder.num.setText(num);
        }

        @Override
        public int getItemCount() {
            if (mPlayers == null)
                return mScenes.size();
            else
                return mPlayers.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView num;
            final ImageView logo;
            final TextView mIdView;
            final CardView cardView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.position);
                logo = (ImageView) view.findViewById(R.id.img);
                num = (TextView) view.findViewById(R.id.num);
                name = (TextView) view.findViewById(R.id.content);
                cardView = (CardView) view.findViewById(R.id.card_view);
            }

            @Override
            public String toString() {
                return super.toString();// + " '" + mContentView.getText() + "'";
            }
        }

    }


}
