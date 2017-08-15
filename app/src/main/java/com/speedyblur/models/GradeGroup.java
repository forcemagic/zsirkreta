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

    public static ArrayList<GradeGroup> assembleGroups(ArrayList<Grade> allGrades, String groupParam, SameGroupComparator cp) throws NoSuchFieldException, IllegalAccessException {
        return assembleGroups(allGrades, groupParam, cp, true, false);
    }

    public static ArrayList<GradeGroup> assembleGroups(ArrayList<Grade> allGrades, String groupParam, SameGroupComparator cp,
                                                       boolean doShowHalfterm) throws NoSuchFieldException, IllegalAccessException {
        return assembleGroups(allGrades, groupParam, cp, doShowHalfterm, false);
    }

    public static ArrayList<GradeGroup> assembleGroups(ArrayList<Grade> allGrades, String groupParam, SameGroupComparator cp,
                                                       boolean doShowHalfterm, final boolean reverseChildSort) throws NoSuchFieldException, IllegalAccessException {
        ArrayList<GradeGroup> complete = new ArrayList<>();
        for (int i=0; i<allGrades.size(); i++) {
            Grade current = allGrades.get(i);
            if ((current.type.contains("végi") || current.type.contains("Félévi")) && !doShowHalfterm) continue;
            boolean found = false;
            for (int j=0; j<complete.size(); j++) {
                if (cp.compare(complete.get(j).groupId, current.getClass().getDeclaredField(groupParam).get(current).toString())) {
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

        // Mass sorting
        Collections.sort(complete, new Comparator<GradeGroup>() {
            @Override
            public int compare(GradeGroup g1, GradeGroup g2) {
                return g2.groupId.compareTo(g1.groupId);
            }
        });
        for (int i=0; i<complete.size(); i++) {
            // We'll always want date-sort here
            ArrayList<Grade> sortedGrades = complete.get(i).grades;
            Collections.sort(sortedGrades, new Comparator<Grade>() {
                @Override
                public int compare(Grade g1, Grade g2) {
                    if (reverseChildSort) {
                        return g2.gotDate - g1.gotDate;
                    } else {
                        return g1.gotDate - g2.gotDate;
                    }
                }
            });
            complete.get(i).grades = sortedGrades;
        }

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

    public interface SameGroupComparator {
        boolean compare(String id1, String id2);
    }
}
