package com.example.nick.overviewofhealthstatistics_nick;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class CurrentTimeService extends Service {

    public long time;

    public CurrentTimeService(){
        time = System.currentTimeMillis();
        Context ctx = getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("time", time);
        editor.commit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
