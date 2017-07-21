package com.speedyblur.kretaremastered;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
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

    @NonNull
    @Override
    @SuppressWarnings("deprecation")
    public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.avglist_item, parent, false);
        Average item = this.getItem(pos);

        TextView avgView = (TextView) convertView.findViewById(R.id.avglabel_average);
        TextView subjView = (TextView) convertView.findViewById(R.id.avglabel_subject);
        TextView descView = (TextView) convertView.findViewById(R.id.avglabel_desc);

        assert item != null;

        GradientDrawable grDrawable = (GradientDrawable) avgView.getBackground();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            grDrawable.setColor(convertView.getResources().getColor(item.colorId, null));
        } else {
            grDrawable.setColor(convertView.getResources().getColor(item.colorId));
        }
        avgView.setText(String.format(Locale.ENGLISH, "%.2f", item.average));

        Resources resx = this.getContext().getResources();
        int resxid = resx.getIdentifier("subject_"+item.subject, "string", convertView.getContext().getPackageName());
        String outpName = resxid == 0 ? item.subject : resx.getString(resxid);
        subjView.setText(outpName);
        descView.setText(resx.getString(R.string.avglabel_desc, item.classAverage, item.average - item.classAverage));

        return convertView;
    }
}
