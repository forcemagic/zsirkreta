package com.speedyblur.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.models.Grade;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DateGradeListAdapter extends ArrayAdapter<Grade> {
    private ArrayList<Grade> grades;

    public DateGradeListAdapter(Context context, ArrayList<Grade> grades) {
        super(context, 0, grades);
        this.grades = grades;
    }

    @NonNull
    @Override
    public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.gradelist_item, null);

        Grade gradeObj = grades.get(pos);

        ImageView gradeBullet = convertView.findViewById(R.id.gradeBullet);
        TextView gradeView = convertView.findViewById(R.id.grade);
        TextView titleView = convertView.findViewById(R.id.gradeTitle);

        gradeView.setText(gradeObj.grade);
        gradeBullet.setColorFilter(ContextCompat.getColor(getContext(), gradeObj.colorId), PorterDuff.Mode.SRC_ATOP);
        int gotResxId = getContext().getResources().getIdentifier("subject_" + gradeObj.subject, "string", getContext().getPackageName());
        titleView.setText(gotResxId == 0 ? gradeObj.subject : getContext().getResources().getString(gotResxId));

        TextView descView1 = convertView.findViewById(R.id.gradeDesc);
        TextView descView2 = convertView.findViewById(R.id.gradeDesc2);

        if (gradeObj.theme.equals(" - ")) {
            descView1.setText(capitalize(gradeObj.type));
        } else {
            descView1.setText(capitalize(gradeObj.type) + " - " + capitalize(gradeObj.theme));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. M. d.", Locale.getDefault());
        descView2.setText(dateFormat.format(new Date((long)gradeObj.gotDate*1000)));

        if (pos == 0) {
            convertView.findViewById(R.id.gradeBarTop).setVisibility(View.INVISIBLE);
        }

        if (pos == grades.size()-1) {
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

}
