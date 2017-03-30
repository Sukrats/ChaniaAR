package tuc.christos.chaniacitywalk2;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    public static final String pref_key_camera_follow = "pref_key_camera_follow";

    public static final String pref_key_map_type = "pref_key_map_type";

    public static final String pref_key_location_update_interval = "pref_key_location_update_interval";

    public static final String pref_key_user_draw_radius = "pref_key_user_draw_radius";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
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
        }
    }
}
