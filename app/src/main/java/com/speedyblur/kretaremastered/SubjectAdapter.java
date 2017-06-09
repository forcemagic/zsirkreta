package com.speedyblur.kretaremastered;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

class SubjectAdapter extends BaseExpandableListAdapter {

    private final ArrayList<Subject> subjects;
    private final LayoutInflater inflater;

    SubjectAdapter(Context context, ArrayList<Subject> subjs) {
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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, final ViewGroup parent) {
        //if (convertView == null) {
            convertView = inflater.inflate(R.layout.subjlist_item, null);

            Subject subjObj = getGroup(groupPosition);

            TextView subjNameView = (TextView) convertView.findViewById(R.id.subject);
            TextView avgView = (TextView) convertView.findViewById(R.id.avg);

            Resources resx = convertView.getResources();
            int resxid = resx.getIdentifier("subject_"+subjObj.name, "string", convertView.getContext().getPackageName());
            String outpName = resxid == 0 ? subjObj.name : resx.getString(resxid);
            subjNameView.setText(outpName);
            avgView.setText(String.format(Locale.ENGLISH, "ÁTLAG: %.2f", subjObj.avg));

            return convertView;
        //} else {
        //    return convertView;
        //}
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
        //if (convertView == null) {
            convertView = inflater.inflate(R.layout.gradelist_item, null);

            Grade gradeObj = getChild(groupPosition, childPosition);

            TextView gradeView = (TextView) convertView.findViewById(R.id.grade);
            TextView dateView = (TextView) convertView.findViewById(R.id.date);
            TextView descView = (TextView) convertView.findViewById(R.id.gradeDesc);

            gradeView.setText(gradeObj.grade);
            dateView.setText(gradeObj.gotDate);
            if (gradeObj.theme.equals(" - ")) { gradeObj.theme = "Ismeretlen Téma"; }
            descView.setText(gradeObj.theme+" - "+gradeObj.type+" - "+gradeObj.teacher);

            return convertView;
        //} else {
        //    return convertView;
        //}
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
