package com.guolei.plugin_1;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/3/11
 * Time: 下午3:49
 * Desc:
 */
public class People {

    private String mName;
    private int mAge;

    public People(String name, int age) {
        mName = name;
        mAge = age;
    }

    @Override
    public String toString() {
        return "from plugin_1:[mName =" + mName + ";mAge=" + mAge + "]";
    }
}
