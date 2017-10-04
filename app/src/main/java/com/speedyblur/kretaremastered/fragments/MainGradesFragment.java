package com.speedyblur.kretaremastered.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.MainActivity;
import com.speedyblur.kretaremastered.adapters.StickyDateGradeAdapter;
import com.speedyblur.kretaremastered.adapters.SubjectExpandableGradeAdapter;
import com.speedyblur.kretaremastered.models.Grade;
import com.speedyblur.kretaremastered.models.SubjectGradeGroup;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;
import com.speedyblur.kretaremastered.shared.IDataStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class MainGradesFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_main_grades, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity parent = (MainActivity) getActivity();
        final RecyclerView subjGrouped = (RecyclerView) parent.findViewById(R.id.mainGradeView);
        final StickyListHeadersListView dateListView = (StickyListHeadersListView) parent.findViewById(R.id.datedGradeList);

        DataStore.asyncQuery(parent, parent.p.getCardid(), Common.SQLCRYPT_PWD, new IDataStore<ArrayList<Grade>>() {

            @Override
            public ArrayList<Grade> requestFromStore(DataStore ds) {
                return ds.getGradesData();
            }

            @Override
            public void processRequest(ArrayList<Grade> data) {
                // Date-grouped (ordered) list
                ArrayList<Grade> gradesWithoutEndterm = new ArrayList<>();
                for (int i=0; i<data.size(); i++) {
                    if (!data.get(i).getType().contains("végi") && !data.get(i).getType().contains("Félévi")) gradesWithoutEndterm.add(data.get(i));
                }
                Collections.sort(gradesWithoutEndterm, new Comparator<Grade>() {
                    @Override
                    public int compare(Grade g1, Grade g2) {
                        return g2.getDate() - g1.getDate();
                    }
                });
                dateListView.setAdapter(new StickyDateGradeAdapter(getContext(), gradesWithoutEndterm));
                dateListView.setEmptyView(parent.findViewById(R.id.noGradesView));

                // Subject-grouped list
                // TODO: Improve sorting
                ArrayList<SubjectGradeGroup> subjectGradeGroups = new ArrayList<>();
                for (int i=data.size()-1; i>0; i--) {
                    Grade cGrade = data.get(i);
                    boolean found = false;
                    for (int j=0; j<subjectGradeGroups.size(); j++) {
                        if (subjectGradeGroups.get(j).getSubject().equals(cGrade.getSubject())) {
                            subjectGradeGroups.get(j).addToGrades(cGrade);
                            found = true;
                        }
                    }
                    ArrayList<Grade> gList = new ArrayList<>();
                    gList.add(cGrade);
                    if (!found) subjectGradeGroups.add(new SubjectGradeGroup(cGrade.getSubject(), gList));
                }
                subjGrouped.setHasFixedSize(false);
                subjGrouped.setLayoutManager(new LinearLayoutManager(parent));
                subjGrouped.setAdapter(new SubjectExpandableGradeAdapter(subjectGradeGroups));
            }

            @Override
            public void onDecryptionFailure(DecryptionException e) {

            }
        });

        // Setup viewFlipper
        ((ViewFlipper) parent.findViewById(R.id.gradeOrderFlipper)).setDisplayedChild(0);
    }
}
