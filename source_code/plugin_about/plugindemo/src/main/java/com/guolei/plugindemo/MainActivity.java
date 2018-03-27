package com.guolei.plugindemo;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

import static com.guolei.plugindemo.Constants.TAG;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = Constants.TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

        findViewById(R.id.start_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MainActivity.this,
//                            TwoActivity.class
                            Class.forName("com.guolei.plugin_1.PluginActivity")
                    );
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, " onClick:  start activity error");
                }
            }
        });

        findViewById(R.id.start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startService(new Intent(MainActivity.this,
                        TestService.class));
            }
        });

        findViewById(R.id.load_ext_jar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadExtJar();
            }

        });

        findViewById(R.id.load_ext_apk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadExtApk();
            }

        });
        findViewById(R.id.load_class_by_host).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadClassByHostClassLoader();
            }
        });
        findViewById(R.id.delegate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query();
            }
        });

        findViewById(R.id.load_so_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSoPlugin();
            }
        });
        findViewById(R.id.start_so_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MainActivity.this,
//                            TwoActivity.class
                            Class.forName("com.guolei.so.MainActivity")
                    );
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, " onClick:  start activity error");
                }
            }
        });
    }

    private void query() {
        getContentResolver().query(Uri.parse("content://com.guolei.delegate.content/com.guolei.plugin_1.provider"),
                null, null, null, null);
    }

    private void loadExtJar() {
        String dexPath = new File("/sdcard/simpledex.jar").getPath();
        File dexOptOutDir = new File(getFilesDir(), "dexopt");
        if (!dexOptOutDir.exists()) {
            boolean result = dexOptOutDir.mkdir();
            if (!result) {
                Log.e(TAG, "loadExtJar: create out dir error");
            }
        }
        String dexOptOutDr = dexOptOutDir.getPath();

        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, dexOptOutDr, null, ClassLoader.getSystemClassLoader());
//        PathClassLoader dexClassLoader = new PathClassLoader(dexPath,ClassLoader.getSystemClassLoader());
        try {
            Class userClz = dexClassLoader.loadClass("com.simplejar.User");
            Object user = userClz.getConstructor(String.class, int.class).newInstance("guolei", 24);
            Method method = userClz.getDeclaredMethod("toString");
            method.setAccessible(true);
            String msg = (String) method.invoke(user);
            Log.e(TAG, "loadExtJar: " + msg);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadExtApk() {
        String apkPath = new File("/sdcard/plugin_1.apk").getPath();
        File dexOptOutDir = new File(getFilesDir(), "dexopt");
        if (!dexOptOutDir.exists()) {
            boolean result = dexOptOutDir.mkdir();
            if (!result) {
                Log.e(TAG, "loadExtJar: create out dir error");
            }
        }
        String dexOptOutDr = dexOptOutDir.getPath();
        ClassLoader classLoader = null;
        if (Constants.isDalvik()) {
            classLoader = new DexClassLoader(apkPath, dexOptOutDr, null, ClassLoader.getSystemClassLoader());
        } else {
            classLoader = new PathClassLoader(apkPath, ClassLoader.getSystemClassLoader());
        }
        try {
            Class userClz = classLoader.loadClass("com.guolei.plugin_1.People");
            Object user = userClz.getConstructor(String.class, int.class).newInstance("guolei", 24);
            Method method = userClz.getDeclaredMethod("toString");
            method.setAccessible(true);
            String msg = (String) method.invoke(user);
            Log.e(TAG, "loadExtApk: " + msg);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseApkFile() {
        String apkPath = new File("/sdcard/plugin_1.apk").getPath();
    }


    // load apk 相关的东西了

    // 利用Apk的ClassLoader去加载插件
    private void loadClassByHostClassLoader() {
        File apkFile = new File("/sdcard/plugin_1.apk");
        ClassLoader baseClassLoader = this.getClassLoader();
        try {
            Field pathListField = baseClassLoader.getClass().getSuperclass().getDeclaredField("pathList");
            pathListField.setAccessible(true);
            Object pathList = pathListField.get(baseClassLoader);

            Class clz = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = clz.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            Object[] dexElements = (Object[]) dexElementsField.get(pathList);

            Class elementClz = dexElements.getClass().getComponentType();
            Object[] newDexElements = (Object[]) Array.newInstance(elementClz, dexElements.length + 1);
            Constructor<?> constructor = elementClz.getConstructor(File.class, boolean.class, File.class, DexFile.class);
            File file = new File(getFilesDir(), "test.dex");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            Object pluginElement = constructor.newInstance(apkFile, false, apkFile, DexFile.loadDex(apkFile.getCanonicalPath(),
                    file.getAbsolutePath(), 0));
            Object[] toAddElementArray = new Object[]{pluginElement};
            System.arraycopy(dexElements, 0, newDexElements, 0, dexElements.length);
            // 插件的那个element复制进去
            System.arraycopy(toAddElementArray, 0, newDexElements, dexElements.length, toAddElementArray.length);
            dexElementsField.set(pathList, newDexElements);

            AssetManager assetManager = getResources().getAssets();
            Method method = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            method.invoke(assetManager, apkFile.getPath());

//            PackageInfo packageInfo = getPackageManager().getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_RECEIVERS);
//            if (packageInfo != null) {
//                for (ActivityInfo info : packageInfo.receivers) {
//                    Log.e(TAG, "loadClassByHostClassLoader: " + info.name );
//
//                }
//            }
            Class packageParseClz = Class.forName("android.content.pm.PackageParser");
            Object packageParser = packageParseClz.newInstance();
            Method parseMethod = packageParseClz.getDeclaredMethod("parsePackage", File.class, int.class);
            parseMethod.setAccessible(true);
            Object packageObject = parseMethod.invoke(packageParser, apkFile, 1 << 2);
            Class packageClz = Class.forName("android.content.pm.PackageParser$Package");
            Field receiversField = packageClz.getDeclaredField("receivers");
            receiversField.setAccessible(true);
            ArrayList receives = (ArrayList) receiversField.get(packageObject);

            Class componentClz = Class.forName("android.content.pm.PackageParser$Component");
            Field intents = componentClz.getDeclaredField("intents");
            intents.setAccessible(true);
            Field classNameField = componentClz.getDeclaredField("className");
            classNameField.setAccessible(true);
            for (int i = 0; i < receives.size(); i++) {
                ArrayList<IntentFilter> intentFilters = (ArrayList<IntentFilter>) intents.get(receives.get(i));
                String className = (String) classNameField.get(receives.get(i));
                registerReceiver((BroadcastReceiver) getClassLoader().loadClass(className).newInstance(), intentFilters.get(0));
            }

            // 安装ContentProvider
            Field providersField = packageClz.getDeclaredField("providers");
            providersField.setAccessible(true);
            ArrayList providers = (ArrayList) providersField.get(packageObject);

            Class providerClz = Class.forName("android.content.pm.PackageParser$Provider");
            Field providerInfoField = providerClz.getDeclaredField("info");
            providersField.setAccessible(true);
            List<ProviderInfo> providerInfos = new ArrayList<>();
            for (int i = 0; i < providers.size(); i++) {
                ProviderInfo providerInfo = (ProviderInfo) providerInfoField.get(providers.get(i));
                providerInfo.applicationInfo = getApplicationInfo();
                providerInfos.add(providerInfo);
            }
            Class contextImplClz = Class.forName("android.app.ContextImpl");
            Field mMainThread = contextImplClz.getDeclaredField("mMainThread");
            mMainThread.setAccessible(true);
            Object activityThread = mMainThread.get(this.getBaseContext());
            Class activityThreadClz = Class.forName("android.app.ActivityThread");
            Method installContentProvidersMethod = activityThreadClz.getDeclaredMethod("installContentProviders", Context.class, List.class);
            installContentProvidersMethod.setAccessible(true);
            installContentProvidersMethod.invoke(activityThread, this, providerInfos);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "loadClassByHostClassLoader: " + e.getMessage());
        }
    }


    private void loadSoPlugin() {
        File apkFile = new File("/sdcard/plugin_so.apk");
        ClassLoader baseClassLoader = this.getClassLoader();
        try {
            Field pathListField = baseClassLoader.getClass().getSuperclass().getDeclaredField("pathList");
            pathListField.setAccessible(true);
            Object pathList = pathListField.get(baseClassLoader);

            Class clz = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = clz.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            Object[] dexElements = (Object[]) dexElementsField.get(pathList);

            Class elementClz = dexElements.getClass().getComponentType();
            Object[] newDexElements = (Object[]) Array.newInstance(elementClz, dexElements.length + 1);
            Constructor<?> constructor = elementClz.getConstructor(File.class, boolean.class, File.class, DexFile.class);
            File file = new File(getFilesDir(), "test.dex");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            Object pluginElement = constructor.newInstance(apkFile, false, apkFile, DexFile.loadDex(apkFile.getCanonicalPath(),
                    file.getAbsolutePath(), 0));
            Object[] toAddElementArray = new Object[]{pluginElement};
            System.arraycopy(dexElements, 0, newDexElements, 0, dexElements.length);
            // 插件的那个element复制进去
            System.arraycopy(toAddElementArray, 0, newDexElements, dexElements.length, toAddElementArray.length);
            dexElementsField.set(pathList, newDexElements);

            AssetManager assetManager = getResources().getAssets();
            Method method = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            method.invoke(assetManager, apkFile.getPath());

            // findNativeLib
            Method findLibMethod = elementClz.getDeclaredMethod("findNativeLibrary",String.class);
            findLibMethod.setAccessible(true);
//            Object soElement = constructor.newInstance(new File("/sdcard/"), true, apkFile, DexFile.loadDex(apkFile.getCanonicalPath(),
//                    file.getAbsolutePath(), 0));
//            findLibMethod.invoke(pluginElement,System.mapLibraryName("native-lib"));
            ZipFile zipFile = new ZipFile(apkFile);
            ZipEntry zipEntry = zipFile.getEntry("lib/armeabi/libnative-lib.so");
            InputStream inputStream = zipFile.getInputStream(zipEntry);
            File outSoFile = new File(getFilesDir(), "libnative-lib.so");
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
            // 构造Element
            Object soElement = constructor.newInstance(getFilesDir(), true, null, null);
//            findLibMethod.invoke(soElement,System.mapLibraryName("native-lib"));

            // 将soElement填充到nativeLibraryPathElements中,
            Field soElementField = clz.getDeclaredField("nativeLibraryPathElements");
            soElementField.setAccessible(true);
            Object[] soElements = (Object[]) dexElementsField.get(pathList);
            Object[] newSoElements = (Object[]) Array.newInstance(elementClz, soElements.length + 1);
            Object[] toAddSoElementArray = new Object[]{soElement};
            System.arraycopy(soElements, 0, newSoElements, 0, soElements.length);
            // 插件的那个element复制进去
            System.arraycopy(toAddSoElementArray, 0, newSoElements, soElements.length, toAddElementArray.length);
            soElementField.set(pathList, newSoElements);

            //将so的文件夹填充到nativeLibraryDirectories中
            Field libDir = clz.getDeclaredField("nativeLibraryDirectories");
            libDir.setAccessible(true);
            List libDirs = (List) libDir.get(pathList);
            libDirs.add(getFilesDir());
            libDir.set(pathList,libDirs);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "loadSoPlugin: " + e.getMessage());
        }

    }


}
