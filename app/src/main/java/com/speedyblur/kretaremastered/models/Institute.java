package com.speedyblur.kretaremastered.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Institute implements Parcelable {
    private final String name;
    private final String id;

    public Institute(String name, String id) {
        this.name = name;
        this.id = id;
    }

    protected Institute(Parcel in) {
        name = in.readString();
        id = in.readString();
    }

    public static final Creator<Institute> CREATOR = new Creator<Institute>() {
        @Override
        public Institute createFromParcel(Parcel in) {
            return new Institute(in);
        }

        @Override
        public Institute[] newArray(int size) {
            return new Institute[size];
        }
    };

    // Getter methods
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
    }

    @Override
    public String toString() {
        return name+" ("+id+")";
    }
}
