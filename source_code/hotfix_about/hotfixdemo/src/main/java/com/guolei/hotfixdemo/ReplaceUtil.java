package com.guolei.hotfixdemo;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/4/4
 * Time: 下午2:30
 * Desc:
 */
public class ReplaceUtil {

    static Field artMethodField;
    private static Class unsafeClass;
    private static Object unsafe;
    private static Method getIntVolatileMethod;
    private static Method putIntVolatileMethod;
    private static Method arrayBaseOffsetMethod;
    private static Method objectFieldOffsetMethod;

    static {
        try {
            Class absMethodClass = Class.forName("java.lang.reflect.AbstractMethod");
            artMethodField = absMethodClass.getDeclaredField("artMethod");
            artMethodField.setAccessible(true);

            unsafeClass = Class.forName("sun.misc.Unsafe");
            if (Build.VERSION.SDK_INT >= 19) {
                Field theUnsafeInstance = unsafeClass.getDeclaredField("theUnsafe");
                theUnsafeInstance.setAccessible(true);
                unsafe = theUnsafeInstance.get(null);
            } else {
                Class AQSClass = Class.forName("java.util.concurrent.locks.AbstractQueuedSynchronizer");
                Field theUnsafeInstance = AQSClass.getDeclaredField("unsafe");
                theUnsafeInstance.setAccessible(true);
                unsafe = theUnsafeInstance.get(null);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            getIntVolatileMethod = unsafeClass.getDeclaredMethod("getIntVolatile", Object.class, long.class);
            getIntVolatileMethod.setAccessible(true);

            putIntVolatileMethod = unsafeClass.getDeclaredMethod("putIntVolatile", Object.class, long.class, int.class);
            putIntVolatileMethod.setAccessible(true);

            arrayBaseOffsetMethod = unsafeClass.getDeclaredMethod("arrayBaseOffset", Class.class);
            arrayBaseOffsetMethod.setAccessible(true);

            objectFieldOffsetMethod = unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class);
            objectFieldOffsetMethod.setAccessible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int calculateMethodSize() {
        try {
            Class clz = Class.forName("com.guolei.hotfixdemo");
        } catch (Exception e) {

        }

        return 0;
    }

    public static long[] getAddr(Method src,Method des){
        try {
            long srcAddr = (long) artMethodField.get(src);
            long desAddr = (long) artMethodField.get(des);
//            int value = (int) getIntVolatileMethod.invoke(unsafe,null,desAddr);
//            putIntVolatileMethod.invoke(unsafe,null,srcAddr,value);
            return new long[]{srcAddr,desAddr};
        }catch (Exception e) {
            e.printStackTrace();
        }
        return new long[]{};
    }

}
