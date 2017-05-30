package com.nickenkoen.fitraxfinalcoachdemo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
import android.view.View;


public class SplashScreen extends AppCompatActivity {

    public int splashScreenTimerInt = 3000;
    public boolean splashScreenSkipped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        splashScreenTimer();
    }

    //when screen touched/pressed, go to Login Activity
    public void skipSplashScreen(View v){
        splashScreenSkipped = true;
        ConstraintLayout splashScreen = (ConstraintLayout) findViewById(R.id.splashScreenLayout);
        splashScreen.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        goToMainActivity();
    }

    //timer to go automatically to Login Activity after certain time
    public void splashScreenTimer(){
        final Handler splashScreenTimerHandler = new Handler();
        splashScreenTimerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!splashScreenSkipped) {
                    goToMainActivity();
                }
            }
        }, splashScreenTimerInt);
    }

    public void goToMainActivity(){
        startActivity(new Intent(SplashScreen.this, MainActivity.class));
    }
}
