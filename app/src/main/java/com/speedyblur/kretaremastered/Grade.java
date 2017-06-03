package com.speedyblur.kretaremastered;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class Grade {
    String grade;
    String subject;
    String gotDate;

    Grade(JSONObject obj) {
        try {
            this.grade = obj.getString("grade");
            this.subject = obj.getString("subject");
            this.gotDate = obj.getString("date");
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
