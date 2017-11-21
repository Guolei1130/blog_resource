package com;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.databinding.BindingAdapter;
import android.databinding.BindingMethods;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 17/11/21
 * Time: 下午2:27
 * Desc:
 */
@BindingMethods({

})
public class CheckBoxBindingAdapter {

    @BindingAdapter("android:checked")
    public static void setChecked(CheckBox box, boolean checked) {
        if (box.isChecked() == checked) {
            return;
        }
        box.setChecked(checked);
    }

    @InverseBindingAdapter(attribute = "android:checked",event = "checkChangeListener")
    public static boolean getChecked(CheckBox box) {
        return box.isChecked();
    }

    @BindingAdapter(value = {"checkChangeListener"}, requireAll = false)
    public static void setCheckChangeListener(CheckBox box, final InverseBindingListener checkChangeListener) {
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkChangeListener != null) {
                    checkChangeListener.onChange();
                }
            }
        });
    }
}
