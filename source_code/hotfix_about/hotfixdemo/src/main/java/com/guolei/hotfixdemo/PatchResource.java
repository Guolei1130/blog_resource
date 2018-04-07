package com.guolei.hotfixdemo;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.ArrayMap;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;


/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/4/2
 * Time: 下午4:54
 * Desc: copy from instant run source code
 */
class PatchResource {

    static void pathResource(Context context, String filePath, Collection<Activity> activities) {
        try {
            AssetManager newAssetManager = AssetManager.class.getConstructor().newInstance();
            Method mAddAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            mAddAssetPath.setAccessible(true);
            if (((Integer) mAddAssetPath.invoke(newAssetManager, filePath)) == 0) {
                throw new IllegalStateException("Could not create new AssetManager");
            }
            Method mEnsureStringBlocks = AssetManager.class.getDeclaredMethod("ensureStringBlocks");
            mEnsureStringBlocks.setAccessible(true);
            mEnsureStringBlocks.invoke(newAssetManager);

            if (activities != null) {
                for (Activity activity : activities) {
                    Resources resources = activity.getResources();
                    try {
                        // 7.0之前，用的mAssets
                        Field mAssets = Resources.class.getDeclaredField("mAssets");
                        mAssets.setAccessible(true);
                        mAssets.set(resources,newAssetManager);
                    }catch (Exception e) {
                        // 7.0及以后版本，用的mResourcesImpl#mAssets
                        Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
                        mResourcesImpl.setAccessible(true);
                        Object resourceImpl = mResourcesImpl.get(resources);
                        Field implAssets = resourceImpl.getClass().getDeclaredField("mAssets");
                        implAssets.setAccessible(true);
                        implAssets.set(resourceImpl, newAssetManager);
                    }

                    // 使用者可能哟通过getTheme.getResource 去获取资源，因此，需要替换掉theme中的
                    Resources.Theme theme = activity.getTheme();
                    try {
                        //版本差异
                        try {
                            Field ma = Resources.Theme.class.getDeclaredField("mAssets");
                            ma.setAccessible(true);
                            ma.set(theme, newAssetManager);
                        }catch (Exception e) {
                            Field themeField = Resources.Theme.class.getDeclaredField("mThemeImpl");
                            themeField.setAccessible(true);
                            Object impl = themeField.get(theme);
                            Field ma = impl.getClass().getDeclaredField("mAssets");
                            ma.setAccessible(true);
                            ma.set(impl, newAssetManager);
                        }

                        // 重新初始化Theme
                        Field mt = ContextThemeWrapper.class.getDeclaredField("mTheme");
                        mt.setAccessible(true);
                        mt.set(activity, null);
                        Method mtm = ContextThemeWrapper.class.getDeclaredMethod("initializeTheme");
                        mtm.setAccessible(true);
                        mtm.invoke(activity);
                        Method mCreateTheme = AssetManager.class.getDeclaredMethod("createTheme");
                        mCreateTheme.setAccessible(true);
                        Object internalTheme = mCreateTheme.invoke(newAssetManager);
                        Field mTheme = Resources.Theme.class.getDeclaredField("mTheme");
                        mTheme.setAccessible(true);
                        mTheme.set(theme, internalTheme);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    pruneResourceCaches(resources);
                }
            }

            // 替换掉缓存起来的
            Collection<WeakReference<Resources>> references;
            if (SDK_INT >= KITKAT) {
                Class<?> resourcesManagerClass = Class.forName("android.app.ResourcesManager");
                Method mGetInstance = resourcesManagerClass.getDeclaredMethod("getInstance");
                mGetInstance.setAccessible(true);
                Object resourcesManager = mGetInstance.invoke(null);
                try {
                    Field fMActiveResources = resourcesManagerClass.getDeclaredField("mActiveResources");
                    fMActiveResources.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    ArrayMap<?, WeakReference<Resources>> arrayMap =
                            (ArrayMap<?, WeakReference<Resources>>) fMActiveResources.get(resourcesManager);
                    references = arrayMap.values();
                } catch (NoSuchFieldException ignore) {
                    Field mResourceReferences = resourcesManagerClass.getDeclaredField("mResourceReferences");
                    mResourceReferences.setAccessible(true);
                    //noinspection unchecked
                    references = (Collection<WeakReference<Resources>>) mResourceReferences.get(resourcesManager);
                }
            }else {
                Class<?> activityThread = Class.forName("android.app.ActivityThread");
                Field fMActiveResources = activityThread.getDeclaredField("mActiveResources");
                fMActiveResources.setAccessible(true);
                Object thread = getActivityThread(context, activityThread);
                @SuppressWarnings("unchecked")
                HashMap<?, WeakReference<Resources>> map =
                        (HashMap<?, WeakReference<Resources>>) fMActiveResources.get(thread);
                references = map.values();
            }

            for (WeakReference<Resources> wr : references) {
                Resources resources = wr.get();
                if (resources != null) {
                    // Set the AssetManager of the Resources instance to our brand new one
                    try {
                        Field mAssets = Resources.class.getDeclaredField("mAssets");
                        mAssets.setAccessible(true);
                        mAssets.set(resources, newAssetManager);
                    } catch (Throwable ignore) {
                        Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
                        mResourcesImpl.setAccessible(true);
                        Object resourceImpl = mResourcesImpl.get(resources);
                        Field implAssets = resourceImpl.getClass().getDeclaredField("mAssets");
                        implAssets.setAccessible(true);
                        implAssets.set(resourceImpl, newAssetManager);
                    }
                    resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 清除资源的缓存
    private static void pruneResourceCaches(Object resources) {
        // Drain TypedArray instances from the typed array pool since these can hold on
        // to stale asset data
        if (SDK_INT >= LOLLIPOP) {
            try {
                Field typedArrayPoolField =
                        Resources.class.getDeclaredField("mTypedArrayPool");
                typedArrayPoolField.setAccessible(true);
                Object pool = typedArrayPoolField.get(resources);
                Class<?> poolClass = pool.getClass();
                Method acquireMethod = poolClass.getDeclaredMethod("acquire");
                acquireMethod.setAccessible(true);
                while (true) {
                    Object typedArray = acquireMethod.invoke(pool);
                    if (typedArray == null) {
                        break;
                    }
                }
            } catch (Throwable ignore) {
            }
        }
        if (SDK_INT >= M) {
            // Really should only be N; fix this as soon as it has its own API level
            try {
                Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
                mResourcesImpl.setAccessible(true);
                // For the remainder, use the ResourcesImpl instead, where all the fields
                // now live
                resources = mResourcesImpl.get(resources);
            } catch (Throwable ignore) {
            }
        }
        // Prune bitmap and color state lists etc caches
        Object lock = null;
        if (SDK_INT >= JELLY_BEAN_MR2) {
            try {
                Field field = resources.getClass().getDeclaredField("mAccessLock");
                field.setAccessible(true);
                lock = field.get(resources);
            } catch (Throwable ignore) {
            }
        } else {
            try {
                Field field = Resources.class.getDeclaredField("mTmpValue");
                field.setAccessible(true);
                lock = field.get(resources);
            } catch (Throwable ignore) {
            }
        }
        if (lock == null) {
            lock = PatchResource.class;
        }
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lock) {
            // Prune bitmap and color caches
            pruneResourceCache(resources, "mDrawableCache");
            pruneResourceCache(resources,"mColorDrawableCache");
            pruneResourceCache(resources,"mColorStateListCache");
            if (SDK_INT >= M) {
                pruneResourceCache(resources, "mAnimatorCache");
                pruneResourceCache(resources, "mStateListAnimatorCache");
            }
        }
    }
    private static boolean pruneResourceCache(Object resources,
                                              String fieldName) {
        try {
            Class<?> resourcesClass = resources.getClass();
            Field cacheField;
            try {
                cacheField = resourcesClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignore) {
                cacheField = Resources.class.getDeclaredField(fieldName);
            }
            cacheField.setAccessible(true);
            Object cache = cacheField.get(resources);
            // Find the class which defines the onConfigurationChange method
            Class<?> type = cacheField.getType();
            if (SDK_INT < JELLY_BEAN) {
                if (cache instanceof SparseArray) {
                    ((SparseArray) cache).clear();
                    return true;
                } else if (SDK_INT >= ICE_CREAM_SANDWICH && cache instanceof LongSparseArray) {
                    // LongSparseArray has API level 16 but was private (and available inside
                    // the framework) in 15 and is used for this cache.
                    //noinspection AndroidLintNewApi
                    ((LongSparseArray) cache).clear();
                    return true;
                }
            } else if (SDK_INT < M) {
                // JellyBean, KitKat, Lollipop
                if ("mColorStateListCache".equals(fieldName)) {
                    // For some reason framework doesn't call clearDrawableCachesLocked on
                    // this field
                    if (cache instanceof LongSparseArray) {
                        //noinspection AndroidLintNewApi
                        ((LongSparseArray)cache).clear();
                    }
                } else if (type.isAssignableFrom(ArrayMap.class)) {
                    Method clearArrayMap = Resources.class.getDeclaredMethod(
                            "clearDrawableCachesLocked", ArrayMap.class, Integer.TYPE);
                    clearArrayMap.setAccessible(true);
                    clearArrayMap.invoke(resources, cache, -1);
                    return true;
                } else if (type.isAssignableFrom(LongSparseArray.class)) {
                    Method clearSparseMap = Resources.class.getDeclaredMethod(
                            "clearDrawableCachesLocked", LongSparseArray.class, Integer.TYPE);
                    clearSparseMap.setAccessible(true);
                    clearSparseMap.invoke(resources, cache, -1);
                    return true;
                }
            } else {
                // Marshmallow: DrawableCache class
                while (type != null) {
                    try {
                        Method configChangeMethod = type.getDeclaredMethod(
                                "onConfigurationChange", Integer.TYPE);
                        configChangeMethod.setAccessible(true);
                        configChangeMethod.invoke(cache, -1);
                        return true;
                    } catch (Throwable ignore) {
                    }
                    type = type.getSuperclass();
                }
            }
        } catch (Throwable ignore) {
            // Not logging these; while there is some checking of SDK_INT here to avoid
            // doing a lot of unnecessary field lookups, it's not entirely accurate and
            // errs on the side of caution (since different devices may have picked up
            // different snapshots of the framework); therefore, it's normal for this
            // to attempt to look up a field for a cache that isn't there; only if it's
            // really there will it continue to flush that particular cache.
        }
        return false;
    }

    public static Object getActivityThread(Context context,
                                           Class<?> activityThread) {
        try {
            if (activityThread == null) {
                activityThread = Class.forName("android.app.ActivityThread");
            }
            Method m = activityThread.getMethod("currentActivityThread");
            m.setAccessible(true);
            Object currentActivityThread = m.invoke(null);
            if (currentActivityThread == null && context != null) {
                // In older versions of Android (prior to frameworks/base 66a017b63461a22842)
                // the currentActivityThread was built on thread locals, so we'll need to try
                // even harder
                Field mLoadedApk = context.getClass().getField("mLoadedApk");
                mLoadedApk.setAccessible(true);
                Object apk = mLoadedApk.get(context);
                Field mActivityThreadField = apk.getClass().getDeclaredField("mActivityThread");
                mActivityThreadField.setAccessible(true);
                currentActivityThread = mActivityThreadField.get(apk);
            }
            return currentActivityThread;
        } catch (Throwable ignore) {
            return null;
        }
    }

}
