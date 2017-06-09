package tuc.christos.chaniacitywalk2;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import tuc.christos.chaniacitywalk2.locationService.LocationService;
import tuc.christos.chaniacitywalk2.utils.Constants;

public class SettingsActivity extends AppCompatActivity {

    public static final String pref_key_camera_follow = "pref_key_camera_follow";

    public static final String pref_key_map_type = "pref_key_map_type";

    public static final String pref_key_auto_sign_in = "pref_key_auto_sign_in";

    public static final String pref_key_location_update_interval = "pref_key_location_update_interval";

    public static final String pref_key_allow_background_locations = "pref_key_allow_background_locations";

    public static final String pref_key_user_draw_radius = "pref_key_user_draw_radius";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_settings_white);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            Preference preference = findPreference(pref_key_map_type);
            preference.setSummary(sharedPreferences.getString(pref_key_map_type,""));

            Preference preference1 = findPreference(pref_key_location_update_interval);
            preference1.setSummary(sharedPreferences.getString(pref_key_location_update_interval,""));
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view,savedInstanceState);
            setDividerHeight(0);
        }

            @Override
        public void onCreatePreferences(Bundle bundle, String key){
        }

        @Override
        public void onResume(){
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause(){
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences  sharedPreferences, String key ){
            if (key.equals(pref_key_map_type)) {
                Preference connectionPref = findPreference(key);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
            }

            if (key.equals(pref_key_location_update_interval)) {
                Preference connectionPref = findPreference(key);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
            }

            if (key.equals(pref_key_allow_background_locations)){
                if(sharedPreferences.getBoolean(key,false)) {
                    getActivity().startService(new Intent(getActivity(), LocationService.class));

                }else {
                    getActivity().stopService(new Intent(getActivity(), LocationService.class));

                    NotificationManager mNotificationManager =
                            (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(Constants.PERMA_NOTIFICATION_ID);
                }
            }
        }
    }
}
