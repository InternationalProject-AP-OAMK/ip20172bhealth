package com.example.nick.fitraxdemo1;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Marker mHereIAm;

    public LocationService gps;

    public LatLng latLngCurrent;

    public Location myLocation;

    public int repeatTimer = 10000;

    public float distZoom = 13; // 8 <> 14

    public double latitudeCurrentPosition;
    public double longitudeCurrentPosition;
    public double latitudeCurrentPositionMarker;
    public double longitudeCurrentPositionMarker;

    public boolean goToLocation, loopStarted, loopStopped;

    public String currentLocationMarker;

    public static final int NOTIFICATION_ID = 1;

    public PowerManager.WakeLock wakeLock;

    public final Handler handlerLoopLocation = new Handler();
    public Handler handler = new Handler();

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (!checkIfAlreadyHavePermission()) {
            requestForSpecificPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        settings = getSharedPreferences("preferences",
                Context.MODE_PRIVATE);

        currentLocationMarker = "Let's start!";
        goToLocation = true;
        loopStarted = false;
        loopStopped = true;

        initiateWakeCPU();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLocation();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngCurrent));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(4));

        drawFirstMarkerOnMap();

        mMap.animateCamera(CameraUpdateFactory.zoomTo(distZoom));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    goToLocation = true;
                    getLocation();

                }
                else {
                    killApp();
                }

                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                        123);

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed(){
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);

    }

    public void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(MapsActivity.this,
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

    public void drawFirstMarkerOnMap() {
        mHereIAm = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitudeCurrentPosition, longitudeCurrentPosition))
                .title(currentLocationMarker)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.markerv001)));
        mHereIAm.setTag(0);
        mHereIAm.showInfoWindow();
    }

    public void drawUpdateMarkerOnMap(){
        latitudeCurrentPositionMarker = latitudeCurrentPosition;
        longitudeCurrentPositionMarker = longitudeCurrentPosition;
        mHereIAm = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitudeCurrentPositionMarker, longitudeCurrentPositionMarker))
                .title(null)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.markerdotv001)));
        mHereIAm.setTag(0);
        mHereIAm.showInfoWindow();
    }

    public void getLocation(){
        wakeLock.acquire();

        gps = new LocationService(MapsActivity.this);
        if(gps.canGetLocation()) { // gps enabled} // return boolean true/false
            latitudeCurrentPosition = gps.getLatitude(); // returns latitude
            longitudeCurrentPosition = gps.getLongitude(); // returns longitude

            latLngCurrent = new LatLng(latitudeCurrentPosition, longitudeCurrentPosition);

            //Toast toastLatCur = makeText(getApplicationContext(), "Lat Current: " + latitudeCurrentPosition + "" ,Toast.LENGTH_SHORT);
            //toastLatCur.show();

            //Toast toastLongCur = makeText(getApplicationContext(), "Long Current: " + longitudeCurrentPosition + "" ,Toast.LENGTH_SHORT);
            //toastLongCur.show();
        }

        else {
            gps.showSettingsAlert();
        }

        if(goToLocation){
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngCurrent));
            goToLocation = false;

            //if(firstStart){
            //    mMap.animateCamera(CameraUpdateFactory.zoomTo(distZoom));
            //    firstStart = false;
            //}
        }

        wakeLock.release();
    }

    public void updateCurrentPosition() {
        final Runnable runnableUpdatePosition = new Runnable() {
            public void run() {
                // do stuff here
                getLocation();

                //if (latitudeCurrentPosition > latitudeCurrentPositionMarker || latitudeCurrentPosition < latitudeCurrentPositionMarker){
                //    if(longitudeCurrentPosition > longitudeCurrentPositionMarker || longitudeCurrentPosition < longitudeCurrentPositionMarker){
                        Toast.makeText(MapsActivity.this, "New marker drawn", Toast.LENGTH_SHORT).show();
                        drawUpdateMarkerOnMap();
                //    }
                //}

                if (loopStarted){
                    handlerLoopLocation.postDelayed(this, repeatTimer);
                }
            }

        };
        runnableUpdatePosition.run();
    }

    public void initiateWakeCPU(){
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
    }

    public void killAppButton(View v){
        ImageButton killAppButton = (ImageButton) findViewById(R.id.killAppButton);
        killAppButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        killApp();
    }

    public void killApp(){
        NotificationManager notificationMngr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationMngr.cancel(NOTIFICATION_ID);

        finish();

        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    public void startRunningButton(View v){
        ImageButton startRunningButton = (ImageButton) findViewById(R.id.startRunningButton);
        startRunningButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        loopStarted = true;
        Toast.makeText(this, "Workout started!", Toast.LENGTH_SHORT).show();
        currentLocationMarker = "Workout started!";
        mMap.clear();
        drawFirstMarkerOnMap();
        updateCurrentPosition();
    }

    public void clearMapButton(View v){
        clearMap();
    }

    public void clearMap(){
        mMap.clear();
        getLocation();
        drawFirstMarkerOnMap();
    }

    //public void startLoop(){
    //    handler.postDelayed(runnable, 5000);
    //}
}


