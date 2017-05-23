package com.nickenkoen.fitraxfinaldemo;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

import static com.nickenkoen.fitraxfinaldemo.MapsActivity.NOTIFICATION_ID;
import static com.nickenkoen.fitraxfinaldemo.R.id.heartRateNowTv;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public int steps;

    public String teamName, userName, randomWelcomeMessage, heartRate, currentSteps, caloriesBurned,
            heartRateAvg;

    public ImageButton newWorkoutBtn, OpenGraphBtn, selectDeviceBtn;
    public TextView stepCounter, calories, heartRateNow, HeartRateAverage;

    private SensorManager mSensorManager;

    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;

    public BluetoothLeService mBluetoothLeService;
    public String mDeviceAddress;

    public DatabaseReference databaseReference;
    public FirebaseDatabase firebaseDatabase;

    public SharedPreferences settings;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        randomWelcomeMessageFunct();

        if (!checkIfAlreadyHavePermission()) {
            requestForSpecificPermission();
        }

        settings = getSharedPreferences("preferences",
                Context.MODE_PRIVATE);

        teamName = settings.getString("teamName", teamName);
        userName = settings.getString("userName", userName);
        heartRate = null;
        currentSteps = null;
        caloriesBurned = null;
        heartRateAvg = null;

        initFireBase();
        createUser();

        TextView userNameTextView = (TextView) findViewById(R.id.userNameText);
        TextView welcomeMessage = (TextView) findViewById(R.id.randomWelcomeMessageTextView);

        userNameTextView.setText("Welcome " + userName + "");
        welcomeMessage.setText("" + randomWelcomeMessage + "");
        setTitle("Team: " + teamName + "");

        saveUserOffline();

        stepCounter = (TextView) findViewById(R.id.stepCounterTv);
        calories = (TextView) findViewById(R.id.caloriesTv);
        heartRateNow = (TextView) findViewById(heartRateNowTv);
        HeartRateAverage = (TextView) findViewById(R.id.heartRateAverageTv);

        newWorkoutBtn = (ImageButton) findViewById(R.id.newWorkoutButton);
        OpenGraphBtn = (ImageButton) findViewById(R.id.openGraphButton);
        selectDeviceBtn =(ImageButton) findViewById(R.id.selectDevice);

        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        newWorkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        OpenGraphBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, HeartRateGraphActivity.class);
                startActivity(i);
            }
        });

        selectDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DeviceScanActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // nothing to show here folks!
                }
                else {
                    killApp();
                }

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                        123);

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                123);
    }

    private boolean checkIfAlreadyHavePermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        else {
            return false;
        }
    }

    private void initFireBase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Teams");
    }

    public void createUser(){
        databaseReference.child(teamName).child("userName").setValue(userName);
        databaseReference.child(teamName).child("heartRate").setValue(heartRate);
        databaseReference.child(teamName).child("currentSteps").setValue(currentSteps);
        databaseReference.child(teamName).child("caloriesBurned").setValue(caloriesBurned);
        databaseReference.child(teamName).child("heartRateAvg").setValue(heartRateAvg);
    }

    public void killApp(){
        // kill all notifications if there are any
        NotificationManager notificationMngr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationMngr.cancel(NOTIFICATION_ID);

        finish();

        // kills app
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    public void saveUserOffline(){
        editor = settings.edit();
        editor.putString("teamName", teamName);
        editor.putString("userName", userName);
        editor.commit();
    }

    public void CalculateBurnedCalories(){
        double caloriesBurnedLocal = steps * 0.044;
        caloriesBurned = String.format("%.1f", caloriesBurnedLocal);
        if(!calories.getText().equals(caloriesBurned))
            calories.setText(caloriesBurned);
    }

    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        float[] values = event.values;
        steps = -1;

        if (values.length > 0) {
            steps = (int) values[0];
        }

        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCounter.setText("" + steps + "");
        }

        CalculateBurnedCalories();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause(){
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        heartRateNow.setText(heartRate);
        if(heartRate == null || heartRate == ""){
            heartRateNow.setText("-");
        }

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

    public void randomWelcomeMessageFunct(){
        Random r = new Random();
        int rInt = r.nextInt(5) + 1;
        switch (rInt){
            case 1:
                randomWelcomeMessage = "Stay fit!";
                break;
            case 2:
                randomWelcomeMessage = "Keep active!";
                break;
            case 3:
                randomWelcomeMessage = "It's your day!";
                break;
            case 4:
                randomWelcomeMessage = "Let's workout!";
                break;
            case 5:
                randomWelcomeMessage = "Let's go!";
                break;
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                heartRate = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                heartRateNow.setText(heartRate);

                if(heartRate == null || heartRate == ""){
                    heartRateNow.setText("-");
                }

                currentSteps = Integer.toString(steps);
                heartRateAvg = Integer.toString(123);
                // TODO: 23-5-2017 hierboven ^^ integer van heartRateAvg zetten.

                databaseReference.child(teamName).child("heartRate").setValue(heartRate);
                databaseReference.child(teamName).child("currentSteps").setValue(currentSteps);
                databaseReference.child(teamName).child("caloriesBurned").setValue(caloriesBurned);
                databaseReference.child(teamName).child("heartRateAvg").setValue(heartRateAvg);
            }
        }
    };
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}