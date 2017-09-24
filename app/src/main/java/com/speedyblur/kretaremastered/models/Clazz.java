package com.speedyblur.kretaremastered.models;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.speedyblur.kretaremastered.R;

import java.util.Calendar;

public class Clazz implements Parcelable {
    private final String subject;
    private final String group;
    private final String teacher;
    private final String room;
    private final int classnum;
    private final int beginTime;
    private final int endTime;
    private final String theme;
    private final boolean isHeld;
    private boolean isAbsent;
    private AbsenceDetails absenceDetails;

    public Clazz(String subject, String group, String teacher, String room, int classnum, int begin, int end, String theme, boolean isheld, boolean isabsent,
                 @Nullable AbsenceDetails absenceDetails) {
        this.subject = subject;
        this.group = group;
        this.teacher = teacher;
        this.room = room;
        this.classnum = classnum;
        this.beginTime = begin;
        this.endTime = end;
        this.theme = theme;
        this.isHeld = isheld;
        this.isAbsent = isabsent;
        this.absenceDetails = absenceDetails;
    }

    protected Clazz(Parcel in) {
        subject = in.readString();
        group = in.readString();
        teacher = in.readString();
        room = in.readString();
        classnum = in.readInt();
        beginTime = in.readInt();
        endTime = in.readInt();
        theme = in.readString();
        isHeld = in.readByte() == 1;
    }

    public static final Creator<Clazz> CREATOR = new Creator<Clazz>() {
        @Override
        public Clazz createFromParcel(Parcel in) {
            return new Clazz(in);
        }

        @Override
        public Clazz[] newArray(int size) {
            return new Clazz[size];
        }
    };

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
        parcel.writeByte((byte) (isHeld ? 1 : 0));
    }

    public Drawable getIcon(Context ctxt) {
        Drawable toSet;
        if (!isHeld && (long) beginTime*1000 < Calendar.getInstance().getTimeInMillis()) {
            toSet = ContextCompat.getDrawable(ctxt, R.drawable.class_not_held_icon_orange);
        } else if (!isHeld) {
            toSet = ContextCompat.getDrawable(ctxt, R.drawable.class_not_held_icon_gray);
        } else if (isAbsent && absenceDetails.isProven()) {
            toSet = ContextCompat.getDrawable(ctxt, R.drawable.check_circle_icon_green);
        } else if (isAbsent && !absenceDetails.isProven()) {
            toSet = ContextCompat.getDrawable(ctxt, R.drawable.dash_circle_icon_red);
        } else if ((long) beginTime*1000 <= Calendar.getInstance().getTimeInMillis() && Calendar.getInstance().getTimeInMillis() <= (long) endTime*1000) {
            toSet = ContextCompat.getDrawable(ctxt, R.drawable.current_class_icon_green);
        } else {
            toSet = ContextCompat.getDrawable(ctxt, R.drawable.schedule_circle).mutate();
            if ((long) beginTime * 1000 < Calendar.getInstance().getTimeInMillis())
                toSet.setColorFilter(ContextCompat.getColor(ctxt, R.color.goodGrade), PorterDuff.Mode.SRC_ATOP);
            else
                toSet.setColorFilter(ContextCompat.getColor(ctxt, android.R.color.darker_gray), PorterDuff.Mode.SRC_ATOP);
        }
        return toSet;
    }

    // Getter methods
    public String getSubject() {
        return subject;
    }
    public String getGroup() {
        return group;
    }
    public String getTeacher() {
        return teacher;
    }
    public String getRoom() {
        return room;
    }
    public int getClassnum() {
        return classnum;
    }
    public int getBeginTime() {
        return beginTime;
    }
    public int getEndTime() {
        return endTime;
    }
    public String getTheme() {
        return theme;
    }
    public boolean isHeld() {
        return isHeld;
    }
    public boolean isAbsent() {
        return isAbsent;
    }
    public AbsenceDetails getAbsenceDetails() {
        return absenceDetails;
    }
}
