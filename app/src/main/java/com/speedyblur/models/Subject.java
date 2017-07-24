package com.speedyblur.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Subject {
    public String name;
    public ArrayList<Grade> grades;
    public double avg;
    
    public Subject(JSONObject subjObj) throws JSONException, IllegalArgumentException {
        this.name = subjObj.getString("subject");
        this.grades = Grade.fromJson(subjObj.getJSONArray("grades"));
        // We don't have any grades, so why show the subject?
        if (this.grades.size() == 0) {
            throw new IllegalArgumentException("Grades not present.");
        }
        this.avg = subjObj.getDouble("avg");
    }

    public static ArrayList<Subject> fromJson(JSONArray jsonObjects) throws JSONException {
        int gradeCount = 0;
        ArrayList<Subject> subjects = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                Subject cSubj = new Subject(jsonObjects.getJSONObject(i));
                subjects.add(cSubj);
                gradeCount += jsonObjects.getJSONObject(i).getJSONArray("grades").length();
            } catch (IllegalArgumentException e) {
                Log.w("Subject", String.format("Skipping subject (%s), because it does not have any grades!", jsonObjects.getJSONObject(i).getString("subject")));
            }
        }
        Log.d("Subject", String.format("We have a total of %d subjects and %d grades.", subjects.size(), gradeCount));
        return subjects;
    }
}
