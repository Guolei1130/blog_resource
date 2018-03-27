package com.guolei.plugindemo;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/3/9
 * Time: 下午1:32
 * Desc:
 */
public class Constants {
    public static final String TAG = "plugin";
    public static final String TARGET_ACTIVITY = "target_activity";
    public static final String TARGET_SERVICE = "target_service";

    /**
     * 或者通过 {@link android.os.Build#SUPPORTED_ABIS}
     *
     * @return
     */
    public static boolean isDalvik() {
        String SELECT_RUNTIME_PROPERTY = "persist.sys.dalvik.vm.lib";
        String LIB_DALVIK = "libdvm.so";
        String LIB_ART = "libart.so";
        String LIB_ART_D = "libartd.so";
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            try {
                Method get = systemProperties.getMethod("get",
                        String.class, String.class);
                if (get == null) {
                    return false;
                }
                try {
                    final String value = (String) get.invoke(
                            systemProperties, SELECT_RUNTIME_PROPERTY,
                        /* Assuming default is */"Dalvik");
                    if (LIB_DALVIK.equals(value)) {
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    return false;
                }
            } catch (NoSuchMethodException e) {
                return false;
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
