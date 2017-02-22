package com.fitrax.koenfitraxv001;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public TextView textViewStepsCount;
    public TextView textViewSensorType;

    public int value;
    public long time;

    private SensorManager mSensorManager;

    private Sensor mStepCounterSensor;

    private Sensor mStepDetectorSensor;

    public Handler handler = new Handler();

    public PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textViewStepsCount = (TextView) findViewById(R.id.stepsCount);
        textViewSensorType = (TextView) findViewById(R.id.sensorType);

        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // saves output (long) in milliseconds
        Context ctx = getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        preferences.getLong("time", time);
        textViewSensorType.setText("time since reboot: " + time + "");
    }

    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        float[] values = event.values;
        value = -1;

        if (values.length > 0) {
            value = (int) values[0];
        }

        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            textViewStepsCount.setText("" + value + "");
            /*
        } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // For test only. Only allowed value is 1.0 i.e. for step taken
            textViewSensorType.setText("Sensor type : " + value + "");
        */
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onResume() {

        super.onResume();

        mSensorManager.registerListener(this, mStepCounterSensor,

                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mStepDetectorSensor,

                SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this, mStepCounterSensor);
        mSensorManager.unregisterListener(this, mStepDetectorSensor);
    }

    public void checkTimeOfDay(){

        wakeLock.acquire();



        wakeLock.release();
    }

    private Runnable runnableTime = new Runnable() {
        @Override
        public void run() {
            checkTimeOfDay();
            startTimeCheckLoop();
        }
    };

    public void startTimeCheckLoop(){
        handler.postDelayed(runnableTime, 1000 * 60 * 30);
    }

}
