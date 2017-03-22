package com.example.nick.fitraxdemo1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public TextView textViewStepsCount;
    public TextView textViewSensorType;
    public TextView speedTv;
    public TextView stepCounterTv;
    public TextView caloriesTv;
    public TextView heartRateNowTv;
    public TextView HeartRateAverageTv;

    public ShareButton fbShareBtn;
    public Button newWorkoutBtn;
    public Button OpenGraphBtn;

    public int steps;
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

        //initialize facebook sdk
        FacebookSdk.sdkInitialize(getApplicationContext());

        textViewSensorType = (TextView) findViewById(R.id.sensorType);

        speedTv = (TextView) findViewById(R.id.speedTv);
        stepCounterTv = (TextView) findViewById(R.id.stepCounterTv);
        caloriesTv = (TextView) findViewById(R.id.caloriesTv);
        heartRateNowTv = (TextView) findViewById(R.id.heartRateNowTv);
        HeartRateAverageTv = (TextView) findViewById(R.id.heartRateAverageTv);

        fbShareBtn = (ShareButton) findViewById(R.id.shareButton);
        newWorkoutBtn = (Button) findViewById(R.id.newWorkoutButton);
        OpenGraphBtn = (Button) findViewById(R.id.openGraphButton);

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
        //textViewSensorType.setText("time since reboot: " + time + "");

        //calculate calories
        CalculateBurnedCalories();



        //Share
        ShareWorkoutOnFb();

        //open MapsActivity
        newWorkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                //i.putExtra("playerPos", position);
                startActivity(i);
            }
        });

        //open HeartRateGraphActivity
        OpenGraphBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, HeartRateGraphActivity.class);
                //i.putExtra("playerPos", position);
                startActivity(i);
            }
        });
    }

    public void CalculateBurnedCalories(){
        double caloriesBurned = steps * 0.044;
        caloriesTv.setText("" + caloriesBurned);
    }

    public void ShareWorkoutOnFb(){
        /*Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.logov002black);
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .build();
        SharePhotoContent content2 = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        fbShareButton.setShareContent(content2);
        */

        //Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.drawable.markerv001);
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentTitle("My workout!")
                .setContentUrl(Uri.parse("https://google.be"))
                //.setImageUrl(uri)
                .build();
        fbShareBtn.setShareContent(content);
    }

    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        float[] values = event.values;
        steps = -1;

        if (values.length > 0) {
            steps = (int) values[0];
        }

        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCounterTv.setText("" + steps + "");
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

    public void startTimeCheckLoop(){
        handler.postDelayed(runnableTime, 1000 * 60 * 30);
    }

    private Runnable runnableTime = new Runnable() {
        @Override
        public void run() {
            checkTimeOfDay();
            startTimeCheckLoop();
        }
    };
}
