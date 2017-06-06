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
            StringBuilder sb = new StringBuilder();
            String[] origArray = subjObj.getString("subject").split(" ");

            for (String frag : origArray) {
                if (!frag.equalsIgnoreCase("és")) {
                    char firstLetter = frag.toCharArray()[0];
                    sb.append(Character.toUpperCase(firstLetter)).append(frag.substring(1)).append(" ");
                } else {
                    sb.append("és ");
                }
            }

            this.name = sb.toString().trim();
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
