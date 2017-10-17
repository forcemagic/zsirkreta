package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.ManageProfilesActivity;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.shared.AccountStore;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DecryptionException;

import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    private final ArrayList<Profile> profiles;
    private final String currentProfileId;
    private final ManageProfilesActivity parent;

    public ProfileAdapter(ManageProfilesActivity parent, ArrayList<Profile> profiles, String currentProfileId) {
        this.profiles = profiles;
        this.currentProfileId = currentProfileId;
        this.parent = parent;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater infl = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ViewHolder(infl.inflate(R.layout.profilelist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Profile p = profiles.get(position);

        if (currentProfileId.equals(p.getCardid())) {
            holder.profileComment.setVisibility(View.VISIBLE);
            holder.profileDelete.setColorFilter(ContextCompat.getColor(holder.profileDelete.getContext(),
                    android.R.color.darker_gray), PorterDuff.Mode.SRC_ATOP);
        } else {
            holder.profileComment.setVisibility(View.GONE);
            holder.profileDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parent.pendingProfileDelete = p;
                    final int lastpos = holder.getAdapterPosition();

                    final BaseTransientBottomBar.BaseCallback<Snackbar> deleteCallback = new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            try {
                                AccountStore as = new AccountStore(holder.profileDelete.getContext(), Common.SQLCRYPT_PWD);
                                as.dropAccount(p.getCardid());
                                as.close();
                                parent.pendingProfileDelete = null;
                            } catch (DecryptionException e) {e.printStackTrace();}
                            super.onDismissed(transientBottomBar, event);
                        }
                    };

                    final Snackbar s = Snackbar.make(parent.findViewById(R.id.manageProfilesCoordinator), R.string.profile_deleted_success, Snackbar.LENGTH_LONG)
                            .addCallback(deleteCallback);

                    s.setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            s.removeCallback(deleteCallback);
                            profiles.add(lastpos, p);
                            notifyItemInserted(holder.getAdapterPosition());
                        }
                    }).show();

                    profiles.remove(p);
                    notifyItemRemoved(holder.getAdapterPosition());
                }
            });
        }

        holder.profileTitle.setText(p.hasFriendlyName() ? p.getFriendlyName() : p.getCardid());
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
