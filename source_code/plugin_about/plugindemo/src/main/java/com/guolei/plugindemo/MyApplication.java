package com.guolei.plugindemo;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ProviderInfo;
import android.content.res.AssetManager;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.guolei.plugindemo.core.ActivityManagerProxy;
import com.guolei.plugindemo.core.HookInstrumentation;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexFile;

import static com.guolei.plugindemo.Constants.TAG;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/3/9
 * Time: 上午10:50
 * Desc:
 */
public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        hookInstrumentation();
        hookAMS();
//        loadClassByHostClassLoader();
    }

    private void hookInstrumentation() {
        Context context = getBaseContext();
        try {
            Class contextImplClz = Class.forName("android.app.ContextImpl");
            Field mMainThread = contextImplClz.getDeclaredField("mMainThread");
            mMainThread.setAccessible(true);
            Object activityThread = mMainThread.get(context);
            Class activityThreadClz = Class.forName("android.app.ActivityThread");
            Field mInstrumentationField = activityThreadClz.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            mInstrumentationField.set(activityThread,
                    new HookInstrumentation((Instrumentation) mInstrumentationField.get(activityThread),
                            context.getPackageManager()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("plugin", "hookInstrumentation: error");
        }
    }



    private void hookAMS() {
        try {
            Class activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Field gDefaultField = activityManagerNative.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            Object origin = gDefaultField.get(null);
            Class singleton = Class.forName("android.util.Singleton");
            Field mInstanceField = singleton.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            Object originAMN = mInstanceField.get(origin);
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{Class.forName("android.app.IActivityManager")},
                    new ActivityManagerProxy(getPackageManager(),originAMN));
            mInstanceField.set(origin, proxy);
            Log.e(TAG, "hookAMS: success" );
        } catch (Exception e) {
            Log.e(TAG, "hookAMS: " + e.getMessage());
        }
    }
}
