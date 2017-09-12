package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Average;
import com.speedyblur.kretaremastered.shared.Common;

import java.util.ArrayList;

import static com.speedyblur.kretaremastered.R.id.avgProgress;

public class AverageAdapter extends ArrayAdapter<Average> {
    public AverageAdapter(Context ctxt, ArrayList<Average> items) {
        super(ctxt, 0, items);
    }

    @NonNull
    @Override
    public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.avglist_item, parent, false);

            holder = new ViewHolder();
            holder.avgProgress = convertView.findViewById(avgProgress);
            holder.avgView = convertView.findViewById(R.id.avglabel_average);
            holder.subjView = convertView.findViewById(R.id.avglabel_subject);
            holder.descView = convertView.findViewById(R.id.avglabel_desc);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Average item = this.getItem(pos);
        assert item != null;

        LayerDrawable lDrawable = (LayerDrawable) holder.avgProgress.getProgressDrawable();
        lDrawable.getDrawable(2).setColorFilter(ContextCompat.getColor(getContext(), item.getColorId()), PorterDuff.Mode.SRC_ATOP);
        holder.avgProgress.setProgress((int) Math.round(item.getAverage()*10));
        holder.avgView.setText(String.valueOf(item.getAverage()));

        holder.subjView.setText(Common.getLocalizedSubjectName(getContext(), item.getSubject()));
        holder.descView.setText(getContext().getString(R.string.avglabel_desc, item.getClassAverage(), item.getAverage() - item.getClassAverage()));

        return convertView;
    }

    private static class ViewHolder {
        ProgressBar avgProgress;
        TextView avgView;
        TextView subjView;
        TextView descView;
        View avgBarTop;
        View avgBarBottom;
    }
}