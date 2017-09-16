package com.speedyblur.kretaremastered.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
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

        MainActivity parent = (MainActivity) getActivity();

        // Grades list
        ArrayList<Grade> grades = new ArrayList<>();
        try {
            DataStore ds = new DataStore(getContext(), parent.p.getCardid(), Common.SQLCRYPT_PWD);
            grades = ds.getGradesData();
            ds.close();
        } catch (DecryptionException e) {e.printStackTrace();}

        // Subject-grouped list
        ExpandableListView expListView = (ExpandableListView) parent.findViewById(R.id.mainGradeView);
        ArrayList<SubjectGradeGroup> subjectGradeGroups = new ArrayList<>();
        for (int i=0; i<grades.size(); i++) {
            Grade cGrade = grades.get(i);
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
        expListView.setChildDivider(null);
        expListView.setDividerHeight(0);
        expListView.setAdapter(new SubjectExpandableGradeAdapter(getContext(), subjectGradeGroups));
        expListView.setEmptyView(parent.findViewById(R.id.noGradesView));

        // Date-grouped (ordered) list
        StickyListHeadersListView dateListView = (StickyListHeadersListView) parent.findViewById(R.id.datedGradeList);
        ArrayList<Grade> gradesWithoutEndterm = new ArrayList<>();
        for (int i=0; i<grades.size(); i++) {
            if (!grades.get(i).getType().contains("végi") && !grades.get(i).getType().contains("Félévi")) gradesWithoutEndterm.add(grades.get(i));
        }
        Collections.sort(gradesWithoutEndterm, new Comparator<Grade>() {
            @Override
            public int compare(Grade g1, Grade g2) {
                return g2.getDate() - g1.getDate();
            }
        });
        dateListView.setAdapter(new StickyDateGradeAdapter(getContext(), gradesWithoutEndterm));
        dateListView.setEmptyView(parent.findViewById(R.id.noGradesView));

        // Setup viewFlipper
        ((ViewFlipper) parent.findViewById(R.id.gradeOrderFlipper)).setDisplayedChild(0);
    }
}
