package com.guolei.hotfixdemo;

import android.app.Application;
import android.content.Context;


//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/4/2
 * Time: 上午10:30
 * Desc:
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (getSharedPreferences("hotfix", 0).getBoolean("enable_so", false)) {
            PatchSo.pathSo(base,"/sdcard/so_fix.apk");
        }
    }
}
