package com.nickenkoen.fitraxfinaldemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class HeartRateGraphActivity extends AppCompatActivity {

    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;
    private int heartBeatNR = 0;
    private int heartBeatNROld = 0;

    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private LineGraphSeries<DataPoint> mSeries;
    private double graphLastXValue = 5d;

    private Button startBtn;
    private boolean startGraph = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_graph);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGraph = !startGraph;
                if(startGraph) {
                    mTimer = new Runnable() {
                        @Override
                        public void run() {
                            graphLastXValue += 1d;
                            if(heartBeatNR != heartBeatNROld){
                            //if(getRandom() != mLastRandom){
                                //mLastRandom = random;
                                mSeries.appendData(new DataPoint(graphLastXValue, /*random*/ heartBeatNR), true, 200000);
                            }
                            mHandler.postDelayed(this, 1200);
                        }
                    };
                    mHandler.postDelayed(mTimer, 1000);
                }
                else {
                    mHandler.removeCallbacks(mTimer);
                }
            }
        });

        GraphView graph = (GraphView) findViewById(R.id.graph);
        mSeries = new LineGraphSeries<>();

        graph.addSeries(mSeries);
        Viewport viewport = graph.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(100);
        viewport.setScrollable(true);
    }
    /*
    double mLastRandom = 5;
    Random mRand = new Random();
    int random;
    private double getRandom() {
        return random = mRand.nextInt(70-69+1)+69;
    }*/

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                //Log.e(TAG, "Unable to initialize Bluetooth");
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
                heartBeatNR = Integer.parseInt(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer);
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
}