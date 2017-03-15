package com.fitrax.koenfitraxloginandrequestsaveddataoffline;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public String teamName;
    public String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent mainActivityIntent = getIntent();
        teamName = mainActivityIntent.getStringExtra("teamName");
        userName = mainActivityIntent.getStringExtra("userName");


        TextView teamNameTextView = (TextView) findViewById(R.id.teamNameText);
        TextView userNameTextView = (TextView) findViewById(R.id.userNameText);

        teamNameTextView.setText(teamName);
        userNameTextView.setText(userName);
    }
}
