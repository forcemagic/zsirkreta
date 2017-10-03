package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.ProfileListActivity;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.shared.AccountStore;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DecryptionException;

import java.util.ArrayList;

public class ProfileAdapter extends ArrayAdapter<Profile> {
    private final ProfileListActivity parentActivity;

    public ProfileAdapter(Context ctxt, ArrayList<Profile> items) {
        super(ctxt, 0, items);
        this.parentActivity = (ProfileListActivity) ctxt;
    }

    @NonNull
    @Override
    public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
        ProfileViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.profile_item, parent, false);

            holder = new ProfileViewHolder();
            holder.profileName = convertView.findViewById(R.id.profileName);
            holder.profileId = convertView.findViewById(R.id.profileId);
            holder.profileDeleteBtn = convertView.findViewById(R.id.deleteProfileBtn);
            holder.profileDeleteBtn.setFocusable(false);
            holder.profileSeparator = convertView.findViewById(R.id.profileLineSeparator);

            convertView.setTag(holder);
        } else {
            holder = (ProfileViewHolder) convertView.getTag();
        }

        final Profile p = this.getItem(pos);
        assert p != null;

        holder.profileDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                deleteDialog.setTitle(R.string.dialog_delete_profile_title);
                deleteDialog.setMessage(R.string.dialog_delete_profile);
                deleteDialog.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            AccountStore ash = new AccountStore(getContext().getApplicationContext(), Common.SQLCRYPT_PWD);
                            ash.dropAccount(p.getCardid());
                            ash.close();

                            parentActivity.deleteProfile(p);
                        } catch (DecryptionException e) {
                            parentActivity.showOnSnackbar(R.string.decrypt_database_fail, Snackbar.LENGTH_LONG);
                        }
                        dialogInterface.dismiss();
                    }
                });
                deleteDialog.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                deleteDialog.show();
            }
        });

        if (p.hasFriendlyName()) {
            holder.profileName.setText(p.getFriendlyName());
            holder.profileId.setText(p.getCardid());
        } else {
            // TODO: Rewrite this awful centering
            holder.profileName.setText(p.getCardid());
            holder.profileName.setGravity(Gravity.CENTER_VERTICAL);
            holder.profileId.setVisibility(View.GONE);
        }

        if (pos == getCount() - 1) {
            holder.profileSeparator.setVisibility(View.INVISIBLE);
        } else {
            holder.profileSeparator.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    private static class ProfileViewHolder {
        TextView profileName;
        TextView profileId;
        ImageButton profileDeleteBtn;
        View profileSeparator;
    }
}
