package com.speedyblur.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.models.Grade;
import com.speedyblur.models.Subject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SubjectAdapter extends BaseExpandableListAdapter {

    private final ArrayList<Subject> subjects;
    private final LayoutInflater inflater;

    public SubjectAdapter(Context context, ArrayList<Subject> subjs) {
        this.inflater = LayoutInflater.from(context);
        this.subjects = subjs;
    }

    @Override
    public int getGroupCount() {
        return subjects.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return subjects.get(groupPosition).grades.size();
    }

    @Override
    public Subject getGroup(int groupPosition) {
        return subjects.get(groupPosition);
    }

    @Override
    public Grade getChild(int groupPosition, int childPosition) {
        return subjects.get(groupPosition).grades.get(childPosition);
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, final ViewGroup parent) {
        convertView = inflater.inflate(R.layout.subjlist_item, null);

        Subject subjObj = getGroup(groupPosition);

        TextView subjNameView = (TextView) convertView.findViewById(R.id.subject);
        TextView avgView = (TextView) convertView.findViewById(R.id.avg);

        Resources resx = convertView.getResources();
        int resxid = resx.getIdentifier("subject_"+subjObj.name, "string", convertView.getContext().getPackageName());
        String outpName = resxid == 0 ? subjObj.name : resx.getString(resxid);
        subjNameView.setText(outpName);
        avgView.setText(convertView.getResources().getString(R.string.subjview_average, subjObj.avg));

        return convertView;
    }

    @SuppressLint("InflateParams")
    @SuppressWarnings("deprecation")
    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
        Grade gradeObj = getChild(groupPosition, childPosition);

        if (gradeObj.type.contains("végi") || gradeObj.type.contains("Félévi")) {
            convertView = inflater.inflate(R.layout.gradelist_importantitem, null);
        } else {
            convertView = inflater.inflate(R.layout.gradelist_item, null);
        }

        // Common things
        TextView gradeView = convertView.findViewById(R.id.grade);
        TextView titleView = convertView.findViewById(R.id.gradeTitle);

        // Set bg color
        GradientDrawable grDrawable = (GradientDrawable) gradeView.getBackground();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            grDrawable.setColor(convertView.getResources().getColor(gradeObj.colorId, null));
        } else {
            grDrawable.setColor(convertView.getResources().getColor(gradeObj.colorId));
        }

        gradeView.setText(gradeObj.grade);

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

            SimpleDateFormat dateFormat = new SimpleDateFormat("Y. m. d.", Locale.getDefault());
            descView2.setText(dateFormat.format(new Date((long)gradeObj.gotDate*1000)));
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
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
