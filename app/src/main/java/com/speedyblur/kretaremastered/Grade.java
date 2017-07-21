package com.speedyblur.kretaremastered;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class Grade {
    String grade;
    String gotDate;
    String teacher;
    String type;
    String theme;
    int colorId;

    private Grade(JSONObject obj) throws JSONException {
        this.grade = obj.getString("grade");
        this.gotDate = obj.getString("date");
        this.teacher = obj.getString("teacher");
        this.theme = obj.getString("theme");
        this.type = obj.getString("type");

        int intGrade = Integer.parseInt(this.grade);
        if (intGrade > 3) {
            this.colorId = R.color.goodGrade;
        } else if (intGrade == 3) {
            this.colorId = R.color.avgGrade;
        } else {
            this.colorId = R.color.badGrade;
        }
    }

    static ArrayList<Grade> fromJson(JSONArray jsonObjects) throws JSONException {
        Log.d("Grade", "About to parse JSON array...");
        ArrayList<Grade> grades = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            grades.add(new Grade(jsonObjects.getJSONObject(i)));
        }
        Log.d("Grade", String.format("DONE! This subject had a total of %d grades.", grades.size()));
        return grades;
    }
}
