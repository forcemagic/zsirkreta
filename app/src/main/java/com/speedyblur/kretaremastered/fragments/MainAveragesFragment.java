package com.speedyblur.kretaremastered.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.MainActivity;
import com.speedyblur.kretaremastered.models.Average;

import java.util.ArrayList;

public class MainAveragesFragment extends Fragment {
    private ArrayList<Average> averages;
    private MainActivity parent = (MainActivity) getActivity();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflated = inflater.inflate(R.layout.frag_main_averages, container, false);



        return inflated;
    }
}
