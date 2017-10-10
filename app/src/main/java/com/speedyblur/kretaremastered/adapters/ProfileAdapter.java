package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Profile;

import java.util.ArrayList;

public class ProfileAdapter extends ArrayAdapter<Profile> {
    private final Profile currentProfile;

    public ProfileAdapter(@NonNull Context context, @NonNull ArrayList<Profile> objects, Profile currentProfile) {
        super(context, 0, objects);
        this.currentProfile = currentProfile;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.profilelist_item, parent, false);

        Profile p = getItem(position);
        assert p != null;

        ((TextView) convertView.findViewById(R.id.profileTitle)).setText(p.hasFriendlyName() ? p.getFriendlyName() : p.getCardid());
        if (currentProfile.getCardid().equals(p.getCardid())) {
            TextView profileComment = convertView.findViewById(R.id.profileComment);
            profileComment.setText(R.string.nodelete_active_profile);
            profileComment.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
