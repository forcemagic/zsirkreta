package com.speedyblur.models;

import android.util.Log;

import com.speedyblur.kretaremastered.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Average {
    public String subject;
    public double average;
    public double classAverage;
    public int colorId;

    public Average(JSONObject jsobj) throws JSONException {
        this.subject = jsobj.getString("subject");
        this.average = jsobj.getDouble("average");
        this.classAverage = jsobj.getDouble("classAverage");

        if (this.average == 5) {
            this.colorId = R.color.excellentGrade;
        } else if (Math.round(this.average) > 3) {
            this.colorId = R.color.goodGrade;
        } else if (Math.round(this.average) == 3) {
            this.colorId = R.color.avgGrade;
        } else {
            this.colorId = R.color.badGrade;
        }
    }

    public static ArrayList<Average> fromJson(JSONArray jsonObjects) {
        ArrayList<Average> grades = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                grades.add(new Average(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                Log.w("Average", "Unable to add average.");
                e.printStackTrace();
            }
        }
        return grades;
    }
}