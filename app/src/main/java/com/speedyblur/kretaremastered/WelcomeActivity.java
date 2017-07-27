package com.speedyblur.kretaremastered;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.speedyblur.models.CustomViewPager;

public class WelcomeActivity extends AppCompatActivity {

    private CustomViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mPager = (CustomViewPager) findViewById(R.id.welcomePager);
        mPager.setPagingEnabled(false);

        PagerAdapter mPgAdapter = new WelcomeSlidePageAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPgAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    public void goToNext(View v) {
        if (mPager.getCurrentItem() != 2) {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
        }
    }

    private class WelcomeSlidePageAdapter extends FragmentStatePagerAdapter {
        public WelcomeSlidePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            if (pos == 0) {
                return new WelcomeSlideFirstFragment();
            } else {
                // TODO: Add more views
                return new WelcomeSlideFirstFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
