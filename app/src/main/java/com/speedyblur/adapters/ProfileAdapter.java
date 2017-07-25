package com.speedyblur.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.models.Profile;
import com.speedyblur.shared.AccountStoreHelper;
import com.speedyblur.shared.HttpHandler;
import com.speedyblur.shared.Vars;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileAdapter extends ArrayAdapter<Profile> {

    private ProfileAdapterCallback cback;

    // TODO: WE GOT A CONTEXT, USE IT!
    public ProfileAdapter(Context ctxt, ArrayList<Profile> items, ProfileAdapterCallback cback) {
        super(ctxt, 0, items);
        this.cback = cback;
    }

    @NonNull
    @Override
    public View getView(int pos, View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.profile_item, parent, false);

        TextView mProfileName = convertView.findViewById(R.id.profileName);
        TextView mProfileId = convertView.findViewById(R.id.profileId);
        ImageButton mProfileDelete = convertView.findViewById(R.id.deleteProfileBtn);
        final Profile p = this.getItem(pos);
        assert p != null;

        mProfileName.setOnClickListener(new LoginClickListener(p, this.cback));
        mProfileId.setOnClickListener(new LoginClickListener(p, this.cback));

        mProfileDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AccountStoreHelper ash = new AccountStoreHelper(getContext().getApplicationContext(), "asd");
                    ash.dropAccount(p.cardid);
                    ash.close();
                    cback.onDeleteOk();
                } catch (AccountStoreHelper.DatabaseDecryptionException e) {
                    cback.onDeleteError(R.string.decrypt_database_fail);
                }
            }
        });

        if (p.hasFriendlyName()) {
            mProfileName.setText(p.friendlyName);
            mProfileId.setText("("+p.cardid+")");
        } else {
            mProfileName.setText(p.cardid);
            mProfileId.setText("");
        }

        return convertView;
    }

    private class LoginClickListener implements View.OnClickListener {

        private ProfileAdapterCallback cback;
        private Profile p;

        private LoginClickListener(Profile p, ProfileAdapterCallback cback) {
            this.cback = cback;
            this.p = p;
        }

        @Override
        public void onClick(View view) {
            cback.onLoginBegin();

            JSONObject payload = new JSONObject();
            try {
                payload.put("username", p.cardid);
                payload.put("password", p.getPasswd());
            } catch (JSONException e) { e.printStackTrace(); }

            // Enqueue request
            HttpHandler.postJson(Vars.APIBASE + "/auth", payload, new HttpHandler.JsonRequestCallback() {
                @Override
                public void onComplete(JSONObject resp) throws JSONException {
                    Vars.AUTHTOKEN = resp.getString("token");
                    cback.onLoginOk();
                }

                @Override
                public void onFailure(final int localizedError) {
                    cback.onLoginError(localizedError);
                }
            });

        }
    }

    public interface ProfileAdapterCallback {
        void onDeleteOk();
        void onDeleteError(int errorMsgRes);
        void onLoginBegin();
        void onLoginError(int errorMsgRes);
        void onLoginOk();
    }
}
