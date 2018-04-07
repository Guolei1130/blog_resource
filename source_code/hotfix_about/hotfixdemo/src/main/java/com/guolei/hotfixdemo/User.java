package com.guolei.hotfixdemo;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import java.util.Locale;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/4/2
 * Time: 上午11:02
 * Desc:
 */
public class User {

    private String mUserName;
    private int mAge;

    public User(String userName, int age) {
        mUserName = userName;
        mAge = age;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "origin:[username:%s,age:%d]", mUserName, mAge);
    }
}
