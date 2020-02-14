package tuc.christos.chaniacitywalk2.testSensorService;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Christos on 11-Jul-17.
 * OMG OMG OMG OMG
 */

class PointerLocationProvider implements SensorEventListener {
    private SensorDataListener listener;
    private SensorManager mSensorManager;

    private float[] accel = new float[3];
    private float[] gravity = new float[3];
    private float[] magnetic = new float[3];
    private float[] rotation = new float[3];
    private float[] orientation = new float[3];
    private float[] rotationOrientation = new float[3];

    private double deviceHeight = 1.5;
    private long lastUpdate = System.currentTimeMillis();
    private boolean hasInitialOrientation = false;
    private WindowManager mWindowManager;


    PointerLocationProvider(SensorDataListener listener, WindowManager wManager, Context context) {
        Log.i("SensorService","Created Provider");
        mWindowManager = wManager;
        this.listener = listener;
        this.mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        accel[0] = 0;
        accel[1] = 0;
        accel[2] = 0;
        gravity[0] = 0;
        gravity[1] = 0;
        gravity[2] = 0;
        magnetic[0] = 0;
        magnetic[1] = 0;
        magnetic[2] = 0;
    }

    void onStart() {
        Log.i("SensorService","Started Provider");
        this.mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_FASTEST);
        this.mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
        this.mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    void onPause() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float FILTER_COEFFICIENT = 0.8f;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accel[0] = ( accel[0]*FILTER_COEFFICIENT + event.values[0]*(1-FILTER_COEFFICIENT));
                accel[1] = ( accel[1]*FILTER_COEFFICIENT + event.values[1]*(1-FILTER_COEFFICIENT));
                accel[2] = ( accel[2]*FILTER_COEFFICIENT + event.values[2]*(1-FILTER_COEFFICIENT));
                // copy new accelerometer data into accel array
                System.arraycopy(event.values, 0, accel, 0, 3);
                break;

            case Sensor.TYPE_GRAVITY:
                gravity[0] = ( gravity[0]*FILTER_COEFFICIENT + event.values[0]*(1-FILTER_COEFFICIENT));
                gravity[1] = ( gravity[1]*FILTER_COEFFICIENT + event.values[1]*(1-FILTER_COEFFICIENT));
                gravity[2] = ( gravity[2]*FILTER_COEFFICIENT + event.values[2]*(1-FILTER_COEFFICIENT));
                // copy new accelerometer data into accel array and calculate orientation
                System.arraycopy(event.values, 0, gravity, 0, 3);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                float[] rotationMatrix = new float[9];
                System.arraycopy(event.values, 0, rotation, 0, 3);

                SensorManager.getRotationMatrixFromVector(rotationMatrix,event.values);

                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X,
                        SensorManager.AXIS_Z, rotationMatrix);

                SensorManager.getOrientation(rotationMatrix,rotationOrientation);
                calculatePosition();
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetic[0] = ( magnetic[0]*FILTER_COEFFICIENT + event.values[0]*(1-FILTER_COEFFICIENT));
                magnetic[1] = ( magnetic[1]*FILTER_COEFFICIENT + event.values[1]*(1-FILTER_COEFFICIENT));
                magnetic[2] = ( magnetic[2]*FILTER_COEFFICIENT + event.values[2]*(1-FILTER_COEFFICIENT));
                // copy new accelerometer data into accel array and calculate orientation
                //System.arraycopy(event.values, 0, magnetic, 0, 3);
                break;

        }

    }

    private void calculatePosition(){

        if (magnetic != null && gravity != null && rotation != null) {
            float rotationMatrix[] = new float[9];
            float I[] = new float[9];
            if (SensorManager.getRotationMatrix(rotationMatrix, I, gravity, magnetic)) {
                SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X,
                        SensorManager.AXIS_Z, rotationMatrix);
                SensorManager.getOrientation(rotationMatrix, orientation);
                hasInitialOrientation = true;
            }
        }

        if (hasInitialOrientation && gravity != null) {
            //compute gravity vector magnitude
            double gravityMagnitude = Math.sqrt(gravity[0]*gravity[0] + gravity[1]*gravity[1] + gravity[2]*gravity[2]);

            //Using Gravity
            //double alphaCos = Math.abs();
            //angle between the gravity vector and the devices x-y plane ( pitch and roll ) in radians
            double alphaAngle = Math.acos(gravity[2]/gravityMagnitude);
            //distancce between the users position and the location the camera is Pointing at
            double crosshairMyWay = Math.tan(alphaAngle)*deviceHeight;

            //Using Rotation Vector NON VIABLE LIKE DIS
            //we need the thetaAngles complement in radians
            double complement = rotationOrientation[1];
            //distancce between the users position and the location the camera is Pointing at
            double crosshair = Math.tan(complement)*deviceHeight;


            //angle between the z-axis(the one that points at the desired location) and the magnetic north
            double bearing = Math.toDegrees(rotationOrientation[0]);
            if( bearing < 0 )
                bearing = (360 + bearing);

            if(System.currentTimeMillis() - lastUpdate >= 100){
                Log.i("SensorService","Provider calculated");
                listener.injectSensorData(bearing, complement, crosshairMyWay);
                lastUpdate = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void registerSensorListener(SensorDataListener listener) {
        this.listener = listener;
    }

    public void removeSensorListener() {
        this.listener = null;
    }

}
