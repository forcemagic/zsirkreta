package com.speedyblur.kretaremastered.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Profile implements Parcelable {
    private final String cardid;
    private final String passwd;
    private final String friendlyName;

    public Profile(String cardid, String passwd, String friendlyName) {
        this.cardid = cardid;
        this.passwd = passwd;
        this.friendlyName = friendlyName;
    }

    protected Profile(Parcel in) {
        cardid = in.readString();
        passwd = in.readString();
        friendlyName = in.readString();
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    public boolean hasFriendlyName() {
        return !this.friendlyName.equals("");
    }

    @Override
    public int describeContents() {
        // Method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(cardid);
        parcel.writeString(passwd);
        parcel.writeString(friendlyName);
    }

    // Getter methods
    public String getCardid() {
        return cardid;
    }
    public String getPasswd() {
        return this.passwd;
    }
    public String getFriendlyName() {
        return friendlyName;
    }
}
