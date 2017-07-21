package com.speedyblur.kretaremastered;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class Subject {
    String name;
    ArrayList<Grade> grades;
    double avg;

    Subject(JSONObject subjObj) throws JSONException, IllegalArgumentException {
        this.name = subjObj.getString("subject");
        this.grades = Grade.fromJson(subjObj.getJSONArray("grades"));
        // We don't have any grades, so why show the subject?
        if (this.grades.size() == 0) {
            throw new IllegalArgumentException("Grades not present.");
        }
        this.avg = subjObj.getDouble("avg");
    }

    static ArrayList<Subject> fromJson(JSONArray jsonObjects) throws JSONException {
        Log.d("Subject", "About to parse JSON array...");
        int gradeCount = 0;
        ArrayList<Subject> subjects = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                Subject cSubj = new Subject(jsonObjects.getJSONObject(i));
                subjects.add(cSubj);
                gradeCount += jsonObjects.getJSONObject(i).getJSONArray("grades").length();
            } catch (IllegalArgumentException e) {
                Log.w("Subject", "Skipping subject, because it does not have any grades!");
            }
        }
        Log.d("Subject", String.format("DONE! We have a total of %d subjects and %d grades.", subjects.size(), gradeCount));
        return subjects;
    }
}
