package com.speedyblur.kretaremastered.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.speedyblur.kretaremastered.activities.ProfileListActivity;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.shared.AccountStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;
import com.speedyblur.kretaremastered.shared.HttpHandler;
import com.speedyblur.kretaremastered.shared.Vars;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileAdapter extends ArrayAdapter<Profile> {
    private ProfileListActivity parentActivity;

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
                try {
                    AccountStore ash = new AccountStore(getContext().getApplicationContext(), Vars.SQLCRYPT_PWD);
                    ash.dropAccount(p.getCardid());
                    ash.close();

                    parentActivity.deleteProfile(p);
                } catch (DecryptionException e) {
                    parentActivity.showOnSnackbar(R.string.decrypt_database_fail, Snackbar.LENGTH_LONG);
                }
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
        private Profile p;

        private LoginClickListener(Profile p) {
            this.p = p;
        }

        @Override
        public void onClick(View view) {
            parentActivity.showProgress(true);

            JSONObject payload = new JSONObject();
            try {
                payload.put("username", p.getCardid());
                payload.put("password", p.getPasswd());
            } catch (JSONException e) { e.printStackTrace(); }

            // Enqueue request
            HttpHandler.postJson(Vars.APIBASE + "/auth", payload, new HttpHandler.JsonRequestCallback() {
                @Override
                public void onComplete(JSONObject resp) throws JSONException {
                    String authToken = resp.getString("token");
                    parentActivity.doLoadResourceLogin(authToken, p);
                }

                @Override
                public void onFailure(final int localizedError) {
                    parentActivity.showProgress(false);
                    parentActivity.showOnSnackbar(localizedError, Snackbar.LENGTH_LONG);
                }
            });
        }
    }
}
