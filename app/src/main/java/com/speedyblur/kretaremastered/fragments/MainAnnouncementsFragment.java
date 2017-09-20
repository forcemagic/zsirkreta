package com.speedyblur.kretaremastered.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.MainActivity;
import com.speedyblur.kretaremastered.adapters.AnnouncementAdapter;
import com.speedyblur.kretaremastered.models.Announcement;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;

import java.util.ArrayList;

public class MainAnnouncementsFragment extends Fragment {
    private ArrayList<Announcement> announcements;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_main_announcements, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity parent = (MainActivity) getActivity();

        // Fetching DataStore
        try {
            DataStore ds = new DataStore(getContext(), parent.p.getCardid(), Common.SQLCRYPT_PWD);
            announcements = ds.getAnnouncementsData();
            ds.close();
        } catch (DecryptionException e) {e.printStackTrace();}

        ListView aList = (ListView) parent.findViewById(R.id.announcementsList);
        aList.setEmptyView(parent.findViewById(R.id.announcementsEmptyView));
        aList.setAdapter(new AnnouncementAdapter(getContext(), announcements));
    }
}
