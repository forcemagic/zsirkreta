package com.speedyblur.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Absence implements Parcelable {
    public String type;
    public int date;
    public int classNum;
    public String provementType;
    public boolean proven;
    public int recordDate;
    public String subject;
    public String theme;

    public Absence(JSONObject jsObj) throws JSONException {
        this.type = jsObj.getString("absencetype");
        this.date = jsObj.getInt("absencedate");
        this.classNum = jsObj.getInt("classnum");
        this.provementType = jsObj.getString("provementtype");
        this.proven = jsObj.getBoolean("proven");
        this.recordDate = jsObj.getInt("recorddate");
        this.subject = jsObj.getString("subject");
        this.theme = jsObj.getString("theme");
    }

    protected Absence(Parcel in) {
        type = in.readString();
        date = in.readInt();
        classNum = in.readInt();
        provementType = in.readString();
        proven = in.readByte() != 0;
        recordDate = in.readInt();
        subject = in.readString();
        theme = in.readString();
    }

    public static final Creator<Absence> CREATOR = new Creator<Absence>() {
        @Override
        public Absence createFromParcel(Parcel in) {
            return new Absence(in);
        }

        @Override
        public Absence[] newArray(int size) {
            return new Absence[size];
        }
    };

    public static ArrayList<Absence> fromJson(JSONArray jsArr) throws JSONException {
        ArrayList<Absence> absences = new ArrayList<>();
        for (int i=0; i<jsArr.length(); i++) {
            absences.add(new Absence(jsArr.getJSONObject(i)));
        }
        return absences;
    }

    @Override
    public int describeContents() {
        // Method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(type);
        parcel.writeInt(date);
        parcel.writeInt(classNum);
        parcel.writeString(provementType);
        parcel.writeByte((byte) (proven ? 1 : 0));
        parcel.writeInt(recordDate);
        parcel.writeString(subject);
        parcel.writeString(theme);
    }
}
