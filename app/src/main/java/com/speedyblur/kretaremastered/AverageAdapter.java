package com.speedyblur.kretaremastered;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

class AverageAdapter extends ArrayAdapter<Average> {
    AverageAdapter(Context ctxt, ArrayList<Average> items) {
        super(ctxt, 0, items);
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.avglist_item, parent, false);
            Average item = this.getItem(pos);

            TextView avgView = (TextView) convertView.findViewById(R.id.avglabel_average);
            TextView subjView = (TextView) convertView.findViewById(R.id.avglabel_subject);

            avgView.setText(String.format(Locale.ENGLISH, "%.2f", item.average));
            subjView.setText(item.subject);

            return convertView;
        } else {
            return convertView;
        }
    }
}
