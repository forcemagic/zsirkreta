package com.speedyblur.shared;

import android.content.Context;

import com.github.mikephil.charting.data.LineDataSet;

import java.util.HashMap;

public class Vars {
    public static final String APIBASE = "https://staging.speedyblur.com/kretaapi";
    public static String AUTHTOKEN = "";
    public static String SQLCRYPT_PWD = "weeee";
    public static int VIEWPAGER_SCROLLDUR = 500;
    public static HashMap<String, LineDataSet> averageGraphData = new HashMap<>();
    public static HashMap<String, Integer> halfTermTimes = new HashMap<>();

    public static String getLocalizedSubjectName(Context context, String subject) {
        int gotResxId = context.getResources().getIdentifier("subject_" + subject, "string", context.getPackageName());
        return gotResxId == 0 ? subject : context.getResources().getString(gotResxId);
    }
}
