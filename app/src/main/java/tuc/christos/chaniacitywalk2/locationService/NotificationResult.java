package tuc.christos.chaniacitywalk2.locationService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import tuc.christos.chaniacitywalk2.R;
import tuc.christos.chaniacitywalk2.SettingsActivity;
import tuc.christos.chaniacitywalk2.utils.Constants;


public class NotificationResult extends AppCompatActivity {

    public static final String pref_key_location_update_interval_dialog = "pref_key_location_update_interval_dialog";

    public static final String pref_key_allow_background_locations_dialog = "pref_key_allow_background_locations_dialog";

    Button toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_result);
        toggle = (Button) findViewById(R.id.toggle);
        Intent intent = getIntent();
        String action = intent.getAction();

        TextView title = (TextView) findViewById(R.id.title);
        final String teet = "Chania AR Locations";
        title.setText(teet);
        Log.i("ACTION",action);
        switch (action){
            case Constants.ACTION_STOP:
                toggle.setText(getResources().getString(R.string.notification_toggle_pause));
                toggle.setOnClickListener(startClickListener);
                break;

            case Constants.ACTION_START:
                toggle.setText(getResources().getString(R.string.notification_toggle_start));
                toggle.setOnClickListener(stopClickListener);
                break;

        }

        Button settings = (Button) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
            }
        });
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.preference, new DialogFragment())
                .commit();

        ImageButton close_x = (ImageButton) findViewById(R.id.close_x);
        close_x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent service = new Intent(getApplicationContext(),LocationService.class);
                final NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                service.putExtra("toggle","stop");
                startService(service);
                mNotificationManager.cancelAll();

                toggle.setText(getResources().getString(R.string.notification_toggle_start));
                toggle.setOnClickListener(stopClickListener);
                NotificationResult.this.finish();
            }
        });
    }


    private final View.OnClickListener startClickListener  = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Intent service = new Intent(getApplicationContext(),LocationService.class);
            final NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            service.putExtra("toggle","stop");
            startService(service);
            // update notification to start service next time it is pressed
            Intent dialogIntent = new Intent(getApplicationContext(), NotificationResult.class);
            dialogIntent.setAction(Constants.ACTION_START);
            PendingIntent piStop = PendingIntent.getActivity(getApplicationContext(), 0, dialogIntent, 0);
            mNotificationManager.notify(Constants.PERMA_NOTIFICATION_ID
                    ,new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.angry_thor_024px)
                            .setContentTitle("Chania AR Location")
                            .setContentText("Paused! Tap to Resume...")
                            .setOngoing(true)
                            .setContentIntent(piStop)
                            .build());

            toggle.setText(getResources().getString(R.string.notification_toggle_start));
            toggle.setOnClickListener(stopClickListener);
        }
    };

    private final View.OnClickListener stopClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Intent service = new Intent(getApplicationContext(),LocationService.class);
            final NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            service.putExtra("toggle","start");
            startService(service);

            Intent dialogIntent = new Intent(getApplicationContext(), NotificationResult.class);
            dialogIntent.setAction(Constants.ACTION_STOP);
            PendingIntent piStop = PendingIntent.getActivity(getApplicationContext(), 0, dialogIntent, 0);
            mNotificationManager.notify(Constants.PERMA_NOTIFICATION_ID,
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.angry_thor_024px)
                            .setContentTitle("Chania AR Location")
                            .setContentText("Running! Tap to Pause...")
                            .setOngoing(true)
                            .setContentIntent(piStop)
                            .build());
            toggle.setText(getResources().getString(R.string.notification_toggle_pause));
            toggle.setOnClickListener(startClickListener);
        }
    };




    public static class DialogFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.dialog_preferences);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            Preference preference1 = findPreference(pref_key_location_update_interval_dialog);
            preference1.setSummary(sharedPreferences.getString(pref_key_location_update_interval_dialog,""));
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view,savedInstanceState);
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
            if (key.equals(pref_key_location_update_interval_dialog)) {
                Preference connectionPref = findPreference(key);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));

                Intent intent = new Intent(getActivity(),LocationService.class);
                intent.putExtra("mode", sharedPreferences.getString(key,""));
                Log.i("LocationMode",sharedPreferences.getString(key,""));
                if(!LocationService.isServiceRunning()) {
                    intent.putExtra("toggle", "stop");
                }
                getActivity().startService(intent);
            }
            if (key.equals(pref_key_allow_background_locations_dialog)){
                if(sharedPreferences.getBoolean(key,false)) {
                    getActivity().startService(new Intent(getActivity(), LocationService.class));

                }else {
                    getActivity().stopService(new Intent(getActivity(), LocationService.class));

                    NotificationManager mNotificationManager =
                            (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(Constants.PERMA_NOTIFICATION_ID);
                }
            }
            if (key.equals(SettingsActivity.pref_key_google_api_client)) {
                // Set summary to be the user-description for the selected value
                Intent intent = new Intent(getActivity(),LocationService.class);
                intent.putExtra("swap", sharedPreferences.getBoolean(key,false));
                if(!LocationService.isServiceRunning()) {
                    intent.putExtra("toggle", "stop");
                }
                getActivity().startService(intent);
            }
        }

    }
}
