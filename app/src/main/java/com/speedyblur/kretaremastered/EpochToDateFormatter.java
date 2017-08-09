package com.speedyblur.kretaremastered;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.util.Date;

public class EpochToDateFormatter implements IAxisValueFormatter {

    public EpochToDateFormatter() {}

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        DateFormat dFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return dFormat.format(new Date((long) value*1000));
    }
}
