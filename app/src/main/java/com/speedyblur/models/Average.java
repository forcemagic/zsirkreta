package com.speedyblur.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.speedyblur.kretaremastered.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Average implements Parcelable {
    public String subject;
    public double average;
    public double classAverage;
    public int colorId;

    public Average(JSONObject jsobj) throws JSONException {
        this.subject = jsobj.getString("subject");
        this.average = jsobj.getDouble("average");
        this.classAverage = jsobj.getDouble("classAverage");

        if (this.average == 5) {
            this.colorId = R.color.excellentGrade;
        } else if (Math.round(this.average) > 3) {
            this.colorId = R.color.goodGrade;
        } else if (Math.round(this.average) == 3) {
            this.colorId = R.color.avgGrade;
        } else {
            this.colorId = R.color.badGrade;
        }
    }

    protected Average(Parcel in) {
        subject = in.readString();
        average = in.readDouble();
        classAverage = in.readDouble();
        colorId = in.readInt();
    }

    public static final Creator<Average> CREATOR = new Creator<Average>() {
        @Override
        public Average createFromParcel(Parcel in) {
            return new Average(in);
        }

        @Override
        public Average[] newArray(int size) {
            return new Average[size];
        }
    };

    public static ArrayList<Average> fromJson(JSONArray jsonObjects) {
        ArrayList<Average> grades = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                grades.add(new Average(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                Log.w("Average", "Unable to add average.");
                e.printStackTrace();
            }
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
        parcel.writeString(subject);
        parcel.writeDouble(average);
        parcel.writeDouble(classAverage);
        parcel.writeInt(colorId);
    }
}
