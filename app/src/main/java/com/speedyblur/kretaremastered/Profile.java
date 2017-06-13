package com.speedyblur.kretaremastered;

import java.util.ArrayList;
import java.util.Set;

class Profile {
    String id;
    String pwd;

    Profile(String stored) {
        String[] sliced = stored.split("-");
        id = sliced[0];
        pwd = sliced[1];
    }

    static ArrayList<Profile> fromSet(Set<String> origSet) {
        ArrayList<Profile> profArray = new ArrayList<Profile>();
        for (String cStr : origSet) {
            profArray.add(new Profile(cStr));
        }
        return profArray;
    }
}
