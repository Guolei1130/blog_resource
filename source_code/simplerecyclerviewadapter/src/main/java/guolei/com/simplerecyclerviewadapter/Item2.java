package guolei.com.simplerecyclerviewadapter;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.view.View;
import android.widget.TextView;

import simpleadapter.BaseItem;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 17/11/21
 * Time: 下午11:01
 * Desc:
 */
public class Item2 extends BaseItem {


    @Override
    public int getLayoutId() {
        return R.layout.item_2;
    }

    @Override
    public void bindValue(View view) {
        ((TextView)view.findViewById(R.id.tv)).setText("item 2");
    }
}
