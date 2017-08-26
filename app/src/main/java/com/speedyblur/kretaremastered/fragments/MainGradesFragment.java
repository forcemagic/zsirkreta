package com.speedyblur.kretaremastered.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.MainActivity;
import com.speedyblur.kretaremastered.adapters.SubjectExpandableGradeAdapter;
import com.speedyblur.kretaremastered.models.Grade;
import com.speedyblur.kretaremastered.models.SubjectGradeGroup;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;
import com.speedyblur.kretaremastered.shared.Vars;

import java.util.ArrayList;

public class MainGradesFragment extends Fragment {
    private ArrayList<Grade> grades;
    private MainActivity parent = (MainActivity) getActivity();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflated = inflater.inflate(R.layout.frag_main_grades, container, false);

        // TODO: HttpHandler -> Callback -> parent.runOnUiThread(...)
        // TODO: Implement loading screen
        ExpandableListView expListView = inflated.findViewById(R.id.mainGradeView);

        // Compose lists
        ArrayList<Grade> grades = new ArrayList<>();
        try {
            DataStore ds = new DataStore(getContext(), parent.p.getCardid(), Vars.SQLCRYPT_PWD);
            grades = ds.getGradesData();
            ds.close();
        } catch (DecryptionException e) {
            e.printStackTrace();
        }

        ArrayList<SubjectGradeGroup> subjectGradeGroups = new ArrayList<>();
        for (int i=0; i<grades.size(); i++) {
            // TODO: Group by grades
        }

        expListView.setAdapter(new SubjectExpandableGradeAdapter(getContext(), subjectGradeGroups));

        return inflated;
    }
}
