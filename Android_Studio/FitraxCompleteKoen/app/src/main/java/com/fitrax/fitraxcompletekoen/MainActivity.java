package com.fitrax.fitraxcompletekoen;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public String teamName;
    public String userName;
    public String randomWelcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final TextView speedTv = (TextView) findViewById(R.id.speedTv);
        Button shareButton = (Button) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sms = speedTv.getText().toString() + " clicks";
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.putExtra("sms_body", sms);
                sendIntent.setData(Uri.parse("sms:"));
                if (sendIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(sendIntent);
                }
            }
        });

        //initialize facebook sdk
        //FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        ShareButton fbShareButton = (ShareButton) findViewById(R.id.shareButton);
        //Share

        //create randomWelcomeMessage
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

        //initialize offline database SharedPreference from LoginActivity
        Intent mainActivityIntent = getIntent();
        teamName = mainActivityIntent.getStringExtra("teamName");
        userName = mainActivityIntent.getStringExtra("userName");

        TextView userNameTextView = (TextView) findViewById(R.id.userNameText);
        TextView welcomeMessage = (TextView) findViewById(R.id.randomWelcomeMessageTextView);

        userNameTextView.setText("Welcome " + userName + "");
        welcomeMessage.setText("" + randomWelcomeMessage + "");
        //set text to Team Name of title/actionbar
        setTitle("Team: " + teamName + "");

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
        fbShareButton.setShareContent(content);



    }

    public void getAndShowCurrentHeartRate(){
        TextView heartRateNow = (TextView) findViewById(R.id.heartRateNowTv);

    }

}
