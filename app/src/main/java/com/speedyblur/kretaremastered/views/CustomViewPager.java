package com.speedyblur.kretaremastered.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import com.speedyblur.kretaremastered.models.SlowScroller;

import java.lang.reflect.Field;

public class CustomViewPager extends ViewPager {

    private boolean isPagingEnabled = true;

    public CustomViewPager(Context context) {
        super(context);
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            SlowScroller slScroller = new SlowScroller(context, new DecelerateInterpolator());
            mScroller.set(this, slScroller);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            SlowScroller slScroller = new SlowScroller(context, new DecelerateInterpolator());
            mScroller.set(this, slScroller);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        return this.isPagingEnabled && super.onTouchEvent(evt);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent evt) {
        return this.isPagingEnabled && super.onInterceptTouchEvent(evt);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }
}
