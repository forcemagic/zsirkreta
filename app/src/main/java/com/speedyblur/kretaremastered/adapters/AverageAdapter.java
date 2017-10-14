package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.speedyblur.kretaremastered.EpochToDateFormatter;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Average;
import com.speedyblur.kretaremastered.shared.Common;

import java.util.ArrayList;

public class AverageAdapter extends RecyclerView.Adapter<AverageAdapter.ViewHolder> {
    public ArrayList<Average> averages;
    private int currentOpened = -1;

    public AverageAdapter(ArrayList<Average> averages) {
        this.averages = averages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater infl = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = infl.inflate(R.layout.avglist_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Average item = averages.get(position);
        Context ctxt = holder.avgProgress.getContext();

        LayerDrawable lDrawable = (LayerDrawable) holder.avgProgress.getProgressDrawable();
        lDrawable.getDrawable(2).setColorFilter(ContextCompat.getColor(ctxt, item.getColorId()), PorterDuff.Mode.SRC_ATOP);
        holder.avgProgress.setProgress((int) Math.round(item.getAverage()*10));
        holder.avgView.setText(String.valueOf(item.getAverage()));

        holder.subjView.setTypeface(Typeface.createFromAsset(ctxt.getAssets(), "fonts/OpenSans-Light.ttf"));
        holder.subjView.setText(Common.getLocalizedSubjectName(ctxt, item.getSubject()));
        holder.descView.setText(ctxt.getString(R.string.avglabel_desc, item.getClassAverage(), item.getAverage() - item.getClassAverage()));

        if (position == currentOpened) {
            holder.expandToggler.setRotation(180f);

            // DataSet settings
            final LineDataSet lds = new LineDataSet(item.getGraphData().getEntries(), "Averages");
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

            // Getting half-term line (if it exists)
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
            holder.chart.setVisibility(View.VISIBLE);
            holder.chart.setAlpha(0f);
            holder.chart.animate().alpha(1f).setDuration((long) 1000);
        } else {
            holder.expandToggler.setRotation(0f);
            holder.chart.setVisibility(View.INVISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    collapse(holder.chart);
                    holder.chart.setVisibility(View.GONE);
                }
            }, 1500);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int lastOpened = currentOpened;
                if (currentOpened == holder.getAdapterPosition()) currentOpened = -1;
                else currentOpened = holder.getAdapterPosition();
                notifyItemChanged(lastOpened); notifyItemChanged(currentOpened);
            }
        });
    }

    @Override
    public int getItemCount() {
        return averages.size();
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ProgressBar avgProgress;
        final TextView avgView;
        final TextView subjView;
        final TextView descView;
        final ImageView expandToggler;
        final LineChart chart;

        ViewHolder(View itemView) {
            super(itemView);
            avgProgress = itemView.findViewById(R.id.avgProgress);
            avgView = itemView.findViewById(R.id.avglabel_average);
            subjView = itemView.findViewById(R.id.avglabel_subject);
            descView = itemView.findViewById(R.id.avglabel_desc);
            expandToggler = itemView.findViewById(R.id.averageExpandToggler);
            chart = itemView.findViewById(R.id.averageChart);
            setupChart();
        }

        private void setupChart() {
            chart.getXAxis().setTextSize(10f);
            chart.getXAxis().setLabelCount(4);
            chart.getXAxis().setValueFormatter(new EpochToDateFormatter());
            chart.getXAxis().setAvoidFirstLastClipping(true);
            chart.getAxisLeft().setTextSize(18f);
            chart.getAxisLeft().setAxisMaximum(5.5f);
            chart.getAxisLeft().setAxisMinimum(0.5f);
            chart.getAxisLeft().setDrawTopYLabelEntry(true);
            chart.getAxisLeft().setGranularity(1f);
            chart.getAxisLeft().setLabelCount(5);
            chart.getAxisRight().setEnabled(false);
            chart.getDescription().setEnabled(false);
            chart.getLegend().setEnabled(false);
            chart.setScaleEnabled(false);
        }
    }
}