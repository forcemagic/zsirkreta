package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.speedyblur.kretaremastered.EpochToDateFormatter;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Average;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;

import java.util.ArrayList;

public class AverageAdapter extends BaseExpandableListAdapter {
    private Context ctxt;
    private ArrayList<Average> items;
    private String profileName;

    public AverageAdapter(Context ctxt, ArrayList<Average> items, String profileName) {
        this.ctxt = ctxt;
        this.items = items;
        this.profileName = profileName;
    }

    @Override
    public Average getGroup(int i) {
        return items.get(i);
    }

    @Override
    public int getGroupCount() {
        return items.size();
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public Object getChild(int i, int i1) {
        return null;
    }

    @Override
    public int getChildrenCount(int i) {
        return 1;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int pos, boolean isExp, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(ctxt).inflate(R.layout.avglist_item, parent, false);

            holder = new ViewHolder();
            holder.avgProgress = convertView.findViewById(R.id.avgProgress);
            holder.avgView = convertView.findViewById(R.id.avglabel_average);
            holder.subjView = convertView.findViewById(R.id.avglabel_subject);
            holder.descView = convertView.findViewById(R.id.avglabel_desc);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Average item = this.getGroup(pos);
        assert item != null;

        LayerDrawable lDrawable = (LayerDrawable) holder.avgProgress.getProgressDrawable();
        lDrawable.getDrawable(2).setColorFilter(ContextCompat.getColor(ctxt, item.getColorId()), PorterDuff.Mode.SRC_ATOP);
        holder.avgProgress.setProgress((int) Math.round(item.getAverage()*10));
        holder.avgView.setText(String.valueOf(item.getAverage()));

        holder.subjView.setTypeface(Typeface.createFromAsset(ctxt.getAssets(), "fonts/OpenSans-Light.ttf"));
        holder.subjView.setText(Common.getLocalizedSubjectName(ctxt, item.getSubject()));
        holder.descView.setText(ctxt.getString(R.string.avglabel_desc, item.getClassAverage(), item.getAverage() - item.getClassAverage()));

        return convertView;
    }

    @Override
    public View getChildView(int pos, int i1, boolean b, View convertView, ViewGroup parent) {
        final ChildHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(ctxt).inflate(R.layout.dropdown_average_graph, parent, false);

            holder = new ChildHolder();

            // Chart
            holder.chart = convertView.findViewById(R.id.averageChart);
            holder.chart.getContentRect().right = 8f;
            holder.chart.getXAxis().setTextSize(10f);
            holder.chart.getXAxis().setLabelCount(4);
            holder.chart.getXAxis().setValueFormatter(new EpochToDateFormatter());
            holder.chart.getXAxis().setAvoidFirstLastClipping(true);
            holder.chart.getAxisLeft().setTextSize(18f);
            holder.chart.getAxisLeft().setAxisMaximum(5.5f);
            holder.chart.getAxisLeft().setAxisMinimum(0.5f);
            holder.chart.getAxisLeft().setDrawTopYLabelEntry(true);
            holder.chart.getAxisLeft().setGranularity(1f);
            holder.chart.getAxisLeft().setLabelCount(5);
            holder.chart.getAxisRight().setEnabled(false);
            holder.chart.getDescription().setEnabled(false);
            holder.chart.getLegend().setEnabled(false);
            holder.chart.setScaleEnabled(false);

            convertView.setTag(holder);
        } else {
            holder = (ChildHolder) convertView.getTag();
        }

        // DataStore
        DataStore ds = null;
        try {
            ds = new DataStore(ctxt, profileName, Common.SQLCRYPT_PWD);
        } catch (DecryptionException e) {
            e.printStackTrace();
        }
        assert ds != null;

        // DataSet settings
        final LineDataSet lds = new LineDataSet(ds.getAverageGraphData(items.get(pos).getSubject()).getEntries(), "Averages");
        ds.close();
        lds.setValueTextColor(R.color.colorPrimaryDark);
        lds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lds.setDrawFilled(true);
        lds.setLineWidth(2f);
        lds.setValueTextSize(12f);
        lds.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (dataSetIndex != 0 && lds.getEntryForIndex(dataSetIndex - 1).getY() == value &&
                        !((boolean) entry.getData()) && dataSetIndex != lds.getEntryCount() - 1)
                    return "";
                return String.valueOf((double) Math.round(value * 100) / 100);
            }
        });
        lds.setCircleRadius(2f);
        lds.setCircleHoleRadius(1f);
        lds.setCircleColor(Color.BLACK);
        lds.setFillColor(ContextCompat.getColor(ctxt, R.color.colorAccent));
        lds.setColor(ContextCompat.getColor(ctxt, R.color.colorAccent));

        // Reset chart
        holder.chart.getXAxis().removeAllLimitLines();

        // Getting half-term line
        int halftermtime = -1;
        for (int i = 0; i < lds.getEntryCount(); i++) {
            Entry e = lds.getEntryForIndex(i);
            if ((boolean) e.getData()) {
                halftermtime = (int) e.getX();
                break;
            }
        }
        if (halftermtime != -1) {
            LimitLine limit = new LimitLine(halftermtime);
            limit.setLineWidth(2f);
            limit.enableDashedLine(24f, 6f, 0f);
            limit.setLineColor(ContextCompat.getColor(ctxt, R.color.veryBadGrade));
            holder.chart.getXAxis().addLimitLine(limit);
        }


        // Final data setting
        holder.chart.setData(new LineData(lds));
        holder.chart.invalidate();

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    private static class ViewHolder {
        ProgressBar avgProgress;
        TextView avgView;
        TextView subjView;
        TextView descView;
    }

    private static class ChildHolder {
        LineChart chart;
    }
}