package com.speedyblur.kretaremastered;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class ProfileAdapter extends ArrayAdapter<Profile> {
    ProfileAdapter(@NonNull Context context, ArrayList<Profile> items) {
        super(context, 0, items);
    }

    @Override
    @NonNull
    public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.proflist_item, parent, false);
        Profile item = this.getItem(pos);

        TextView subjId = (TextView) convertView.findViewById(R.id.profitem_studentid);
        subjId.setText(item.id);

        return convertView;
    }
}
