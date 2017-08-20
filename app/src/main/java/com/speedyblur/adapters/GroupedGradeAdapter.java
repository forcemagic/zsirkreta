package com.speedyblur.adapters;

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
import com.speedyblur.models.Grade;
import com.speedyblur.models.GradeGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GroupedGradeAdapter extends BaseExpandableListAdapter {

    private ArrayList<GradeGroup> gradeGroups;
    private LayoutInflater inflater;
    private Context context;
    private GradeGroup.FormatHelper fHelper;

    public GroupedGradeAdapter(Context context, ArrayList<Grade> allGrades, String groupCrieria,
                               GradeGroup.FormatHelper fHelper, GradeGroup.SameGroupComparator comp) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        try {
            this.gradeGroups = GradeGroup.assembleGroups(allGrades, groupCrieria, comp);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        this.fHelper = fHelper;
    }

    @Override
    public int getGroupCount() {
        return gradeGroups.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return gradeGroups.get(i).grades.size();
    }

    @Override
    public GradeGroup getGroup(int i) {
        return gradeGroups.get(i);
    }

    @Override
    public Grade getChild(int i, int i1) {
        return gradeGroups.get(i).grades.get(i1);
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

        GradeGroup datedGrade = getGroup(i);
        TextView subjNameView = convertView.findViewById(R.id.gradeGroupTitle);
        subjNameView.setText(datedGrade.getFormattedId(fHelper));

        return convertView;
    }

    @Override
    @SuppressLint("inflateparams")
    public View getChildView(int i, int i1, boolean isLastChild, View convertView, ViewGroup viewGroup) {
        Grade gradeObj = getChild(i, i1);

        if (gradeObj.type.contains("végi") || gradeObj.type.contains("Félévi")) {
            convertView = inflater.inflate(R.layout.gradelist_importantitem, null);
        } else {
            convertView = inflater.inflate(R.layout.gradelist_item, null);
        }

        // Common things
        ImageView gradeBullet = convertView.findViewById(R.id.gradeBullet);
        TextView gradeView = convertView.findViewById(R.id.grade);
        TextView titleView = convertView.findViewById(R.id.gradeTitle);

        gradeView.setText(gradeObj.grade);
        gradeBullet.setColorFilter(ContextCompat.getColor(context, gradeObj.colorId), PorterDuff.Mode.SRC_ATOP);

        // Not common things
        if (gradeObj.type.contains("Félévi")) {
            titleView.setText(R.string.grade_end_of_halfterm);
        } else if (gradeObj.type.contains("végi")) {
            titleView.setText(R.string.grade_end_of_year);
        } else {
            titleView.setText(capitalize(gradeObj.type));

            TextView descView1 = convertView.findViewById(R.id.gradeDesc);
            TextView descView2 = convertView.findViewById(R.id.gradeDesc2);

            if (gradeObj.theme.equals(" - ")) {
                descView1.setText(gradeObj.teacher);
            } else {
                descView1.setText(capitalize(gradeObj.theme) + " - " + gradeObj.teacher);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. M. d.", Locale.getDefault());
            descView2.setText(dateFormat.format(new Date((long)gradeObj.gotDate*1000)));
        }

        if (i1 == 0) {
            convertView.findViewById(R.id.gradeBarTop).setVisibility(View.INVISIBLE);
        }

        if (isLastChild) {
            convertView.findViewById(R.id.gradeBarBottom).setVisibility(View.INVISIBLE);
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
