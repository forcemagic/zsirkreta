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
import com.speedyblur.kretaremastered.shared.HttpHandler;

import org.json.JSONException;
import org.json.JSONObject;

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
        convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.profile_item, parent, false);

        // UI Refs
        TextView mProfileName = convertView.findViewById(R.id.profileName);
        TextView mProfileId = convertView.findViewById(R.id.profileId);
        ImageButton mProfileDelete = convertView.findViewById(R.id.deleteProfileBtn);
        View mProfLineSep = convertView.findViewById(R.id.profileLineSeparator);

        final Profile p = this.getItem(pos);
        assert p != null;

        convertView.setOnClickListener(new LoginClickListener(p));
        mProfileDelete.setOnClickListener(new View.OnClickListener() {
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
            mProfileName.setText(p.getFriendlyName());
            mProfileId.setText(p.getCardid());
        } else {
            // TODO: Rewrite this awful centering
            mProfileName.setText(p.getCardid());
            mProfileName.setGravity(Gravity.CENTER_VERTICAL);
            mProfileId.setVisibility(View.GONE);
        }

        if (pos == getCount() - 1) {
            mProfLineSep.setVisibility(View.INVISIBLE);
        } else {
            mProfLineSep.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    private class LoginClickListener implements View.OnClickListener {
        private final Profile p;

        private LoginClickListener(Profile p) {
            this.p = p;
        }

        @Override
        public void onClick(View view) {
            parentActivity.showProgress(true);
            parentActivity.changeProgressStatus(R.string.loading_logging_in);

            JSONObject payload = new JSONObject();
            try {
                payload.put("username", p.getCardid());
                payload.put("password", p.getPasswd());
            } catch (JSONException e) { e.printStackTrace(); }

            // Enqueue request
            HttpHandler.postJson(Common.APIBASE + "/auth", payload, new HttpHandler.JsonRequestCallback() {
                @Override
                public void onComplete(JSONObject resp) throws JSONException {
                    String authToken = resp.getString("token");
                    parentActivity.doLoadResourceLogin(authToken, p);
                }

                @Override
                public void onFailure(int localizedError) {
                    parentActivity.askCachedVersion(p, localizedError);
                }
            });
        }
    }
}
