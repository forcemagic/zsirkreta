package com.speedyblur.kretaremastered.models;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.speedyblur.kretaremastered.shared.Vars;


public class SlowScroller extends Scroller {

    public SlowScroller(Context ctxt) {
        super(ctxt);
    }

    public SlowScroller(Context ctxt, Interpolator intpol) {
        super(ctxt, intpol);
    }

    public SlowScroller(Context ctxt, Interpolator intpol, boolean flywheel) {
        super(ctxt, intpol, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, Vars.VIEWPAGER_SCROLLDUR);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int dur) {
        super.startScroll(startX, startY, dx, dy, Vars.VIEWPAGER_SCROLLDUR);
    }

}
