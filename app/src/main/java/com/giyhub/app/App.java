package com.giyhub.app;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Tiny AI Self-Healing & Crash Recovery Engine
        AppHealthManager.getInstance(this).initializeSelfHealing();
    }
}
