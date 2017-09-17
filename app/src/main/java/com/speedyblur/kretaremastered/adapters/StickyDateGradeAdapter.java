package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Grade;
import com.speedyblur.kretaremastered.shared.Common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class StickyDateGradeAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private final Context ctxt;
    private final LayoutInflater inflater;
    private final ArrayList<Grade> grades;

    public StickyDateGradeAdapter(@NonNull Context context, ArrayList<Grade> grades) {
        ctxt = context;
        inflater = LayoutInflater.from(context);
        this.grades = grades;
    }

    @Override
    public int getCount() {
        return grades.size();
    }

    @Override
    public Grade getItem(int i) {
        return grades.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = inflater.inflate(R.layout.gradelist_item, parent, false);

            holder = new ViewHolder();
            holder.gradeBullet = convertView.findViewById(R.id.gradeBullet);
            holder.gradeView = convertView.findViewById(R.id.grade);
            holder.titleView = convertView.findViewById(R.id.gradeTitle);
            holder.descView1 = convertView.findViewById(R.id.gradeDesc);
            holder.descView2 = convertView.findViewById(R.id.gradeDesc2);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Grade gradeObj = getItem(pos);
        holder.gradeView.setText(String.valueOf(gradeObj.getGrade()));
        holder.gradeBullet.setColorFilter(ContextCompat.getColor(ctxt, gradeObj.getColorId()), PorterDuff.Mode.SRC_ATOP);
        holder.titleView.setText(Common.getLocalizedSubjectName(ctxt, gradeObj.getSubject()));

        if (gradeObj.getTheme().equals(" - ")) {
            holder.descView1.setText(capitalize(gradeObj.getType()));
        } else {
            holder.descView1.setText(capitalize(gradeObj.getType()) + " - " + capitalize(gradeObj.getTheme()));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. M. d.", Locale.getDefault());
        holder.descView2.setText(dateFormat.format(new Date((long) gradeObj.getDate() * 1000)));

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder headerholder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = inflater.inflate(R.layout.datedgrade_header, parent, false);

            headerholder = new HeaderViewHolder();
            headerholder.textView = convertView.findViewById(R.id.datedGradeHeader);
            headerholder.datedGradeGroupBar = convertView.findViewById(R.id.datedGradeGroupBar);

            convertView.setTag(headerholder);
        } else {
            headerholder = (HeaderViewHolder) convertView.getTag();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MMMM", Locale.getDefault());
        headerholder.textView.setTypeface(Typeface.createFromAsset(ctxt.getAssets(), "fonts/OpenSans-Light.ttf"));
        headerholder.textView.setText(dateFormat.format(new Date((long) grades.get(position).getDate() * 1000)));
        headerholder.datedGradeGroupBar.getLayoutParams().width = (int) Math.round(Resources.getSystem().getDisplayMetrics().widthPixels * 0.8);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis((long) grades.get(position).getDate() * 1000);
        return Long.parseLong(String.valueOf(c.get(Calendar.YEAR))+String.valueOf(c.get(Calendar.MONTH)));
    }

    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    private static class ViewHolder {
        ImageView gradeBullet;
        TextView gradeView;
        TextView titleView;
        TextView descView1;
        TextView descView2;
    }

    private static class HeaderViewHolder {
        TextView textView;
        View datedGradeGroupBar;
    }
}
