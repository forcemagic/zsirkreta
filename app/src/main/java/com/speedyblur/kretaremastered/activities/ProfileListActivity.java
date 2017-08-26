package com.speedyblur.kretaremastered.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.google.gson.Gson;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.adapters.ProfileAdapter;
import com.speedyblur.kretaremastered.models.Average;
import com.speedyblur.kretaremastered.models.Grade;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.shared.AccountStore;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;
import com.speedyblur.kretaremastered.shared.HttpHandler;
import com.speedyblur.kretaremastered.shared.Vars;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileListActivity extends AppCompatActivity {

    private final static String LOGTAG = "ProfileList";
    private final static int INTENT_REQ_NEWPROF = 1;
    private Context ctxt = this;

    private ArrayMap<String, String> headers = new ArrayMap<>();
    private ArrayList<Profile> profiles;
    private ArrayAdapter<Profile> profileAdapter;
    private ViewFlipper mViewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilelist);

        // Setting up ListView
        ListView mProfileList = (ListView) findViewById(R.id.profileList);
        mProfileList.setEmptyView(findViewById(R.id.emptyListViewText));

        // Setting up ViewFlipper (for the progressBar)
        mViewFlipper = (ViewFlipper) findViewById(R.id.profileSelectorFlipper);
        mViewFlipper.setDisplayedChild(0);

        try {
            AccountStore ash = new AccountStore(getApplicationContext(), Vars.SQLCRYPT_PWD);
            profiles = ash.getAccounts();
            ash.close();

            profileAdapter = new ProfileAdapter(this, profiles);
            mProfileList.setAdapter(profileAdapter);
            Log.d(LOGTAG, String.format("We have %s profiles. List population complete.", profiles.size()));
        } catch (DecryptionException e) {
            showOnSnackbar(R.string.decrypt_database_fail, Snackbar.LENGTH_LONG);
        }
    }

    // LOADERS START HERE
    public void doLoadResourceLogin(String authToken, Profile profile) {
        Log.d(LOGTAG, "Logged in. Loading 'things'...");

        headers.put("X-Auth-Token", authToken);
        loadGrades(profile);
    }

    private void loadGrades(final Profile profile) {
        HttpHandler.getJson(Vars.APIBASE + "/grades", headers, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                try {
                    ArrayList<Grade> grades = new ArrayList<>();
                    for (int i=0; i<resp.getJSONArray("data").length(); i++) {
                        JSONObject currentObj = resp.getJSONArray("data").getJSONObject(i);
                        Grade g = new Gson().fromJson(currentObj.toString(), Grade.class);      // TODO: This toString method doesn't look all right to me
                        grades.add(g);
                    }

                    // Commit
                    DataStore ds = new DataStore(ctxt, profile.getCardid(), Vars.SQLCRYPT_PWD);
                    ds.putGradesData(grades);
                    ds.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadAverages(profile);
                        }
                    });
                } catch (DecryptionException e) {
                    showOnSnackbar(R.string.decrypt_database_fail, Snackbar.LENGTH_LONG);
                }
            }

            @Override
            public void onFailure(int localizedError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        askCachedVersion();
                    }
                });
            }
        });
    }

    private void loadAverages(final Profile profile) {
        HttpHandler.getJson(Vars.APIBASE + "/avg", headers, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                try {
                    ArrayList<Average> averages = new ArrayList<>();
                    for (int i=0; i<resp.getJSONArray("data").length(); i++) {
                        JSONObject currentObj = resp.getJSONArray("data").getJSONObject(i);
                        Average avg = new Gson().fromJson(currentObj.toString(), Average.class);      // TODO: This toString method doesn't look all right to me
                        averages.add(avg);
                    }

                    // Commit
                    DataStore ds = new DataStore(ctxt, profile.getCardid(), Vars.SQLCRYPT_PWD);
                    ds.putAveragesData(averages);
                    ds.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadFinish(profile);
                        }
                    });
                } catch (DecryptionException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            askCachedVersion();
                        }
                    });
                }
            }

            @Override
            public void onFailure(int localizedError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        askCachedVersion();
                    }
                });
            }
        });
    }

    private void loadFinish(Profile profile) {
        Intent it = new Intent(ProfileListActivity.this, MainActivity.class);
        it.putExtra("profile", profile);
        startActivity(it);
    }
    // LOADERS END HERE

    public void askCachedVersion() {
        // TODO: Show dialog and ask whether show cached result
    }

    public void showProgress(boolean doShow) {
        mViewFlipper.setDisplayedChild(doShow ? 1 : 0);
    }

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

    public void goToNewProfile(View v) {
        Intent it = new Intent(ProfileListActivity.this, NewProfileActivity.class);
        startActivityForResult(it, INTENT_REQ_NEWPROF);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == INTENT_REQ_NEWPROF) {
            Profile newProf = data.getParcelableExtra("profile");
            profiles.add(newProf);
            profileAdapter.notifyDataSetChanged();
        }
    }
}
