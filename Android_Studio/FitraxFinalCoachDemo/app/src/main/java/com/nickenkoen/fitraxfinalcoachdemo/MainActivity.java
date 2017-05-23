package com.nickenkoen.fitraxfinalcoachdemo;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.nickenkoen.fitraxfinalcoachdemo.R.id.heartRateNowTv;

public class MainActivity extends AppCompatActivity {

    public String teamName, userName, heartRate, currentSteps, caloriesBurned,
            heartRateAvg;

    public TextView stepCounter, calories, heartRateNow, heartRateAverage, teamNameInput;
    public EditText inputTeamName;
    public FloatingActionButton connectTeam;

    public DatabaseReference databaseReference;
    public FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        stepCounter = (TextView) findViewById(R.id.stepCounterTv);
        calories = (TextView) findViewById(R.id.caloriesTv);
        heartRateNow = (TextView) findViewById(heartRateNowTv);
        heartRateAverage = (TextView) findViewById(R.id.heartRateAverageTv);
        inputTeamName = (EditText) findViewById(R.id.inputTeamNameEditText);
        connectTeam = (FloatingActionButton) findViewById(R.id.connectToDatabase);
        teamNameInput = (TextView) findViewById(R.id.teamNameInputTextView);

        teamName = null;
        userName = null;

        initFireBase();

        //repeatedlyGetUserData();

        connectTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teamName = inputTeamName.getText().toString();
                inputTeamName.setText("");
                try {
                    getTeam();
                } catch (Exception e) {
                    // Keep on moving folks!
                }

                if (userName == null){
                    teamNameInput.setText("Not connected");
                }
            }
        });
    }

    private void initFireBase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Teams");
    }

    public void getTeam(){
        databaseReference.child(teamName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("userName").getValue(String.class);
                heartRate = dataSnapshot.child("heartRate").getValue(String.class);
                currentSteps = dataSnapshot.child("currentSteps").getValue(String.class);
                caloriesBurned = dataSnapshot.child("caloriesBurned").getValue(String.class);
                heartRateAvg = dataSnapshot.child("heartRateAvg").getValue(String.class);

                heartRateNow.setText(heartRate);
                stepCounter.setText(currentSteps);
                calories.setText(caloriesBurned);
                heartRateAverage.setText(heartRateAvg);
                teamNameInput.setText("Connected to: " + userName + " (" + teamName + ")");

                if (userName == null){
                    teamNameInput.setText("Not connected");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //teamName = databaseReference.getParent();
        //databaseReference.child(teamName).child("heartRate").setValue(heartRate);
    }
}
