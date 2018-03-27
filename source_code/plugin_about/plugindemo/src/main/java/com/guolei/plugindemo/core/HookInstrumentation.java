package com.guolei.plugindemo.core;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

import static com.guolei.plugindemo.Constants.TAG;
import static com.guolei.plugindemo.Constants.TARGET_ACTIVITY;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/3/8
 * Time: 下午9:54
 * Desc:
 */
public class HookInstrumentation extends Instrumentation {

    private Instrumentation mOriginInstrumentation;
    private PackageManager mPackageManager;

    public HookInstrumentation(Instrumentation instrumentation, PackageManager packageManager) {
        mOriginInstrumentation = instrumentation;
        mPackageManager = packageManager;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        if (!TextUtils.isEmpty(intent.getStringExtra(TARGET_ACTIVITY))) {
            return super.newActivity(cl, intent.getStringExtra(TARGET_ACTIVITY), intent);
        }
        return super.newActivity(cl, className, intent);
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        List<ResolveInfo> infos = mPackageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        if (infos == null || infos.size() == 0) {
            //没查到，要启动的这个没注册
            intent.putExtra(TARGET_ACTIVITY, intent.getComponent().getClassName());
            intent.setClassName(who, "com.guolei.plugindemo.StubActivity");
        }

        Class instrumentationClz = Instrumentation.class;
        try {
            Method execMethod = instrumentationClz.getDeclaredMethod("execStartActivity",
                    Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class);
            return (ActivityResult) execMethod.invoke(mOriginInstrumentation, who, contextThread, token,
                    target, intent, requestCode, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
