package com.guolei.plugindemo.core;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.List;

import static com.guolei.plugindemo.Constants.TAG;
import static com.guolei.plugindemo.Constants.TARGET_SERVICE;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/3/9
 * Time: 下午2:33
 * Desc:
 */
public class ActivityManagerProxy implements InvocationHandler {

    private PackageManager mPackageManager;
    private Object mOrigin;

    public ActivityManagerProxy(PackageManager packageManager, Object o) {
        mPackageManager = packageManager;
        mOrigin = o;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("startService")) {
            Intent intent = (Intent) args[1];
            List<ResolveInfo> infos = mPackageManager.queryIntentServices(intent, PackageManager.MATCH_ALL);
            if (infos == null || infos.size() == 0) {
                intent.putExtra(TARGET_SERVICE, intent.getComponent().getClassName());
                intent.setClassName("com.guolei.plugindemo", "com.guolei.plugindemo.StubService");
            }

        }
        return method.invoke(mOrigin, args);
    }
}
