package com.fitrax.fitraxpart2koen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartAtBootReceiver extends BroadcastReceiver {
    public StartAtBootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent autoStart = new Intent(context,MainActivity.class);
        context.startService(autoStart);
    }
}
