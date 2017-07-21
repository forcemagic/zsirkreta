package com.speedyblur.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import com.speedyblur.models.Profile;

import java.util.ArrayList;

public class ProfileAdapter extends ArrayAdapter<Profile> {
    public ProfileAdapter(@NonNull Context context, ArrayList<Profile> items) {
        super(context, android.R.layout.simple_list_item_1, items);
    }
}
