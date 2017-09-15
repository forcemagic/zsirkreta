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
import com.speedyblur.kretaremastered.models.Clazz;
import com.speedyblur.kretaremastered.shared.Common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ClazzAdapter extends ArrayAdapter<Clazz> {

    public ClazzAdapter(@NonNull Context context, ArrayList<Clazz> clazzes) {
        super(context, 0, clazzes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.schedulelist_item, parent, false);

            holder = new ViewHolder();
            holder.scheduleStatus = convertView.findViewById(R.id.scheduleStatus);
            holder.scheduleClassNum = convertView.findViewById(R.id.scheduleClassNum);
            holder.scheduleSubject = convertView.findViewById(R.id.scheduleSubject);
            holder.scheduleTheme = convertView.findViewById(R.id.scheduleTheme);
            holder.scheduleTimes = convertView.findViewById(R.id.scheduleTimes);
            holder.scheduleReplacement = convertView.findViewById(R.id.scheduleReplacement);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Clazz c = getItem(position);
        assert c != null;

        // Setting drawable
        Drawable toSet;
        if (!c.isHeld()) {
            toSet = ContextCompat.getDrawable(getContext(), R.drawable.class_not_held_icon_gray);
        } else if (c.isAbsent() && c.getAbsenceDetails().isProven()) {
            toSet = ContextCompat.getDrawable(getContext(), R.drawable.check_circle_icon_green);
        } else if (c.isAbsent() && !c.getAbsenceDetails().isProven()) {
            toSet = ContextCompat.getDrawable(getContext(), R.drawable.dash_circle_icon_red);
        } else if ((long) c.getBeginTime()*1000 > Calendar.getInstance().getTimeInMillis()) {
            toSet = ContextCompat.getDrawable(getContext(), R.drawable.normalgrade).mutate();
            toSet.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.darker_gray), PorterDuff.Mode.SRC_ATOP);
        } else if ((long) c.getBeginTime()*1000 < Calendar.getInstance().getTimeInMillis() && Calendar.getInstance().getTimeInMillis() < (long) c.getEndTime()*1000) {
            toSet = ContextCompat.getDrawable(getContext(), R.drawable.current_class_icon_green);
        } else {
            toSet = ContextCompat.getDrawable(getContext(), R.drawable.normalgrade).mutate();
            toSet.setColorFilter(ContextCompat.getColor(getContext(), R.color.goodGrade), PorterDuff.Mode.SRC_ATOP);
        }

        // Get replacement and replacer (if exists)
        holder.scheduleReplacement.setVisibility(View.GONE);
        for (int i=0; i<getCount(); i++) {
            Clazz notHeldC = getItem(i); assert notHeldC != null;
            if (!notHeldC.isHeld() && notHeldC.getClassnum() == c.getClassnum() && !notHeldC.equals(c)) {
                holder.scheduleReplacement.setText(getContext().getString(R.string.substitute_class, Common.getLocalizedSubjectName(getContext(), notHeldC.getSubject())));
                holder.scheduleReplacement.setVisibility(View.VISIBLE);
            }
        }
        for (int i=0; i<getCount(); i++) {
            Clazz subC = getItem(i); assert subC != null;
            // TODO: Optimize; this should not really be used
            if (!c.isHeld() && subC.isHeld() && c.getClassnum() == subC.getClassnum()) return new View(getContext());
        }

        holder.scheduleStatus.setImageDrawable(toSet);
        holder.scheduleClassNum.setText(getContext().getString(R.string.class_number, c.getClassnum()));
        holder.scheduleTheme.setVisibility(View.VISIBLE);
        if (c.getTheme().equals("")) holder.scheduleTheme.setVisibility(View.GONE);
        else holder.scheduleTheme.setText(c.getTheme());
        holder.scheduleSubject.setText(Common.getLocalizedSubjectName(getContext(), c.getSubject()));

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm", Locale.getDefault());
        holder.scheduleTimes.setText(sdf.format(new Date((long) c.getBeginTime()*1000))+" - "+sdf.format(new Date((long) c.getEndTime()*1000)));

        return convertView;
    }

    private static class ViewHolder {
        ImageView scheduleStatus;
        TextView scheduleClassNum;
        TextView scheduleSubject;
        TextView scheduleTheme;
        TextView scheduleTimes;
        TextView scheduleReplacement;
    }
}
