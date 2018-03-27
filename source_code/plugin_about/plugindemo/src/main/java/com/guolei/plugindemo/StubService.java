package com.guolei.plugindemo;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.guolei.plugindemo.Constants.TAG;
import static com.guolei.plugindemo.Constants.TARGET_ACTIVITY;
import static com.guolei.plugindemo.Constants.TARGET_SERVICE;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/3/9
 * Time: 下午6:25
 * Desc:
 */
public class StubService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: stub service ");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: stub service ");
        if (intent != null && !TextUtils.isEmpty(intent.getStringExtra(TARGET_SERVICE))) {
            //启动真正的service
            String serviceName = intent.getStringExtra(TARGET_SERVICE);
            try {
                Class activityThreadClz = Class.forName("android.app.ActivityThread");
                Method getActivityThreadMethod = activityThreadClz.getDeclaredMethod("getApplicationThread");
                getActivityThreadMethod.setAccessible(true);
                //获取ActivityThread
                Class contextImplClz = Class.forName("android.app.ContextImpl");
                Field mMainThread = contextImplClz.getDeclaredField("mMainThread");
                mMainThread.setAccessible(true);
                Object activityThread = mMainThread.get(getBaseContext());
                Object applicationThread = getActivityThreadMethod.invoke(activityThread);
                //获取token值
                Class iInterfaceClz = Class.forName("android.os.IInterface");
                Method asBinderMethod = iInterfaceClz.getDeclaredMethod("asBinder");
                asBinderMethod.setAccessible(true);
                Object token = asBinderMethod.invoke(applicationThread);
                //Service的attach方法
                Class serviceClz = Class.forName("android.app.Service");
                Method attachMethod = serviceClz.getDeclaredMethod("attach",
                        Context.class, activityThreadClz, String.class, IBinder.class, Application.class, Object.class);
                attachMethod.setAccessible(true);
                Class activityManagerNative = Class.forName("android.app.ActivityManagerNative");
                Field gDefaultField = activityManagerNative.getDeclaredField("gDefault");
                gDefaultField.setAccessible(true);
                Object origin = gDefaultField.get(null);
                Class singleton = Class.forName("android.util.Singleton");
                Field mInstanceField = singleton.getDeclaredField("mInstance");
                mInstanceField.setAccessible(true);
                Object originAMN = mInstanceField.get(origin);
                Service targetService = (Service) Class.forName(serviceName).newInstance();
                attachMethod.invoke(targetService, this, activityThread, intent.getComponent().getClassName(), token,
                        getApplication(), originAMN);
                //service的oncreate方法
                Method onCreateMethod = serviceClz.getDeclaredMethod("onCreate");
                onCreateMethod.setAccessible(true);
                onCreateMethod.invoke(targetService);
                targetService.onStartCommand(intent, flags, startId);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "onStartCommand: " + e.getMessage());
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
