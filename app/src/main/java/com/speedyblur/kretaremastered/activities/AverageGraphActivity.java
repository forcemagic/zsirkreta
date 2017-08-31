package com.speedyblur.kretaremastered.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.speedyblur.kretaremastered.EpochToDateFormatter;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;

public class AverageGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_average_graph);

        final String subject = getIntent().getStringExtra("subject");
        String profileName = getIntent().getStringExtra("profileName");

        // Set title
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(Common.getLocalizedSubjectName(this, subject));

        LineChart chart = (LineChart) findViewById(R.id.averageChart);

        // DataStore
        DataStore ds = null;
        try {
            ds = new DataStore(this, profileName, Common.SQLCRYPT_PWD);
        } catch (DecryptionException e) {e.printStackTrace();}
        assert ds != null;

        // DataSet settings
        final LineDataSet dataSet = new LineDataSet(ds.getAverageGraphData(subject).getEntries(), "Averages");
        ds.close();
        dataSet.setColor(R.color.colorAccent);
        dataSet.setValueTextColor(R.color.colorPrimaryDark);
        dataSet.setLineWidth(4f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                if (dataSet.getEntryIndex(entry) != 0 && dataSet.getEntryForIndex(dataSet.getEntryIndex(entry) - 1).getY() == value &&
                        !((boolean) entry.getData()) && dataSet.getEntryIndex(entry) != dataSet.getEntryCount()-1) return "";
                return String.valueOf((double)Math.round(value*100)/100);
            }
        });

        // Chart settings
        chart.getXAxis().setTextSize(10f);
        chart.getXAxis().setLabelCount(4);
        chart.getXAxis().setValueFormatter(new EpochToDateFormatter());
        chart.getXAxis().setAvoidFirstLastClipping(true);

        // Getting half-term line
        int halftermtime = 0;
        for (int i=0; i<dataSet.getEntryCount(); i++) {
            Entry e = dataSet.getEntryForIndex(i);
            if ((boolean) e.getData()) {
                halftermtime = (int) e.getX();
                break;
            }
        }
        LimitLine limit = new LimitLine(halftermtime);
        limit.enableDashedLine(4f, 4f, 2f);
        limit.setLabel(getResources().getString(R.string.avggraph_termseparator));
        chart.getXAxis().addLimitLine(limit);

        chart.getAxisLeft().setTextSize(16f);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }
}
