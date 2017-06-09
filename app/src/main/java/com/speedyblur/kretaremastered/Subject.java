package com.speedyblur.kretaremastered;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class Subject {
    String name;
    ArrayList<Grade> grades;
    double avg;

    Subject(JSONObject subjObj) {
        try {
            this.name = subjObj.getString("subject");
            this.grades = Grade.fromJson(subjObj.getJSONArray("grades"));
            this.avg = subjObj.getDouble("avg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static ArrayList<Subject> fromJson(JSONArray jsonObjects) {
        ArrayList<Subject> grades = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                grades.add(new Subject(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return grades;
    }
}
