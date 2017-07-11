package tuc.christos.chaniacitywalk2.testSensorService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import tuc.christos.chaniacitywalk2.R;


public class SensorCheckActivity extends AppCompatActivity implements SensorServiceListener {

    private ImageView thor;
    private FrameLayout odin;
    private TextView bearingTx;
    private TextView thetaTx;
    private TextView distanceTx;

    private SensorService.mIBinder mBinder;
    private boolean mBount = false;
    private SensorService mService;
    final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorService.mIBinder binder = (SensorService.mIBinder) service;
            mBinder = binder;
            mService = binder.getService();
            mService.registerServiceListener(SensorCheckActivity.this);
            Log.i("SensorService", "Bount");
            mBount = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService.removeListener();
            mBount = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_check);
        thor = (ImageView) findViewById(R.id.thor);
        odin = (FrameLayout) findViewById(R.id.odin);
        bearingTx = (TextView) findViewById(R.id.bearing);
        thetaTx = (TextView) findViewById(R.id.theta);
        distanceTx = (TextView) findViewById(R.id.distance);
        startService(new Intent(this,SensorService.class));
    }

    @Override
    public void onResume() {
        bindService(new Intent(this, SensorService.class), mConnection, Context.BIND_NOT_FOREGROUND);
        super.onResume();
    }

    @Override
    public void onPause() {
        Toast.makeText(this,"Bount?:"+mBount,Toast.LENGTH_SHORT).show();
        unbindService(mConnection);
        mBount = false;
        super.onPause();
    }


    @Override
    public void updateData(double bearing, double theta, double distance) {
        if (thor == null || odin == null) {
            return;
        }
        Log.i("SensorService", "received...");
        thetaTx.setText("Ground Angle: "+theta+"o");
        bearingTx.setText("Bearing: "+bearing+"o");
        distanceTx.setText("Distance: "+distance+"m");

        int pLeft = 0;
        int pRight = 0;
        int pTop = 0;
        int pBottom = 0;

        int radius = (int) distance;

        int posX = radius * (int) Math.cos(bearing);
        int posY = radius * (int) Math.sin(bearing);

        if (posX < 0) {
            pRight = posX;
        } else {
            pLeft = posX;
        }

        if (posY < 0) {
            pTop = posY;
        } else {
            pBottom = posY;
        }

        thor.setPadding(pLeft * 1000, pTop * 1000, pRight * 1000, pBottom * 1000);
    }
}
