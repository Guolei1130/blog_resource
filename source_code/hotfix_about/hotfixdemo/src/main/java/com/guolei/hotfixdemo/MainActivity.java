package com.guolei.hotfixdemo;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tmall.wireless.jandfix.MethodReplaceProxy;
import com.tmall.wireless.jandfix.MethodSizeUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "hotfix";

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    private Object $change;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences("hotfix", 0);
        mEditor = mSharedPreferences.edit();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        findViewById(R.id.log_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User("guolei", 24);
                Log.e(TAG, "onClick: " + user.toString());
            }
        });

        findViewById(R.id.load_fix).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFix();
            }
        });
        final Button changeTextButton = findViewById(R.id.change_text);
        changeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collection<Activity> activities = new ArrayList<>();
                activities.add(MainActivity.this);
                PatchResource.pathResource(MainActivity.this, "/sdcard/resource_fix.apk", activities);
                changeTextButton.setText(R.string.app_button_text);
            }
        });

        final Button enableSoFix = findViewById(R.id.enable_so_fix);
        final boolean enable = mSharedPreferences.getBoolean("enable_so", false);
        enableSoFix.setText("是否允许SO热修复" + enable);
        enableSoFix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.putBoolean("enable_so", !enable);
                mEditor.commit();
                enableSoFix.setText("是否允许SO热修复" + !enable);
            }
        });
        ((TextView) findViewById(R.id.from_jni)).setText(stringFromJNI());

        findViewById(R.id.enable_instant_run).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableInstantRun();
            }
        });

        findViewById(R.id.instant_run_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast();
            }
        });

        findViewById(R.id.struct_replace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceWithStruct();
            }
        });
        findViewById(R.id.test_struct_replace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new com.guolei.hotfixdemo.Log().log();
            }
        });
        findViewById(R.id.so_diff_patch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = ContextCompat.getDataDir(MainActivity.this);
                File libFile = new File(file.getAbsolutePath(),"lib");
                if (libFile.exists() && libFile.isDirectory()) {
                    File[] files = libFile.listFiles();
                    for (File f :
                            files) {
                        Log.e(TAG, "onClick: " + f.getAbsolutePath() );
                        Log.e(TAG, "onClick: " + f.canWrite() );
                    }

                }
            }
        });

    }

    /**
     * 基于方法体修改的方案
     */
    private void replaceWithStruct() {
        try {
            Method src = com.guolei.hotfixdemo.Log.class.getDeclaredMethod("log");
            Method des = LogFix.class.getDeclaredMethod("log");
            long[] addr = ReplaceUtil.getAddr(src, des);
            replace(addr[0], addr[1], MethodSizeUtils.methodSize());
//            MethodReplaceProxy.instance().replace(src,des);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast() {
        if ($change != null) {
            try {
                Method method = $change.getClass().getDeclaredMethod("showToast");
                method.setAccessible(true);
                method.invoke($change);
            } catch (Exception e) {
                Toast.makeText(this, "instant run方案测试失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "这是在测试instant run 方案", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableInstantRun() {
        //为了简单，就不修改ClassLoader了，直接用一个新的ClassLoader去加载。
        String dexPath = new File("/sdcard/instant_run.jar").getPath();
        File dexOptOutDir = new File(getFilesDir(), "dexopt");
        if (!dexOptOutDir.exists()) {
            boolean result = dexOptOutDir.mkdir();
            if (!result) {
                Log.e(TAG, "loadExtJar: create out dir error");
            }
        }
        String dexOptOutDr = dexOptOutDir.getPath();

        DexClassLoader dexClassLoader = new DexClassLoader(dexPath, dexOptOutDr, null, ClassLoader.getSystemClassLoader());
        try {
            $change = dexClassLoader.loadClass("com.guolei.hotfixdemo.MainActivity$override").getConstructor(Object.class)
                    .newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFix() {
        File apkFile = new File("/sdcard/hotfix_code.apk");
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
            System.arraycopy(toAddElementArray, 0, newDexElements, 0, toAddElementArray.length);
            System.arraycopy(dexElements, 0, newDexElements, toAddElementArray.length, dexElements.length);
            dexElementsField.set(pathList, newDexElements);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public native String stringFromJNI();

    public native void replace(long src, long des, int size);
}
