package com.speedyblur.kretaremastered;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

class ProfileAdapter extends ArrayAdapter<Profile> {
    ProfileAdapter(@NonNull Context context, ArrayList<Profile> items) {
        super(context, android.R.layout.simple_list_item_1, items);
    }
}
