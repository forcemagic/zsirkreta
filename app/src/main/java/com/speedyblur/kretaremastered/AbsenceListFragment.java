package com.speedyblur.kretaremastered;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.speedyblur.adapters.AbsenceAdapter;
import com.speedyblur.models.Absence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class AbsenceListFragment extends Fragment {

    private ArrayList<Absence> currentAbsences;

    public AbsenceListFragment() {
        currentAbsences = getArguments().getParcelableArrayList("absences");
    }

    @Override
    public View onCreateView(LayoutInflater infl, ViewGroup cont, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) infl.inflate(R.layout.frag_absence_list, cont, false);

        ListView lv = rootView.findViewById(R.id.absenceList);
        lv.setAdapter(new AbsenceAdapter(getContext(), currentAbsences));

        return rootView;
    }
}
