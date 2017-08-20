package com.speedyblur.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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

import com.speedyblur.kretaremastered.AverageGraphActivity;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.models.Average;

import java.util.ArrayList;

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
        assert item != null;

        ProgressBar avgProgress = convertView.findViewById(R.id.avgProgress);
        TextView avgView = convertView.findViewById(R.id.avglabel_average);
        TextView subjView = convertView.findViewById(R.id.avglabel_subject);
        TextView descView = convertView.findViewById(R.id.avglabel_desc);

        LayerDrawable lDrawable = (LayerDrawable) avgProgress.getProgressDrawable();
        lDrawable.getDrawable(2).setColorFilter(ContextCompat.getColor(getContext(), item.colorId), PorterDuff.Mode.SRC_ATOP);
        avgProgress.setProgress((int) Math.round(item.average*10));
        avgView.setText(String.valueOf(item.average));

        Resources resx = getContext().getResources();
        int resxid = resx.getIdentifier("subject_"+item.subject, "string", convertView.getContext().getPackageName());
        String outpName = resxid == 0 ? item.subject : resx.getString(resxid);
        subjView.setText(outpName);
        descView.setText(resx.getString(R.string.avglabel_desc, item.classAverage, item.average - item.classAverage));

        final Context sharedCtxt = getContext();
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(sharedCtxt, AverageGraphActivity.class);
                it.putExtra("subject", item.subject);
                sharedCtxt.startActivity(it);
            }
        });

        if (pos == 0) convertView.findViewById(R.id.avgBarTop).setVisibility(View.INVISIBLE);
        if (pos == getCount() - 1) convertView.findViewById(R.id.avgBarBottom).setVisibility(View.INVISIBLE);

        return convertView;
    }
}
