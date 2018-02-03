package com.guolei.nestedscroll;

//                    _    _   _ _
//__      _____  _ __| | _| |_(_) | ___
//\ \ /\ / / _ \| '__| |/ / __| | |/ _ \
// \ V  V / (_) | |  |   <| |_| | |  __/
//  \_/\_/ \___/|_|  |_|\_\\__|_|_|\___|


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.guolei.Constants;
import com.guolei.execption.CustomException;

/**
 * Copyright © 2013-2017 Worktile. All Rights Reserved.
 * Author: guolei
 * Email: 1120832563@qq.com
 * Date: 18/1/30
 * Time: 下午10:19
 * Desc:
 */
public class StickNavLayout extends LinearLayout implements NestedScrollingParent {

    private int mTopHeight;

    private Scroller mScroller;
    private View mTopView;
    private boolean up = false;

    private int mLastY;

    public StickNavLayout(Context context) {
        this(context, null);
    }

    public StickNavLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickNavLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) +
                        getChildAt(0).getLayoutParams().height,
                MeasureSpec.getMode(heightMeasureSpec));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getChildCount() < 2) {
            throw new CustomException("StickNavLayout must have two child view");
        }
        mTopView = getChildAt(0);
        mTopHeight = mTopView.getMeasuredHeight();
    }


    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(View child) {
        super.onStopNestedScroll(child);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0) {
            //上滑
            if (getScrollY() >= mTopHeight) {
                super.onNestedPreScroll(target, dx, dy, consumed);
            } else {
                int needConsumed = mTopHeight - getScrollY() > dy ? dy : mTopHeight - getScrollY();
                scrollBy(0, needConsumed);
                consumed[1] = needConsumed;
            }
        } else {
            //下滑
            if (getScrollY() > 0) {
                int needConsumed = getScrollY() > -dy ? dy : -getScrollY();
                scrollBy(0, needConsumed);
                consumed[1] = needConsumed;
            } else {
                super.onNestedPreScroll(target, dx, dy, consumed);
            }
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.e(Constants.TAG, "onNestedFling: " + velocityY);
        mScroller.forceFinished(true);
        if (velocityY > 0) {
            // 上滑。
            mScroller.fling(0, getScrollY(), (int) velocityX, (int) velocityY,
                    0, 0, 0, mTopHeight * 3);
        } else {
            // 下滑。
            Log.e(Constants.TAG, "onNestedFling: " + getScrollY());
            mScroller.fling(0, getScrollY(), (int) velocityX, (int) velocityY,
                    0, 0, 0, mTopHeight * 3);
        }
        up = velocityY > 0;
        return true;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        //不拦截子控件的fling事件
        return false;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            Log.e(Constants.TAG, "computeScroll: " + y);
            int targetY;
            if (up) {
                if (getScrollY() == mTopHeight) return;
                targetY = y > 600 ? 600 : y;
            } else {
                if (getScrollY() <= 0) return;
                targetY = y < 0 ? 0 : y;
            }
            scrollTo(0, targetY);
            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        mLastY = (int) ev.getY();
        boolean intercept = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                intercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                intercept = ev.getY() <= mTopHeight;
                break;

        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = (int) (event.getY() - mLastY);
                if (dy > 0 && getScrollY() >= 0) {
                    //下滑,做矫正
                    scrollBy(0, -dy);
                } else if (dy < 0 && getScrollY() < 600) {
                    //上滑，同样需要做矫正
                    scrollBy(0, -dy);
                }
                mLastY = (int) event.getY();
                break;
        }
        return true;
    }
}
