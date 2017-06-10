package tuc.christos.chaniacitywalk2;

import android.content.Intent;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import tuc.christos.chaniacitywalk2.data.DataManager;
import tuc.christos.chaniacitywalk2.data.RestClient;
import tuc.christos.chaniacitywalk2.mInterfaces.ContentListener;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {

    DataManager mDataManager;
    RestClient mRestClient;

    ContentLoadingProgressBar progressBar;
    TextView progressMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mDataManager = DataManager.getInstance();
        mDataManager.init(this);
        mRestClient = RestClient.getInstance();

        final String sync = getIntent().getStringExtra("sync_key");
        long Overall = 400;
        if (!mDataManager.isInitialised() && sync != null) {
            Overall = 400;
        } else
            Overall = 200;


        final long max = Overall;
        progressBar = (ContentLoadingProgressBar) findViewById(R.id.progress);
        progressBar.setMax(100);
        progressMsg = (TextView) findViewById(R.id.progress_msg);

        if (!mDataManager.isInitialised()) {
            progressMsg.setText("Downloading Periods...");
            progressBar.setProgress(0);
            mRestClient.downloadPeriods(new ContentListener() {
                @Override
                public void downloadComplete(boolean success, int httpCode, String TAG, String msg) {
                    switch (TAG) {
                        case "Periods":
                            mRestClient.downloadScenes(this);
                            progressBar.setProgress(25);
                            progressMsg.setText("Downloading Scenes...");
                            break;

                        case "Scenes":
                            Log.i("SYNC", sync);
                            if (sync != null && sync.equals("local")) {
                                mRestClient.downloadData(mDataManager.getActivePlayer().getLinks().get("visits"), this, "Visits");
                                progressBar.setProgress(50);
                                progressMsg.setText("Downloading Visits...");
                                //Toast.makeText(getApplicationContext(),"TODO: Download player data", Toast.LENGTH_LONG).show();
                                //startMapsActivity();
                            } else if (sync != null && sync.equals("remote")) {
                                Toast.makeText(getApplicationContext(), "TODO: Upload to remote DB", Toast.LENGTH_LONG).show();
                                startMapsActivity();
                            } else {
                                progressBar.setProgress(100);
                                progressMsg.setText("Downloade Complete...");
                                startMapsActivity();
                            }
                            break;

                        case "Visits":
                            if (success) {
                                mRestClient.downloadData(mDataManager.getActivePlayer().getLinks().get("places"), this, "Places");
                                progressBar.setProgress(75);
                                progressMsg.setText("Downloading Places...");
                                break;
                            } else {
                                mRestClient.downloadData(mDataManager.getActivePlayer().getLinks().get("places"), this, "Places");
                                progressBar.setProgress(75);
                                progressMsg.setText("Downloading Places...");
                            }
                        case "Places":
                            progressBar.setProgress(100);
                            progressMsg.setText("Downloade Complete!");
                            startMapsActivity();
                            break;
                        case "Player":
                            startMapsActivity();
                            break;

                    }
                }
            });

/*
            mRestClient.getInitialContent( new ClientListener() {
                @Override
                public void onCompleted(boolean success, int httpCode, String msg) {
                    if(success) {
                        progressBar.setProgress(100);
                        progressMsg.setText("Download Complete");
                    }
                    else {
                        progressBar.setProgress(0);
                        progressMsg.setText(msg + " Error num:" + httpCode);
                    }
                }

                @Override
                public void onUpdate(int progress, String msg) {
                    progressMsg.setText("Downloading..."+progress+"%");
                    progressBar.setProgress(progress);
                }
            });*/
        } else {
            startMapsActivity();
        }
    }

    void startMapsActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

}
