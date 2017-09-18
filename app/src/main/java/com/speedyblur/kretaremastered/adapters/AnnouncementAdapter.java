package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Announcement;

import java.util.ArrayList;

public class AnnouncementAdapter extends ArrayAdapter<Announcement> {
    public AnnouncementAdapter(@NonNull Context ctxt, ArrayList<Announcement> announcements) {
        super(ctxt, 0, announcements);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.announcementlist_item, parent, false);

            holder = new ViewHolder();
            holder.mTeacher = convertView.findViewById(R.id.announcementTeacher);
            holder.mContent = convertView.findViewById(R.id.announcementContent);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Announcement a = getItem(position);
        assert a != null;

        holder.mTeacher.setText(a.getTeacher());
        holder.mContent.setText(a.getContent());

        return convertView;
    }

    private static class ViewHolder {
        TextView mTeacher;
        TextView mContent;
    }
}
