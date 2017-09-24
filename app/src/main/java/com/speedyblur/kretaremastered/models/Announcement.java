package com.speedyblur.kretaremastered.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Announcement implements Parcelable {
    private final String teacher;
    private final String content;
    private final int date;
    private final boolean seen;

    public Announcement(String teacher, String content, int date, boolean seen) {
        this.teacher = teacher;
        this.content = content;
        this.date = date;
        this.seen = seen;
    }

    protected Announcement(Parcel in) {
        teacher = in.readString();
        content = in.readString();
        date = in.readInt();
        seen = in.readInt() == 1;
    }

    public static final Creator<Announcement> CREATOR = new Creator<Announcement>() {
        @Override
        public Announcement createFromParcel(Parcel in) {
            return new Announcement(in);
        }

        @Override
        public Announcement[] newArray(int size) {
            return new Announcement[size];
        }
    };

    @Override
    public int describeContents() {
        // Method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(teacher);
        parcel.writeString(content);
        parcel.writeInt(date);
        parcel.writeInt(seen ? 1 : 0);
    }

    // Getter methods
    public String getTeacher() {
        return teacher;
    }
    public String getContent() {
        return content;
    }
    public int getDate() {
        return date;
    }
    public boolean isSeen() {
        return seen;
    }
}
