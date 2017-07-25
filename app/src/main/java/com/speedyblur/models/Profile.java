package com.speedyblur.models;

public class Profile {
    public String cardid;
    private String passwd;      // Not that this adds much to security, but just in case
    public String friendlyName;

    public Profile(String cardid, String passwd, String friendlyName) {
        this.cardid = cardid;
        this.passwd = passwd;
        this.friendlyName = friendlyName;
    }

    public String getPasswd() {
        return this.passwd;
    }

    public boolean hasFriendlyName() {
        return !this.friendlyName.equals("");
    }
}
