package com.speedyblur.kretaremastered;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.speedyblur.adapters.ProfileAdapter;
import com.speedyblur.models.Profile;
import com.speedyblur.shared.AccountStoreHelper;
import com.speedyblur.shared.Vars;

import java.util.ArrayList;

public class ProfileListActivity extends AppCompatActivity {

    private final static int INTENT_REQ_NEWPROF = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilelist);
        updateProfileList();
    }

    public void goToNewProfile(View v) {
        Intent it = new Intent(ProfileListActivity.this, NewProfileActivity.class);
        startActivityForResult(it, INTENT_REQ_NEWPROF);
    }

    public void updateProfileList() {
        try {
            AccountStoreHelper ash = new AccountStoreHelper(getApplicationContext(), Vars.SQLCRYPT_PWD);
            ArrayList<Profile> profiles = ash.getAccounts();
            ash.close();
            ListView lv = (ListView) findViewById(R.id.profileList);
            final Context fixCtxt = this;
            ArrayAdapter strAdapter = new ProfileAdapter(this, profiles, new ProfileAdapter.ProfileAdapterCallback() {
                @Override
                public void onDeleteError(int errorMsgRes) {
                    Snackbar.make(findViewById(R.id.profListCoordinator), errorMsgRes, Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onLoginBegin() {
                    Snackbar.make(findViewById(R.id.profListCoordinator), R.string.logging_in, Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onLoginError(int errorMsgRes) {
                    Snackbar.make(findViewById(R.id.profListCoordinator), errorMsgRes, Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onLoginOk(String profileName) {
                    Intent it = new Intent(ProfileListActivity.this, MainActivity.class);
                    it.putExtra("profileName", profileName);
                    fixCtxt.startActivity(it);
                }

                @Override
                public void onDeleteOk() {
                    Snackbar.make(findViewById(R.id.profListCoordinator), R.string.profile_delete_success, Snackbar.LENGTH_LONG).show();
                    updateProfileList();
                }
            });
            lv.setAdapter(strAdapter);
            Log.d("ProfileList", String.format("We have %s profiles. List population complete.", profiles.size()));
        } catch (AccountStoreHelper.DatabaseDecryptionException e) {
            Snackbar.make(findViewById(R.id.profListCoordinator), R.string.decrypt_database_fail, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == INTENT_REQ_NEWPROF) {
            updateProfileList();
        }
    }
}
