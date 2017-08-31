package com.speedyblur.kretaremastered.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.AverageGraphActivity;
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

        ListView avgList = (ListView) parent.findViewById(R.id.averageList);
        avgList.setAdapter(new AverageAdapter(getContext(), averages));
        avgList.setEmptyView(parent.findViewById(R.id.noAveragesView));
        avgList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Average avg = (Average) adapterView.getItemAtPosition(i);

                Intent it = new Intent(getContext(), AverageGraphActivity.class);
                it.putExtra("profileName", ((MainActivity)getContext()).p.getCardid());
                it.putExtra("subject", avg.getSubject());
                getContext().startActivity(it);
            }
        });
    }
}
