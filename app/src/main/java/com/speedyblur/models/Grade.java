package com.speedyblur.models;

import com.speedyblur.kretaremastered.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Grade {
    public String grade;
    public String gotDate;
    public String teacher;
    public String type;
    public String theme;
    public int colorId;

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

    public static ArrayList<Grade> fromJson(JSONArray jsonObjects) throws JSONException {
        ArrayList<Grade> grades = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            grades.add(new Grade(jsonObjects.getJSONObject(i)));
        }
        return grades;
    }
}