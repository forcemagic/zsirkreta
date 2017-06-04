package com.speedyblur.kretaremastered;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class GradeAdapter extends ArrayAdapter<Grade> {
    GradeAdapter(Context context, ArrayList<Grade> grades) {
        super(context, 0, grades);
    }

    @NonNull
    @Override
    public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
        Grade grade = getItem(pos);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grades_table, parent, false);
        }
        TextView gradeView = (TextView) convertView.findViewById(R.id.grade);
        TextView subjView = (TextView) convertView.findViewById(R.id.subject);
        TextView dateGotView = (TextView) convertView.findViewById(R.id.dateOfGrade);
        TextView teacherView = (TextView) convertView.findViewById(R.id.teacher);

        StringBuilder res = new StringBuilder();
        String[] strArr = grade.subject.split(" ");
        for (String str : strArr) {
            str = str.trim();
            if (!str.equalsIgnoreCase("és")) {
                char[] stringArray = str.toCharArray();
                stringArray[0] = Character.toUpperCase(stringArray[0]);
                str = new String(stringArray);
            }
            res.append(str).append(" ");
        }

        gradeView.setText(grade.grade);
        subjView.setText(res.toString().trim());
        dateGotView.setText(grade.gotDate);
        teacherView.setText(grade.teacher);

        return convertView;
    }
}
