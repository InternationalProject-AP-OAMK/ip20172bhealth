package com.nickenkoen.fitraxfinaldemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadHeartRateReceiver extends BroadcastReceiver {
    // Prevents instantiation
    public DownloadHeartRateReceiver() {
    }
    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
    }
}
