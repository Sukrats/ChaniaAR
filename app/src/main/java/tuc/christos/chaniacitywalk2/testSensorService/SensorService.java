package tuc.christos.chaniacitywalk2.testSensorService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;


public class SensorService extends Service implements SensorDataListener {

    mIBinder mBinder = new mIBinder();
    boolean isRunning = false;

    private Looper mThreadLooper;
    private ServiceHandler mServiceHandler;
    private PointerLocationProvider sensorDataProvider;

    private SensorServiceListener listener;

    @Override
    public void injectSensorData(double direction, double angle, double distance) {
        Log.i("SensorService","injected...");
        if(this.listener != null)
            this.listener.updateData(direction,angle,distance);
    }

    public class mIBinder extends Binder {
        public SensorService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SensorService.this;
        }

    }

    public void registerServiceListener(SensorServiceListener listener) {
        Log.i("SensorService","Registered Service Listener");
        this.listener=(listener);
    }

    public void removeListener() {
        listener = null;
    }

    @Override
    public void onCreate() {
        Log.i("SensorService","Service onCreate Called");
        if (!isRunning) {
            HandlerThread thread = new HandlerThread("SensorProviderThread",
                    Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            mThreadLooper = thread.getLooper();
            mServiceHandler = new ServiceHandler(mThreadLooper);
            isRunning = true;
        }
    }


    @Override
    public void onDestroy() {
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SensorService","Service startCommand Called");
        if (!isRunning) {
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            mServiceHandler.sendMessage(msg);
            isRunning = true;
        }

        return START_STICKY;
    }


    private final class ServiceHandler extends Handler {

        ServiceHandler(Looper looper) {
            super(looper);
            Log.i("SensorService","Provider Created");
            sensorDataProvider = new PointerLocationProvider(SensorService.this, (WindowManager) getSystemService(Context.WINDOW_SERVICE), SensorService.this);
        }

        @Override
        public void handleMessage(Message msg) {
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("SensorService","Service onBind Called");
        //Toast.makeText(this, "onBind Called", Toast.LENGTH_SHORT).show();
        sensorDataProvider.onStart();
        return mBinder;
    }

    /**
     * Called when all clients have unbound with unbindService()
     */
    @Override
    public boolean onUnbind(Intent intent) {
        //Toast.makeText(this, "onUnbind Called", Toast.LENGTH_SHORT).show();
        Log.i("SensorService","Service onUnbind Called");
        sensorDataProvider.onPause();
        return true;
    }

    /**
     * Called when a client is binding to the service with bindService()
     */
    @Override
    public void onRebind(Intent intent) {
        //Toast.makeText(this, "onRebind Called", Toast.LENGTH_SHORT).show();
        Log.i("SensorService","Service onRebind Called");
        sensorDataProvider.onStart();
    }


}
