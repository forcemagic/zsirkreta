package com.speedyblur.kretaremastered.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.adapters.ProfileAdapter;
import com.speedyblur.kretaremastered.models.Announcement;
import com.speedyblur.kretaremastered.models.Average;
import com.speedyblur.kretaremastered.models.AvgGraphData;
import com.speedyblur.kretaremastered.models.AvgGraphDataDeserializer;
import com.speedyblur.kretaremastered.models.Clazz;
import com.speedyblur.kretaremastered.models.ClazzDeserializer;
import com.speedyblur.kretaremastered.models.Grade;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.shared.AccountStore;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;
import com.speedyblur.kretaremastered.shared.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class ProfileListActivity extends AppCompatActivity {

    private final static String LOGTAG = "ProfileList";
    private final static int INTENT_REQ_NEWPROF = 1;
    private final Context ctxt = this;

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
            AccountStore ash = new AccountStore(getApplicationContext(), Common.SQLCRYPT_PWD);
            profiles = ash.getAccounts();
            ash.close();

            profileAdapter = new ProfileAdapter(this, profiles);
            mProfileList.setAdapter(profileAdapter);
            mProfileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    doLoadResourceLogin((Profile) adapterView.getItemAtPosition(i));
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
                    doLoadResourceLogin(profiles.get(i));
                    showOnSnackbar(R.string.login_lastprofile, Snackbar.LENGTH_SHORT);
                }
            }
        }
    }

    // LOADERS START HERE
    public void doLoadResourceLogin(final Profile profile) {
        showProgress(true);
        changeProgressStatus(R.string.loading_logging_in);

        JSONObject payload = new JSONObject();
        try {
            payload.put("username", profile.getCardid());
            payload.put("password", profile.getPasswd());
        } catch (JSONException e) { e.printStackTrace(); }

        HttpHandler.postJson(Common.APIBASE + "/auth", payload, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                ArrayMap<String, String> headers = new ArrayMap<>();
                headers.put("X-Auth-Token", resp.getString("token"));

                final long loadBeginTime = System.currentTimeMillis();
                HttpHandler.getJson(Common.APIBASE + "/bundle", headers, new HttpHandler.JsonRequestCallback() {
                    @Override
                    public void onComplete(JSONObject resp) throws JSONException {
                        changeProgressStatus(R.string.loading_saving);

                        ArrayList<Grade> grades = new ArrayList<>();
                        ArrayList<Average> averages = new ArrayList<>();
                        ArrayList<AvgGraphData> avgGraphDatas = new ArrayList<>();
                        ArrayList<Clazz> clazzes = new ArrayList<>();
                        ArrayList<Announcement> announcements = new ArrayList<>();

                        JSONArray rawGrades = resp.getJSONObject("grades").getJSONArray("data");
                        JSONArray rawAvg = resp.getJSONObject("avg").getJSONArray("data");
                        JSONArray rawGraphData = resp.getJSONObject("avggraph").getJSONArray("data");
                        JSONArray rawClazzes = resp.getJSONObject("schedule").getJSONObject("data").getJSONArray("classes");
                        //JSONArray rawAllDayEvents = resp.getJSONObject("schedule").getJSONObject("data").getJSONArray("allday");
                        JSONArray rawAnnouncements = resp.getJSONObject("announcements").getJSONArray("data");
                        for (int i = 0; i < rawGrades.length(); i++) {
                            JSONObject currentObj = rawGrades.getJSONObject(i);
                            Grade g = new Gson().fromJson(currentObj.toString(), Grade.class);
                            grades.add(g);
                        }
                        for (int i = 0; i < rawAvg.length(); i++) {
                            JSONObject currentObj = rawAvg.getJSONObject(i);
                            Average avg = new Gson().fromJson(currentObj.toString(), Average.class);
                            averages.add(avg);
                        }
                        for (int i = 0; i < rawGraphData.length(); i++) {
                            JSONObject currentObj = rawGraphData.getJSONObject(i);
                            GsonBuilder gsonBuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
                            gsonBuilder.registerTypeAdapter(AvgGraphData.class, new AvgGraphDataDeserializer());
                            AvgGraphData agd = gsonBuilder.create().fromJson(currentObj.toString(), AvgGraphData.class);
                            avgGraphDatas.add(agd);
                        }
                        for (int i = 0; i < rawClazzes.length(); i++) {
                            JSONObject currentObj = rawClazzes.getJSONObject(i);
                            GsonBuilder gsonBuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
                            gsonBuilder.registerTypeAdapter(Clazz.class, new ClazzDeserializer());
                            Clazz c = gsonBuilder.create().fromJson(currentObj.toString(), Clazz.class);
                            clazzes.add(c);
                        }
                        for (int i = 0; i < rawAnnouncements.length(); i++) {
                            JSONObject currentObj = rawAnnouncements.getJSONObject(i);
                            Announcement a = new Gson().fromJson(currentObj.toString(), Announcement.class);
                            announcements.add(a);
                        }

                        try {
                            DataStore ds = new DataStore(ctxt, profile.getCardid(), Common.SQLCRYPT_PWD);
                            ds.putGradesData(grades);
                            ds.putAveragesData(averages);
                            ds.putAverageGraphData(avgGraphDatas);
                            ds.upsertClassData(clazzes);
                            ds.upsertAnnouncementsData(announcements);
                            ds.close();
                        } catch (DecryptionException e) {e.printStackTrace();}

                        final long loadEndTime = System.currentTimeMillis();
                        Log.v("KretaApi", "Fetch completed in " + String.valueOf(loadEndTime - loadBeginTime) + "ms");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadFinish(profile, (int) (loadEndTime - loadBeginTime));
                            }
                        });
                    }

                    @Override
                    public void onFailure(int localizedError) {
                        askCachedVersion(profile, localizedError);
                    }
                });
            }

            @Override
            public void onFailure(int localizedError) {
                askCachedVersion(profile, localizedError);
            }
        });
    }

    private void loadFinish(Profile profile, int loadTime) {
        getSharedPreferences("main", MODE_PRIVATE).edit().putString("lastUsedProfile", profile.getCardid()).apply();
        Intent it = new Intent(ProfileListActivity.this, MainActivity.class);
        it.putExtra("profile", profile);
        it.putExtra("loadtime", loadTime);
        startActivity(it);
        showProgress(false);
    }
    // LOADERS END HERE

    public void askCachedVersion(final Profile profile, @StringRes final int originalMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false);
                try {
                    DataStore ds = new DataStore(ctxt, profile.getCardid(), Common.SQLCRYPT_PWD);
                    Calendar cal = ds.getLastSave();
                    ds.close();

                    if (cal != null) {
                        AlertDialog.Builder cacheDialog = new AlertDialog.Builder(ctxt);
                        cacheDialog.setTitle(R.string.dialog_show_cached_title);
                        cacheDialog.setMessage(getString(R.string.dialog_show_cached, getString(originalMsg)));
                        cacheDialog.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadFinish(profile, -1);
                                dialogInterface.dismiss();
                            }
                        });
                        cacheDialog.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        cacheDialog.show();
                    } else {
                        showOnSnackbar(originalMsg, Snackbar.LENGTH_LONG);
                    }
                } catch (DecryptionException e) {
                    showOnSnackbar(R.string.decrypt_database_fail, Snackbar.LENGTH_LONG);
                }
            }
        });
    }

    private void showProgress(boolean doShow) {
        mViewFlipper.setDisplayedChild(doShow ? 1 : 0);
    }

    private void changeProgressStatus(@StringRes final int message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.loadingStatusText)).setText(message);
            }
        });
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
