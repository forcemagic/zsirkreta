package com.speedyblur.kretaremastered.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.speedyblur.kretaremastered.R;

public class Average implements Parcelable {
    private final String subject;
    private final double average;
    private final double classAverage;
    private AvgGraphData graphData;
    private final int colorId;

    public Average(String subject, double average, double classAverage) {
        this.subject = subject;
        this.average = average;
        this.classAverage = classAverage;

        int roundedAvg = (int) Math.round(this.average);
        if (roundedAvg == 5) {
            this.colorId = R.color.excellentGrade;
        } else if (roundedAvg == 4) {
            this.colorId = R.color.goodGrade;
        } else if (roundedAvg == 3) {
            this.colorId = R.color.avgGrade;
        } else if (roundedAvg == 2) {
            this.colorId = R.color.badGrade;
        } else {
            this.colorId = R.color.veryBadGrade;
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

    public void setGraphData(AvgGraphData graphData) {
        this.graphData = graphData;
    }

    // Getter methods
    public String getSubject() {
        return subject;
    }
    public double getAverage() {
        return average;
    }
    public double getClassAverage() {
        return classAverage;
    }
    public int getColorId() {
        return colorId;
    }
    public AvgGraphData getGraphData() {
        return graphData;
    }
}
