package com.speedyblur.kretaremastered.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.MainActivity;
import com.speedyblur.kretaremastered.adapters.AverageAdapter;
import com.speedyblur.kretaremastered.models.Average;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;
import com.speedyblur.kretaremastered.shared.GradeSeparatorDecoration;
import com.speedyblur.kretaremastered.shared.IDataStore;

import java.util.ArrayList;

public class MainAveragesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_main_averages, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity parent = (MainActivity) getActivity();
        final RecyclerView avgList = (RecyclerView) parent.findViewById(R.id.averageList);

        DataStore.asyncQuery(parent, parent.p.getCardid(), Common.SQLCRYPT_PWD, new IDataStore<ArrayList<Average>>() {

            @Override
            public ArrayList<Average> requestFromStore(DataStore ds) {
                return ds.getAveragesData();
            }

            @Override
            public void processRequest(ArrayList<Average> data) {
                avgList.setAdapter(new AverageAdapter(data, parent.p.getCardid()));
            }

            @Override
            public void onDecryptionFailure(DecryptionException e) {
                e.printStackTrace();
            }
        });

        avgList.addItemDecoration(new GradeSeparatorDecoration(getContext()));
        avgList.setLayoutManager(new LinearLayoutManager(getContext()));
        avgList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                parent.setSwipeRefreshEnabled(!recyclerView.canScrollVertically(-1));
            }
        });
    }
}
