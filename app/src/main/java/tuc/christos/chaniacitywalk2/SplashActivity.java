package tuc.christos.chaniacitywalk2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.AppCompatTextView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import tuc.christos.chaniacitywalk2.data.DataManager;
import tuc.christos.chaniacitywalk2.data.RestClient;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {

    DataManager mDataManager ;
    RestClient mRestClient;

    ContentLoadingProgressBar progressBar;
    TextView progressMsg ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mDataManager = DataManager.getInstance();
        mDataManager.init(this);
        mRestClient  = RestClient.getInstance();

        final String sync = getIntent().getStringExtra("sync_key");
        long Overall = 400;
        if(!mDataManager.isInitialised() && sync != null){
            Overall = 400;
        }else
            Overall = 200;


        final long max = Overall;
        progressBar = (ContentLoadingProgressBar) findViewById(R.id.progress);
        progressMsg = (TextView) findViewById(R.id.progress_msg);

        if(!mDataManager.isInitialised()){
            mRestClient.downloadPeriods(new ContentListener() {
                @Override
                public void downloadComplete(boolean success, int httpCode, String TAG, String msg) {
                    if(success){
                        switch (TAG){
                            case "Periods": mRestClient.downloadScenes(this); break;

                            case "Scenes":
                                if(sync!=null && sync.equals("local")){
                                    //mRestClient.getPlayerData(this); break;
                                    Toast.makeText(getApplicationContext(),"TODO: Download player data", Toast.LENGTH_LONG).show();
                                    startMapsActivity();
                                }else if(sync!=null && sync.equals("remote")){
                                    Toast.makeText(getApplicationContext(),"TODO: Upload to remote DB", Toast.LENGTH_LONG).show();
                                    startMapsActivity();
                                }else
                                    startMapsActivity();
                                break;
                            case "Player":
                                    startMapsActivity();
                                break;

                        }
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
        }else {
            startMapsActivity();
        }
    }

    void startMapsActivity(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

}
