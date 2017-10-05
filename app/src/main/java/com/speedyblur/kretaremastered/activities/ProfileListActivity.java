package com.speedyblur.kretaremastered.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.adapters.ProfileAdapter;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.shared.AccountStore;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DecryptionException;

import java.util.ArrayList;

public class ProfileListActivity extends AppCompatActivity {

    private final static String LOGTAG = "ProfileList";
    private final static int INTENT_REQ_NEWPROF = 1;

    private ArrayList<Profile> profiles;
    private ArrayAdapter<Profile> profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilelist);

        // Setting up ListView
        ListView mProfileList = (ListView) findViewById(R.id.profileList);
        mProfileList.setEmptyView(findViewById(R.id.emptyListViewText));

        try {
            AccountStore ash = new AccountStore(getApplicationContext(), Common.SQLCRYPT_PWD);
            profiles = ash.getAccounts();
            ash.close();

            profileAdapter = new ProfileAdapter(this, profiles);
            mProfileList.setAdapter(profileAdapter);
            mProfileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    navToProfile(profileAdapter.getItem(i));
                }
            });
            Log.d(LOGTAG, String.format("We have %s profile(s). List population complete.", profiles.size()));
        } catch (DecryptionException e) {
            showOnSnackbar(R.string.decrypt_database_fail, Snackbar.LENGTH_LONG);
        }

        String lastUsedProfile = getSharedPreferences("main", MODE_PRIVATE).getString("lastUsedProfile", "");
        if (!lastUsedProfile.equals("")) {
            for (int i=0; i<profiles.size(); i++) {
                if (profiles.get(i).getCardid().equals(lastUsedProfile)) {
                    navToProfile(profiles.get(i));
                    showOnSnackbar(R.string.login_lastprofile, Snackbar.LENGTH_SHORT);
                }
            }
        }
    }

    // LOADERS START HERE
    private void navToProfile(Profile profile) {
        getSharedPreferences("main", MODE_PRIVATE).edit().putString("lastUsedProfile", profile.getCardid()).apply();
        Intent it = new Intent(ProfileListActivity.this, MainActivity.class);
        it.putExtra("profile", profile);
        startActivity(it);
    }
    // LOADERS END HERE

    public void showOnSnackbar(@StringRes final int message, final int length) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(R.id.profListCoordinator), message, length).show();
            }
        });
    }

    public void deleteProfile(Profile p) {
        if (profiles.remove(p)) {
            profileAdapter.notifyDataSetChanged();
            showOnSnackbar(R.string.profile_delete_success, Snackbar.LENGTH_LONG);
        }
    }

    public void goToNewProfile(@SuppressWarnings("UnusedParameters") View v) {
        Intent it = new Intent(ProfileListActivity.this, NewProfileActivity.class);
        startActivityForResult(it, INTENT_REQ_NEWPROF);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == INTENT_REQ_NEWPROF) {
            if (data != null && data.hasExtra("profile")) {
                Profile newProf = data.getParcelableExtra("profile");
                profiles.add(newProf);
                profileAdapter.notifyDataSetChanged();
            }
        }
    }
}
