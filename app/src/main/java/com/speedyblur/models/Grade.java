package com.speedyblur.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.speedyblur.kretaremastered.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Grade implements Parcelable {
    public String grade;
    public int gotDate;
    public String teacher;
    public String type;
    public String theme;
    public int colorId;

    private Grade(JSONObject obj) throws JSONException {
        this.grade = obj.getString("grade");
        this.gotDate = obj.getInt("date");
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

    protected Grade(Parcel in) {
        grade = in.readString();
        gotDate = in.readInt();
        teacher = in.readString();
        type = in.readString();
        theme = in.readString();
        colorId = in.readInt();
    }

    public static final Creator<Grade> CREATOR = new Creator<Grade>() {
        @Override
        public Grade createFromParcel(Parcel in) {
            return new Grade(in);
        }

        @Override
        public Grade[] newArray(int size) {
            return new Grade[size];
        }
    };

    public static ArrayList<Grade> fromJson(JSONArray jsonObjects) throws JSONException {
        ArrayList<Grade> grades = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            grades.add(new Grade(jsonObjects.getJSONObject(i)));
        }
        return grades;
    }

    @Override
    public int describeContents() {
        // Method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(grade);
        parcel.writeInt(gotDate);
        parcel.writeString(teacher);
        parcel.writeString(type);
        parcel.writeString(theme);
        parcel.writeInt(colorId);
    }
}
