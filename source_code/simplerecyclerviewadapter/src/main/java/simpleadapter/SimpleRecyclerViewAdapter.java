package simpleadapter;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 17/11/21
 * Time: 下午10:45
 * Desc:
 */
public class SimpleRecyclerViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<BaseItem> mData;
    private LayoutInflater mInflater;

    public SimpleRecyclerViewAdapter(List<BaseItem> data) {
        mData = data;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        return new BaseViewHolder(mInflater.inflate(viewType, parent, false));
    }


    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        mData.get(position).bindValue(holder.getView());
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getLayoutId();
    }
}
