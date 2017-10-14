package com.speedyblur.kretaremastered.shared;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.speedyblur.kretaremastered.R;

public class GradeSeparatorDecoration extends RecyclerView.ItemDecoration {
    private final Drawable mDivider;

    public GradeSeparatorDecoration(Context ctxt) {
        mDivider = ContextCompat.getDrawable(ctxt, R.drawable.unified_separator);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        int dividerHeight = mDivider.getIntrinsicHeight();

        for (int i = 1; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int ty = (int)(child.getTranslationY() + 0.5f);
            int top = child.getTop() - params.topMargin + ty;
            int bottom = top + dividerHeight;
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
