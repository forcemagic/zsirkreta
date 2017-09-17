package com.speedyblur.kretaremastered.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class SubjectGradeGroup implements Parcelable {
    private final String subject;
    private final ArrayList<Grade> grades;

    public SubjectGradeGroup(String subject, ArrayList<Grade> grades) {
        this.subject = subject;
        this.grades = grades;
    }

    protected SubjectGradeGroup(Parcel in) {
        subject = in.readString();
        grades = in.createTypedArrayList(Grade.CREATOR);
    }

    public static final Creator<SubjectGradeGroup> CREATOR = new Creator<SubjectGradeGroup>() {
        @Override
        public SubjectGradeGroup createFromParcel(Parcel in) {
            return new SubjectGradeGroup(in);
        }

        @Override
        public SubjectGradeGroup[] newArray(int size) {
            return new SubjectGradeGroup[size];
        }
    };

    @Override
    public int describeContents() {
        // Method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(subject);
        dest.writeTypedList(grades);
    }

    public void addToGrades(Grade g) {
        grades.add(g);
    }

    // Getter methods
    public String getSubject() {
        return subject;
    }
    public ArrayList<Grade> getGrades() {
        return grades;
    }
}
