package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Profile;

import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    private final ArrayList<Profile> profiles;
    private final String currentProfileId;

    public ProfileAdapter(ArrayList<Profile> profiles, String currentProfileId) {
        this.profiles = profiles;
        this.currentProfileId = currentProfileId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater infl = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ViewHolder(infl.inflate(R.layout.profilelist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Profile p = profiles.get(position);

        if (currentProfileId.equals(p.getCardid()))
            holder.profileComment.setVisibility(View.VISIBLE);
        else holder.profileComment.setVisibility(View.GONE);

        holder.profileTitle.setText(p.hasFriendlyName() ? p.getFriendlyName() : p.getCardid());
        holder.profileDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView profileDelete;
        final TextView profileTitle;
        final TextView profileComment;

        ViewHolder(View itemView) {
            super(itemView);

            profileDelete = itemView.findViewById(R.id.profileDeleteIcon);
            profileTitle = itemView.findViewById(R.id.profileTitle);
            profileComment = itemView.findViewById(R.id.profileComment);
        }
    }
}
