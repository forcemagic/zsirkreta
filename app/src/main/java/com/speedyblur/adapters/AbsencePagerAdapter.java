package com.speedyblur.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.speedyblur.kretaremastered.AbsenceListFragment;
import com.speedyblur.models.Absence;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class AbsencePagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Absence> absences;

    public AbsencePagerAdapter(FragmentManager fm, ArrayList<Absence> allAbsences) {
        super(fm);
        absences = allAbsences;

    }

    @Override
    public Fragment getItem(int pos) {
        AbsenceListFragment frag = new AbsenceListFragment();
        Bundle bundle = new Bundle();

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis((long) pos*1000);

        ArrayList<Absence> listElements = new ArrayList<>();
        for (int i=0; i<absences.size(); i++) {
            Calendar toCompare = Calendar.getInstance();
            toCompare.setTimeInMillis((long)absences.get(i).date*1000);
            if (toCompare.get(Calendar.DAY_OF_YEAR) == c.get(Calendar.DAY_OF_YEAR)) listElements.add(absences.get(i));
        }
        Collections.sort(listElements, new Comparator<Absence>() {
            @Override
            public int compare(Absence t1, Absence t2) {
                return t1.classNum - t2.classNum;
            }
        });

        bundle.putParcelableArrayList("absences", listElements);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public int getCount() {
        return 10000000;
    }
}
