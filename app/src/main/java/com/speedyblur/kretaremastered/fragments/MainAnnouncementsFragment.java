package com.speedyblur.kretaremastered.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.MainActivity;
import com.speedyblur.kretaremastered.adapters.AnnouncementAdapter;
import com.speedyblur.kretaremastered.models.Announcement;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainAnnouncementsFragment extends Fragment {
    private ArrayList<Announcement> announcements;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_main_announcements, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity parent = (MainActivity) getActivity();

        // Fetching DataStore
        try {
            DataStore ds = new DataStore(getContext(), parent.p.getCardid(), Common.SQLCRYPT_PWD);
            announcements = ds.getAnnouncementsData();
            ds.close();
        } catch (DecryptionException e) {e.printStackTrace();}

        final ListView aList = (ListView) parent.findViewById(R.id.announcementsList);
        aList.setEmptyView(parent.findViewById(R.id.announcementsEmptyView));
        aList.setAdapter(new AnnouncementAdapter(getContext(), announcements));
        aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Announcement a = (Announcement) adapterView.getItemAtPosition(i);
                if (!a.isSeen()) {
                    try {
                        DataStore ds = new DataStore(getContext(), parent.p.getCardid(), Common.SQLCRYPT_PWD);
                        ArrayList<Announcement> toUpsert = new ArrayList<>();
                        toUpsert.add(new Announcement(a.getTeacher(), a.getContent(), a.getDate(), true));
                        ds.upsertAnnouncementsData(toUpsert, true);
                        ds.close();
                    } catch (DecryptionException e) {e.printStackTrace();}
                }
                View dialView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_announcement_details, null);

                TextView mAuthor = dialView.findViewById(R.id.announcementInfoAuthor);
                TextView mDate = dialView.findViewById(R.id.announcementInfoDate);
                TextView mContent = dialView.findViewById(R.id.announcementInfoContent);

                mAuthor.setText(a.getTeacher());
                mDate.setText(new SimpleDateFormat("yyyy. MMM. dd.", Locale.getDefault()).format(new Date((long) a.getDate()*1000)));
                mContent.setText(a.getContent());

                new AlertDialog.Builder(getContext()).setView(dialView)
                        .setPositiveButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (!a.isSeen()) {
                                    try {
                                        DataStore ds = new DataStore(getContext(), parent.p.getCardid(), Common.SQLCRYPT_PWD);
                                        announcements = ds.getAnnouncementsData();
                                        ds.close();
                                    } catch (DecryptionException e) {e.printStackTrace();}
                                    aList.setAdapter(new AnnouncementAdapter(getContext(), announcements));
                                }

                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });
    }
}
