package com.speedyblur.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GradeGroup implements Parcelable {
    private String groupId;
    public ArrayList<Grade> grades;

    public GradeGroup(String groupId, JSONArray grades) throws JSONException {
        new GradeGroup(groupId, Grade.fromJson(grades));
    }

    public GradeGroup(String groupId, ArrayList<Grade> grades) {
        this.groupId = groupId;
        this.grades = grades;
    }

    protected GradeGroup(Parcel in) {
        groupId = in.readString();
        grades = in.createTypedArrayList(Grade.CREATOR);
    }

    public static final Creator<GradeGroup> CREATOR = new Creator<GradeGroup>() {
        @Override
        public GradeGroup createFromParcel(Parcel in) {
            return new GradeGroup(in);
        }

        @Override
        public GradeGroup[] newArray(int size) {
            return new GradeGroup[size];
        }
    };

    public String getFormattedId(FormatHelper fHelper) {
        return fHelper.doFormat(groupId);
    }

    public static ArrayList<GradeGroup> assembleGroups(ArrayList<Grade> allGrades, String groupParam) throws NoSuchFieldException, IllegalAccessException {
        ArrayList<GradeGroup> complete = new ArrayList<>();
        for (int i=0; i<allGrades.size(); i++) {
            Grade current = allGrades.get(i);
            if (current.type.contains("végi") || current.type.contains("Félévi")) continue;
            boolean found = false;
            for (int j=0; j<complete.size(); j++) {
                if (complete.get(j).groupId.equals(current.getClass().getDeclaredField(groupParam).get(current).toString())) {
                    found = true;
                    complete.get(j).grades.add(current);
                }
            }
            if (!found) {
                ArrayList<Grade> newGradeList = new ArrayList<>();
                newGradeList.add(current);
                complete.add(new GradeGroup(current.getClass().getDeclaredField(groupParam).get(current).toString(), newGradeList));
            }
        }
        Collections.sort(complete, new Comparator<GradeGroup>() {
            @Override
            public int compare(GradeGroup g1, GradeGroup g2) {
                return g1.groupId.compareTo(g2.groupId);
            }
        });
        return complete;
    }

    @Override
    public int describeContents() {
        // Method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int i) {
        p.writeString(groupId);
        p.writeTypedList(grades);
    }

    public interface FormatHelper {
        String doFormat(String in);
    }
}
