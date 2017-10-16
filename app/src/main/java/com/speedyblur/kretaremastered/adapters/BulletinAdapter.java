package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Bulletin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BulletinAdapter extends ArrayAdapter<Bulletin> {
    public BulletinAdapter(@NonNull Context ctxt, ArrayList<Bulletin> bulletins) {
        super(ctxt, 0, bulletins);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bulletinlist_item, parent, false);

            holder = new ViewHolder();
            holder.mBullet = convertView.findViewById(R.id.announcementSeenBullet);
            holder.mTeacher = convertView.findViewById(R.id.announcementTeacher);
            holder.mContent = convertView.findViewById(R.id.announcementContent);
            holder.mDate = convertView.findViewById(R.id.announcementDate);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Bulletin a = getItem(position);
        assert a != null;

        if (a.isSeen()) {
            Drawable d = ContextCompat.getDrawable(getContext(), R.drawable.announcement_bullet).mutate();
            d.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.darker_gray), PorterDuff.Mode.SRC_ATOP);
            holder.mBullet.setImageDrawable(d);
        }
        holder.mTeacher.setText(a.getTeacher());
        holder.mContent.setText(a.getContent());
        holder.mDate.setText(new SimpleDateFormat("yyyy. MMM. dd.", Locale.getDefault()).format(new Date((long) a.getDate()*1000)));

        return convertView;
    }

    private static class ViewHolder {
        ImageView mBullet;
        TextView mTeacher;
        TextView mContent;
        TextView mDate;
    }
}
