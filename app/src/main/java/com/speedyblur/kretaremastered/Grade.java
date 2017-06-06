package com.speedyblur.kretaremastered;

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

    Grade(JSONObject obj) {
        try {
            this.grade = obj.getString("grade");
            this.gotDate = obj.getString("date");
            this.teacher = obj.getString("teacher");
            this.theme = obj.getString("theme");
            this.type = obj.getString("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static ArrayList<Grade> fromJson(JSONArray jsonObjects) {
        ArrayList<Grade> grades = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                grades.add(new Grade(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return grades;
    }
}
