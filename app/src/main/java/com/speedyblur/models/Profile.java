package com.speedyblur.models;

import java.util.ArrayList;
import java.util.Set;

public class Profile {
    public String id;
    public String pwd;

    public Profile(String stored) {
        String[] sliced = stored.split("@");
        id = sliced[0];
        pwd = sliced[1];
    }

    public static ArrayList<Profile> fromSet(Set<String> origSet) {
        ArrayList<Profile> profArray = new ArrayList<>();
        for (String cStr : origSet) {
            profArray.add(new Profile(cStr));
        }
        return profArray;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
