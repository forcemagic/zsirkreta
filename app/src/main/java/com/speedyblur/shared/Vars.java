package com.speedyblur.shared;

import com.github.mikephil.charting.data.LineDataSet;

import java.util.HashMap;

public class Vars {
    public static final String APIBASE = "https://staging.speedyblur.com/kretaapi/dummy-withgrades";
    public static String AUTHTOKEN = "";
    public static String SQLCRYPT_PWD = "weeee";
    public static int VIEWPAGER_SCROLLDUR = 500;
    public static HashMap<String, LineDataSet> averageGraphData = new HashMap<>();
    public static HashMap<String, Integer> halfTermTimes = new HashMap<>();
}
