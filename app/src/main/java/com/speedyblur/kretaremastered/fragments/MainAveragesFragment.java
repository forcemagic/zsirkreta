package com.speedyblur.kretaremastered.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.MainActivity;
import com.speedyblur.kretaremastered.adapters.AverageAdapter;
import com.speedyblur.kretaremastered.models.Average;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;

import java.util.ArrayList;

public class MainAveragesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_main_averages, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity parent = (MainActivity) getActivity();

        // Averages list
        ArrayList<Average> averages = new ArrayList<>();
        try {
            DataStore ds = new DataStore(getContext(), parent.p.getCardid(), Common.SQLCRYPT_PWD);
            averages = ds.getAveragesData();
            ds.close();
        } catch (DecryptionException e) {e.printStackTrace();}

        ExpandableListView avgList = (ExpandableListView) parent.findViewById(R.id.averageList);
        avgList.setAdapter(new AverageAdapter(getContext(), averages, parent.p.getCardid(), avgList));
        avgList.setEmptyView(parent.findViewById(R.id.noAveragesView));
    }
}
