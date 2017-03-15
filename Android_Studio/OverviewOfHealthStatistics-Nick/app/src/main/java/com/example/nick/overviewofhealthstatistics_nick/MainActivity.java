package com.example.nick.overviewofhealthstatistics_nick;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        ShareButton fbShareButton = (ShareButton) findViewById(R.id.shareButton);
        //Share

        /*Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.logov002black);
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .build();
        SharePhotoContent content2 = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        fbShareButton.setShareContent(content2);
        */

        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.drawable.markerv001);
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentTitle("My workout!")
                .setContentUrl(Uri.parse("https://google.be"))
                .setImageUrl(uri)
                .build();
        fbShareButton.setShareContent(content);

    }

}
