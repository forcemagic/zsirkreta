package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;

import com.speedyblur.kretaremastered.models.Institute;

import java.util.ArrayList;

public class InstituteAdapter extends ArrayAdapter<Institute> {

    public InstituteAdapter(@NonNull Context context, ArrayList<Institute> institutes) {
        super(context, android.R.layout.simple_list_item_1, institutes);
    }
}
