package com.yw.android.aoptest;

import android.app.Application;

/**
 * Created by feng on 2017/10/9.
 */

public class BaseApplication extends Application {

    public static boolean isLogin = true;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
