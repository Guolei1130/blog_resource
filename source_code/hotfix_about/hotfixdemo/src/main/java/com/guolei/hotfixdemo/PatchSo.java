package com.guolei.hotfixdemo;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/4/3
 * Time: 下午4:47
 * Desc:
 */
class PatchSo {

    static void pathSo(Context context, String so_apk_path) {
        File apkFile = new File(so_apk_path);
        if (!apkFile.exists()) {
            return;
        }

        ClassLoader baseClassLoader = context.getClassLoader(); // PathClassLoader
        try {
            Field pathListField = baseClassLoader.getClass().getSuperclass().getDeclaredField("pathList");
            pathListField.setAccessible(true);
            Object pathList = pathListField.get(baseClassLoader);
            Class elementClz = Class.forName("dalvik.system.DexPathList$Element");
            Constructor<?> elementConstructor = elementClz.getConstructor(File.class, boolean.class,
                    File.class, DexFile.class);
            Method findLibMethod = elementClz.getDeclaredMethod("findNativeLibrary", String.class);
            findLibMethod.setAccessible(true);
            ZipFile zipFile = new ZipFile(apkFile);
            ZipEntry zipEntry = zipFile.getEntry("lib/armeabi/libnative-lib.so");
            InputStream inputStream = zipFile.getInputStream(zipEntry);
            File outSoFile = new File(context.getFilesDir(), "libnative-lib.so");
            if (outSoFile.exists()) {
                outSoFile.delete();
            }
            FileOutputStream outputStream = new FileOutputStream(outSoFile);
            byte[] cache = new byte[2048];
            int count = 0;
            while ((count = inputStream.read(cache)) != -1) {
                outputStream.write(cache, 0, count);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Object soElement = elementConstructor.newInstance(context.getFilesDir(), true, null, null);
            Class dexPathListClz = Class.forName("dalvik.system.DexPathList");
            Field soElementField = dexPathListClz.getDeclaredField("nativeLibraryPathElements");
            soElementField.setAccessible(true);
            Object[] soElements = (Object[]) soElementField.get(pathList);
            Object[] newSoElements = (Object[]) Array.newInstance(elementClz, soElements.length + 1);
            Object[] toAddSoElementArray = new Object[]{soElement};
            //复制到第一个
            System.arraycopy(toAddSoElementArray, 0, newSoElements, 0, toAddSoElementArray.length);
            System.arraycopy(soElements, 0, newSoElements, toAddSoElementArray.length, soElements.length);
            soElementField.set(pathList, newSoElements);
            ///将so的文件夹填充到nativeLibraryDirectories中
            Field libDir = dexPathListClz.getDeclaredField("nativeLibraryDirectories");
            libDir.setAccessible(true);
            List libDirs = (List) libDir.get(pathList);
            libDirs.add(0, context.getFilesDir());
            libDir.set(pathList, libDirs);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("hotfix", "pathSo() returned: " + e.getMessage());
        }
    }

}
