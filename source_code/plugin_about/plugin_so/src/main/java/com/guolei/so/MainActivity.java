package com.guolei.so;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.guolei.plugin_so.R;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin);
        ((TextView)findViewById(R.id.plugin_so_text)).setText(stringFromJNI());
    }

    public native String stringFromJNI();
}
