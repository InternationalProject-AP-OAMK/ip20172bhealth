package com.fitrax.fitraxpart2koen;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import static com.fitrax.fitraxpart2koen.MapsActivity.NOTIFICATION_ID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView userNameTextView;
    public TextView welcomeMessage;
    public TextView textViewSensorType;
    public TextView speedTv;
    public TextView stepCounterTv;
    public TextView caloriesTv;
    public TextView heartRateNowTv;
    public TextView HeartRateAverageTv;

    public String teamName;
    public String userName;
    public String randomWelcomeMessage;

    public ShareButton fbShareBtn;
    private ShareButton shareButton;
    private Bitmap image;
    private int counter = 0;

    //public ShareDialog shareDialog;
    public Button newWorkoutBtn;
    public Button OpenGraphBtn;

    public int steps;
    public long time;

    private SensorManager mSensorManager;

    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;

    public FloatingActionButton signOutButton;

    public Handler handler = new Handler();

    public SharedPreferences settings;
    public SharedPreferences.Editor editor;

    private FirebaseAuth.AuthStateListener mAuthListener;
    public FirebaseAuth firebaseAuth;

    public FirebaseDatabase firebaseDatabase;
    public DatabaseReference databaseReference;

    //public FirebaseDatabase database;
    //public DatabaseReference mRootRef;
    //DatabaseReference mSignUpRef = mRootRef.child("userName");
    public FirebaseUser firebaseUser;

    public PowerManager.WakeLock wakeLock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        settings = getSharedPreferences("preferences",
                Context.MODE_PRIVATE);
        editor = settings.edit();

        //userName = settings.getString("userName", userName);
        //teamName = settings.getString("teamName", teamName);

        //get firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        //get current user
        //firebaseUser = getIntent().getExtras("Username");
        userName = getIntent().getStringExtra("Username");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

       mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               String naam = dataSnapshot.child("users/abcd/naam").getKey();
               userNameTextView.setText(naam);
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });

        signOutButton = (FloatingActionButton) findViewById(R.id.signout);

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        //userName = settings.getString("userName", userName);
        //teamName = settings.getString("teamName", teamName);

        randomWelcomeMessageFunct();

        if (!checkIfAlreadyHavePermission()) {
            requestForSpecificPermission();
        }

        //initialize facebook sdk
        FacebookSdk.sdkInitialize(getApplicationContext());

        //initialize offline database SharedPreference from LoginActivity

        userNameTextView = (TextView) findViewById(R.id.userNameText);
        welcomeMessage = (TextView) findViewById(R.id.randomWelcomeMessageTextView);

        Toast.makeText(MainActivity.this, "userName: " + userName, Toast.LENGTH_SHORT).show();
        welcomeMessage.setText("" + randomWelcomeMessage + "");
        userNameTextView.setText("Welcome " + userName + "");

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

        //shareDialog = new ShareDialog(this);

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("teamName");

        //myRef.setValue(teamName);

        //shareButton = (ShareButton) findViewById(R.id.shareButton);
        //shareButton.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View view) {
        //        shareScreenshotOnFacebook();
        //    }
        //});



        /*
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        //databaseReference = firebaseDatabase.getReference("userName");
        //databaseReference.setValue(userName);
        databaseReference = firebaseDatabase.getReference();
        DatabaseReference teamNameDataReference = databaseReference.child("teamName").child("team1").child("name");
        DatabaseReference userNameDataReference = databaseReference.child("teamName").child("team1").child("userName");
        //databaseReference.setValue(teamName);

        teamNameDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String value = dataSnapshot.getValue(String.class);
                teamName = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        userNameDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String value = dataSnapshot.getValue(String.class);
                userName = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        */



    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        Context ctx = getApplicationContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString("username", userName).commit();
    }
    @Override
    public void onStop() {
        super.onStop();

        mSensorManager.unregisterListener(this, mStepCounterSensor);
        mSensorManager.unregisterListener(this, mStepDetectorSensor);
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



    public void signOut(){
        firebaseAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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

    public void CalculateBurnedCalories(){
        double caloriesBurned = steps * 0.044;
        String newCaloriesBurned = String.format("%.1f", caloriesBurned);
        if(!caloriesTv.getText().equals(newCaloriesBurned))
            caloriesTv.setText(newCaloriesBurned);
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
                //Dummy logo link, shared via Dropbox
                .setContentUrl(Uri.parse("https://www.dropbox.com/s/496dsu4xujzc7q8/logov002squaredblack.png?dl=0"))

                //.setImageUrl(uri)
                .build();
        fbShareBtn.setShareContent(content);
    }

    /*
    public void shareScreenshotOnFacebook(View view) {
        //check counter
        if(counter == 0) {
            //save the screenshot
            View rootView = findViewById(android.R.id.content).getRootView();
            rootView.setDrawingCacheEnabled(true);
            // creates immutable clone of image
            image = Bitmap.createBitmap(rootView.getDrawingCache());
            // destroy
            rootView.destroyDrawingCache();

            //share dialog
            AlertDialog.Builder shareDialog = new AlertDialog.Builder(this);
            shareDialog.setTitle("Share Screen Shot");
            shareDialog.setMessage("Share image to Facebook?");
            shareDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //share the image to Facebook
                    SharePhoto photo = new SharePhoto.Builder().setBitmap(image).build();
                    SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
                    shareButton.setShareContent(content);
                    counter = 1;
                    shareButton.performClick();
                }
            });
            shareDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            shareDialog.show();
        }
        else {
            counter = 0;
            shareButton.setShareContent(null);
        }
    }
    */

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
        //calculate calories
        CalculateBurnedCalories();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onResume() {

        super.onResume();

        Context ctx = getApplicationContext();
        userName = PreferenceManager.getDefaultSharedPreferences(ctx).getString("username", "defaultStringIfNothingFound");
        //userNameTextView.setText("Welcome " + userName + "");

        mSensorManager.registerListener(this, mStepCounterSensor,

                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mStepDetectorSensor,

                SensorManager.SENSOR_DELAY_FASTEST);
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


    public void getAndShowCurrentHeartRate(){
        TextView heartRateNow = (TextView) findViewById(R.id.heartRateNowTv);

    }
}
