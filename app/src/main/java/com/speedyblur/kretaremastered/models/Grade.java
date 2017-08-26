package com.speedyblur.kretaremastered.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.speedyblur.kretaremastered.R;

public class Grade implements Parcelable {
    private String subject;
    private int grade;
    private int gotDate;
    private String type;
    private String theme;
    private String teacher;
    private int colorId;

    public Grade(String subject, int grade, int date, String type, String theme, String teacher) {
        this.subject = subject;
        this.grade = grade;
        this.gotDate = date;
        this.type = type;
        this.theme = theme;
        this.teacher = teacher;

        if (grade == 5) {
            this.colorId = R.color.excellentGrade;
        } else if (grade == 4) {
            this.colorId = R.color.goodGrade;
        } else if (grade == 3) {
            this.colorId = R.color.avgGrade;
        } else if (grade == 2) {
            this.colorId = R.color.badGrade;
        } else {
            this.colorId = R.color.veryBadGrade;
        }
    }

    protected Grade(Parcel in) {
        subject = in.readString();
        grade = in.readInt();
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

    @Override
    public int describeContents() {
        // Method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(subject);
        parcel.writeInt(grade);
        parcel.writeInt(gotDate);
        parcel.writeString(teacher);
        parcel.writeString(type);
        parcel.writeString(theme);
        parcel.writeInt(colorId);
    }

    // Getter methods
    public String getSubject() {
        return subject;
    }
    public int getGrade() {
        return grade;
    }
    public int getGotDate() {
        return gotDate;
    }
    public String getType() {
        return type;
    }
    public String getTheme() {
        return theme;
    }
    public String getTeacher() {
        return teacher;
    }
    public int getColorId() {
        return colorId;
    }
}
