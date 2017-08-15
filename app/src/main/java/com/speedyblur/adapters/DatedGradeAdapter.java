package com.speedyblur.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.models.GradeGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatedGradeAdapter extends ArrayAdapter<GradeGroup> {
    private ArrayList<GradeGroup> groups;

    public DatedGradeAdapter(@NonNull Context context, ArrayList<GradeGroup> groups) {
        super(context, 0, groups);
        this.groups = groups;
    }

    @NonNull
    @Override
    @SuppressLint("inflateparams")
    public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.datedgrade_group, null);

        TextView titleView = convertView.findViewById(R.id.datedgradegroupTitle);
        ListView rootList = convertView.findViewById(R.id.datedgradegroupSubItems);

        titleView.setText(groups.get(pos).getFormattedId(new GradeGroup.FormatHelper() {
            @Override
            public String doFormat(String in) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MMM", Locale.getDefault());
                return sdf.format(new Date(Long.parseLong(in)*1000));
            }
        }));
        rootList.setAdapter(new GradeListAdapter(getContext(), groups.get(pos).grades));

        return convertView;
    }
}
