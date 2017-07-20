package tuc.christos.chaniacitywalk2.testSensorService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import tuc.christos.chaniacitywalk2.MyApp;
import tuc.christos.chaniacitywalk2.R;
import tuc.christos.chaniacitywalk2.testSensorService.myOpenGlStarterPack.MyGLRenderer;


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


    private MyGLSurfaceView mGLView = new MyGLSurfaceView(MyApp.getAppContext());


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mGLView);
        startService(new Intent(this,SensorService.class));

    }

    @Override
    public void onResume() {
        super.onResume();
        if(!mBount)
            bindService(new Intent(this, SensorService.class), mConnection, Context.BIND_NOT_FOREGROUND);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mBount) {
            unbindService(mConnection);
            mBount = false;
        }
    }


    @Override
    public void updateData(double bearing, double theta, double distance) {
        Log.i("SensorService", "received...");

    }

    private class MyGLSurfaceView extends GLSurfaceView {

        private final MyGLRenderer mRenderer;

        public MyGLSurfaceView(Context context){
            super(context);
            // Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);
            mRenderer = new MyGLRenderer();
            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(mRenderer);
        }
    }
}
