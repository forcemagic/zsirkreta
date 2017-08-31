package com.speedyblur.kretaremastered.models;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public class AvgGraphData {
    private final String subject;
    private final ArrayList<Entry> entries;

    public AvgGraphData(String subject, ArrayList<Entry> entires) {
        this.subject = subject;
        this.entries = entires;
    }

    // Getter methods
    public String getSubject() {
        return subject;
    }
    public ArrayList<Entry> getEntries() {
        return entries;
    }
}
