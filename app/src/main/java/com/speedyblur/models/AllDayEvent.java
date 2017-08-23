package com.speedyblur.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AllDayEvent implements Parcelable {
    public String name;
    public int date;

    public AllDayEvent(JSONObject jsObj) throws JSONException {
        this.name = jsObj.getString("name");
        this.date = jsObj.getInt("date");
    }

    protected AllDayEvent(Parcel in) {
        name = in.readString();
        date = in.readInt();
    }

    public static final Creator<AllDayEvent> CREATOR = new Creator<AllDayEvent>() {
        @Override
        public AllDayEvent createFromParcel(Parcel in) {
            return new AllDayEvent(in);
        }

        @Override
        public AllDayEvent[] newArray(int size) {
            return new AllDayEvent[size];
        }
    };

    public static ArrayList<AllDayEvent> fromJson(JSONArray jsArr) throws JSONException {
        ArrayList<AllDayEvent> allDayEvents = new ArrayList<>();
        for (int i=0; i<jsArr.length(); i++) {
            allDayEvents.add(new AllDayEvent(jsArr.getJSONObject(i)));
        }
        return allDayEvents;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(date);
    }
}
