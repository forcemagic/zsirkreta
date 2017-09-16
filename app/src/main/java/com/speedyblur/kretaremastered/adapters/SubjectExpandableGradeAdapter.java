package com.speedyblur.kretaremastered.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Grade;
import com.speedyblur.kretaremastered.models.SubjectGradeGroup;
import com.speedyblur.kretaremastered.shared.Common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SubjectExpandableGradeAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<SubjectGradeGroup> subjectGradeGroups;

    public SubjectExpandableGradeAdapter(Context context, ArrayList<SubjectGradeGroup> subjectGradeGroups) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.subjectGradeGroups = subjectGradeGroups;
    }

    @Override
    public int getGroupCount() {
        return subjectGradeGroups.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return subjectGradeGroups.get(i).getGrades().size();
    }

    @Override
    public SubjectGradeGroup getGroup(int i) {
        return subjectGradeGroups.get(i);
    }

    @Override
    public Grade getChild(int i, int i1) {
        return subjectGradeGroups.get(i).getGrades().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    @SuppressLint("inflateparams")
    public View getGroupView(int i, boolean b, View convertView, ViewGroup viewGroup) {
        convertView = inflater.inflate(R.layout.gradegroup_item, null);

        SubjectGradeGroup subjectGradeGroup = getGroup(i);
        TextView subjNameView = convertView.findViewById(R.id.gradeGroupTitle);
        subjNameView.setText(Common.getLocalizedSubjectName(context, subjectGradeGroup.getSubject()));

        return convertView;
    }

    @Override
    @SuppressLint("inflateparams")
    public View getChildView(int i, int i1, boolean isLastChild, View convertView, ViewGroup viewGroup) {
        Grade gradeObj = getChild(i, i1);

        if (gradeObj.getType().contains("végi") || gradeObj.getType().contains("Félévi")) {
            convertView = inflater.inflate(R.layout.gradelist_importantitem, null);
        } else {
            convertView = inflater.inflate(R.layout.gradelist_item, null);
        }

        // Common things
        ImageView gradeBullet = convertView.findViewById(R.id.gradeBullet);
        TextView gradeView = convertView.findViewById(R.id.grade);
        TextView titleView = convertView.findViewById(R.id.gradeTitle);

        gradeView.setText(String.valueOf(gradeObj.getGrade()));
        gradeBullet.setColorFilter(ContextCompat.getColor(context, gradeObj.getColorId()), PorterDuff.Mode.SRC_ATOP);

        // Not common things
        if (gradeObj.getType().contains("Félévi")) {
            titleView.setText(R.string.grade_end_of_halfterm);
        } else if (gradeObj.getType().contains("végi")) {
            titleView.setText(R.string.grade_end_of_year);
        } else {
            titleView.setText(capitalize(gradeObj.getType()));

            TextView descView1 = convertView.findViewById(R.id.gradeDesc);
            // TODO: Remove this from the layout
            convertView.findViewById(R.id.gradeDesc2).setVisibility(View.GONE);

            if (gradeObj.getTheme().equals(" - ")) {
                descView1.setText(gradeObj.getTeacher());
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. M. d.", Locale.getDefault());
                descView1.setText(capitalize(gradeObj.getTheme()) + " - " + dateFormat.format(new Date((long)gradeObj.getDate()*1000)));
            }
        }

        return convertView;
    }

    /**
     *  Helper function to capitalize first letter of string
     */
    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}
