package com.speedyblur.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Subject implements Parcelable {
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

    protected Subject(Parcel in) {
        name = in.readString();
        in.readTypedList(grades, Grade.CREATOR);
        avg = in.readDouble();
    }

    public static final Creator<Subject> CREATOR = new Creator<Subject>() {
        @Override
        public Subject createFromParcel(Parcel in) {
            return new Subject(in);
        }

        @Override
        public Subject[] newArray(int size) {
            return new Subject[size];
        }
    };

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

    @Override
    public int describeContents() {
        // Method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeTypedList(grades);
        parcel.writeDouble(avg);
    }
}
