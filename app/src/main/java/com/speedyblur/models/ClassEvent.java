package com.speedyblur.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ClassEvent implements Parcelable {
    public String subject;
    public String group;
    public String teacher;
    public String room;
    public int classnum;
    public int beginTime;
    public int endTime;
    public String theme;

    public ClassEvent(JSONObject jsObj) throws JSONException {
        this.subject = jsObj.getString("subject");
        this.group = jsObj.getString("group");
        this.teacher = jsObj.getString("teacher");
        this.room = jsObj.getString("room");
        this.classnum = jsObj.getInt("classnum");
        this.beginTime = jsObj.getInt("begin");
        this.endTime = jsObj.getInt("end");
        this.theme = jsObj.getString("theme");
    }

    protected ClassEvent(Parcel in) {
        subject = in.readString();
        group = in.readString();
        teacher = in.readString();
        room = in.readString();
        classnum = in.readInt();
        beginTime = in.readInt();
        endTime = in.readInt();
        theme = in.readString();
    }

    public static final Creator<ClassEvent> CREATOR = new Creator<ClassEvent>() {
        @Override
        public ClassEvent createFromParcel(Parcel in) {
            return new ClassEvent(in);
        }

        @Override
        public ClassEvent[] newArray(int size) {
            return new ClassEvent[size];
        }
    };

    public static ArrayList<ClassEvent> fromJson(JSONArray jsArr) throws JSONException {
        ArrayList<ClassEvent> classes = new ArrayList<>();
        for (int i=0; i<jsArr.length(); i++) {
            classes.add(new ClassEvent(jsArr.getJSONObject(i)));
        }
        return classes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(subject);
        parcel.writeString(group);
        parcel.writeString(teacher);
        parcel.writeString(room);
        parcel.writeInt(classnum);
        parcel.writeInt(beginTime);
        parcel.writeInt(endTime);
        parcel.writeString(theme);
    }
}
