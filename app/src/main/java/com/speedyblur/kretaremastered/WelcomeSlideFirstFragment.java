package com.speedyblur.kretaremastered;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WelcomeSlideFirstFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater infl, ViewGroup cont, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) infl.inflate(R.layout.frag_welcome_first, cont, false);
        return rootView;
    }
}