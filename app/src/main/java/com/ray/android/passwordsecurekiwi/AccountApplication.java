package com.ray.android.passwordsecurekiwi;

import android.app.Application;
import android.content.Context;

public class AccountApplication extends Application {

    private static Context context;

    public static Context getAppContext() {
        return AccountApplication.context;
    }

    public void onCreate() {
        super.onCreate();
        AccountApplication.context = getApplicationContext();
    }
}
