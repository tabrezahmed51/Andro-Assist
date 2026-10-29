package com.giyhub.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            RoleManager roleManager = new RoleManager(context);
            if (roleManager.isChild()) {
                Intent serviceIntent = new Intent(context, ChildMonitorService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            }
        }
    }
}
