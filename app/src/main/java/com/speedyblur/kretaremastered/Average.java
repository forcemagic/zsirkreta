package com.speedyblur.kretaremastered;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class Average {
    String subject;
    double average;
    double classAverage;
    int colorId;

    private Average(JSONObject jsobj) {
        try {
            this.subject = jsobj.getString("subject");
            this.average = jsobj.getDouble("average");
            this.classAverage = jsobj.getDouble("classAverage");

            if (this.average > 3) {
                this.colorId = R.color.goodGrade;
            } else if (Math.round(this.average) == 3) {
                this.colorId = R.color.avgGrade;
            } else {
                this.colorId = R.color.badGrade;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static ArrayList<Average> fromJson(JSONArray jsonObjects) {
        ArrayList<Average> grades = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                grades.add(new Average(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return grades;
    }
}
