package com.giyhub.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AudioMonitorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start your audio monitoring service here
        return START_STICKY;
    }
}
