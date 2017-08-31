package com.speedyblur.kretaremastered.models;

import android.os.Parcel;
import android.os.Parcelable;

public class AllDayEvent implements Parcelable {
    private final String name;
    private final int date;

    public AllDayEvent(String name, int date) {
        this.name = name;
        this.date = date;
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

    @Override
    public int describeContents() {
        // Method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(date);
    }

    // Getter methods
    public String getName() {
        return name;
    }
    public int getDate() {
        return date;
    }
}
