package com.speedyblur.kretaremastered;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.speedyblur.shared.Vars;

public class AverageGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_average_graph);

        LineChart chart = (LineChart) findViewById(R.id.averageChart);

        // DataSet settings
        LineDataSet dataSet = Vars.averageGraphData.get(this.getIntent().getStringExtra("subject"));
        dataSet.setColor(R.color.colorAccent);
        dataSet.setValueTextColor(R.color.colorPrimary);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(12f);

        // Chart settings
        chart.getXAxis().setTextSize(10f);
        chart.getXAxis().setLabelCount(4);
        chart.getXAxis().setValueFormatter(new EpochToDateFormatter());
        chart.getXAxis().setAvoidFirstLastClipping(true);
        chart.getAxisLeft().setTextSize(16f);
        chart.getAxisRight().setDrawAxisLine(false);
        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }
}
