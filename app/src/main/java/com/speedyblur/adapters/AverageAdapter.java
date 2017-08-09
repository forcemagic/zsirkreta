package com.speedyblur.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.speedyblur.kretaremastered.AverageGraphActivity;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.models.Average;

import java.util.ArrayList;
import java.util.Locale;

public class AverageAdapter extends ArrayAdapter<Average> {
    public AverageAdapter(Context ctxt, ArrayList<Average> items) {
        super(ctxt, 0, items);
    }

    @NonNull
    @Override
    @SuppressWarnings("deprecation")
    public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.avglist_item, parent, false);
        final Average item = this.getItem(pos);

        TextView avgView = convertView.findViewById(R.id.avglabel_average);
        TextView subjView = convertView.findViewById(R.id.avglabel_subject);
        TextView descView = convertView.findViewById(R.id.avglabel_desc);

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

        final Context sharedCtxt = this.getContext();
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(sharedCtxt, AverageGraphActivity.class);
                it.putExtra("subject", item.subject);
                sharedCtxt.startActivity(it);
            }
        });

        return convertView;
    }
}
