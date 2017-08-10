package com.speedyblur.kretaremastered;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EpochToDateFormatter implements IAxisValueFormatter {

    public EpochToDateFormatter() {}

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        SimpleDateFormat dFormat = new SimpleDateFormat("M-d", Locale.getDefault());
        return dFormat.format(new Date((long) value*1000));
    }
}
