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

    private final ArrayMap<String, String> headers = new ArrayMap<>();
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
            Log.d(LOGTAG, String.format("We have %s profile(s). List population complete.", profiles.size()));
        } catch (DecryptionException e) {
            showOnSnackbar(R.string.decrypt_database_fail, Snackbar.LENGTH_LONG);
        }
    }

    // LOADERS START HERE
    // TODO: Migrate loaders to a single class
    public void doLoadResourceLogin(String authToken, final Profile profile) {
        Log.d(LOGTAG, "Logged in. Loading 'things'...");

        headers.put("X-Auth-Token", authToken);

        HttpHandler.getJson(Common.APIBASE + "/bundle", headers, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                ArrayList<Grade> grades = new ArrayList<>();
                ArrayList<Average> averages = new ArrayList<>();
                ArrayList<AvgGraphData> avgGraphDatas = new ArrayList<>();
                ArrayList<Clazz> clazzes = new ArrayList<>();

                JSONArray rawGrades = resp.getJSONObject("grades").getJSONArray("data");
                JSONArray rawAvg = resp.getJSONObject("avg").getJSONArray("data");
                JSONArray rawGraphData = resp.getJSONObject("avggraph").getJSONArray("data");
                JSONArray rawClazzes = resp.getJSONObject("schedule").getJSONObject("data").getJSONArray("classes");
                //JSONArray rawAllDayEvents = resp.getJSONObject("schedule").getJSONObject("data").getJSONArray("allday");
                for (int i=0; i<rawGrades.length(); i++) {
                    JSONObject currentObj = rawGrades.getJSONObject(i);
                    Grade g = new Gson().fromJson(currentObj.toString(), Grade.class);
                    grades.add(g);
                }
                for (int i=0; i<rawAvg.length(); i++) {
                    JSONObject currentObj = rawAvg.getJSONObject(i);
                    Average avg = new Gson().fromJson(currentObj.toString(), Average.class);
                    averages.add(avg);
                }
                for (int i = 0; i<rawGraphData.length(); i++) {
                    JSONObject currentObj = rawGraphData.getJSONObject(i);
                    GsonBuilder gsonBuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
                    gsonBuilder.registerTypeAdapter(AvgGraphData.class, new AvgGraphDataDeserializer());
                    AvgGraphData agd = gsonBuilder.create().fromJson(currentObj.toString(), AvgGraphData.class);
                    avgGraphDatas.add(agd);
                }
                for (int i=0; i<rawClazzes.length(); i++) {
                    JSONObject currentObj = rawClazzes.getJSONObject(i);
                    GsonBuilder gsonBuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
                    gsonBuilder.registerTypeAdapter(Clazz.class, new ClazzDeserializer());
                    Clazz c = gsonBuilder.create().fromJson(currentObj.toString(), Clazz.class);
                    clazzes.add(c);
                }
                try {
                    DataStore ds = new DataStore(ctxt, profile.getCardid(), Common.SQLCRYPT_PWD);
                    ds.putGradesData(grades);
                    ds.putAveragesData(averages);
                    ds.putAverageGraphData(avgGraphDatas);
                    ds.upsertClassData(clazzes);
                    ds.close();
                } catch (DecryptionException e) {e.printStackTrace();}
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadAnnouncements(profile);
                    }
                });
            }

            @Override
            public void onFailure(int localizedError)   {
                askCachedVersion(profile, localizedError);
            }
        });
    }

    private void loadGrades(final Profile profile) {
        changeProgressStatus(R.string.loading_grades);
        HttpHandler.getJson(Common.APIBASE + "/grades", headers, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                try {
                    ArrayList<Grade> grades = new ArrayList<>();
                    for (int i=0; i<resp.getJSONArray("data").length(); i++) {
                        JSONObject currentObj = resp.getJSONArray("data").getJSONObject(i);
                        Grade g = new Gson().fromJson(currentObj.toString(), Grade.class);
                        grades.add(g);
                    }

                    // Commit
                    DataStore ds = new DataStore(ctxt, profile.getCardid(), Common.SQLCRYPT_PWD);
                    ds.putGradesData(grades);
                    ds.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadAverages(profile);
                        }
                    });
                } catch (DecryptionException e) {e.printStackTrace();}
            }

            @Override
            public void onFailure(int localizedError) {
                askCachedVersion(profile, localizedError);
            }
        });
    }

    private void loadAverages(final Profile profile) {
        changeProgressStatus(R.string.loading_averages);
        HttpHandler.getJson(Common.APIBASE + "/avg", headers, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                try {
                    ArrayList<Average> averages = new ArrayList<>();
                    for (int i=0; i<resp.getJSONArray("data").length(); i++) {
                        JSONObject currentObj = resp.getJSONArray("data").getJSONObject(i);
                        Average avg = new Gson().fromJson(currentObj.toString(), Average.class);
                        averages.add(avg);
                    }

                    // Commit
                    DataStore ds = new DataStore(ctxt, profile.getCardid(), Common.SQLCRYPT_PWD);
                    ds.putAveragesData(averages);
                    ds.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadAverageGraph(profile);
                        }
                    });
                } catch (DecryptionException e) {e.printStackTrace();}
            }

            @Override
            public void onFailure(int localizedError) {
                askCachedVersion(profile, localizedError);
            }
        });
    }

    private void loadAverageGraph(final Profile profile) {
        changeProgressStatus(R.string.loading_avggraph);
        HttpHandler.getJson(Common.APIBASE + "/avggraph", headers, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                try {
                    ArrayList<AvgGraphData> avgGraphDatas = new ArrayList<>();
                    for (int i = 0; i < resp.getJSONArray("data").length(); i++) {
                        JSONObject currentObj = resp.getJSONArray("data").getJSONObject(i);
                        GsonBuilder gsonBuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
                        gsonBuilder.registerTypeAdapter(AvgGraphData.class, new AvgGraphDataDeserializer());
                        AvgGraphData agd = gsonBuilder.create().fromJson(currentObj.toString(), AvgGraphData.class);
                        avgGraphDatas.add(agd);
                    }

                    // Commit
                    DataStore ds = new DataStore(ctxt, profile.getCardid(), Common.SQLCRYPT_PWD);
                    ds.putAverageGraphData(avgGraphDatas);
                    ds.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadSchedule(profile);
                        }
                    });
                } catch (DecryptionException e) {e.printStackTrace();}
            }

            @Override
            public void onFailure(int localizedError) {
                askCachedVersion(profile, localizedError);
            }
        });
    }

    private void loadSchedule(final Profile profile) {
        changeProgressStatus(R.string.loading_schedule);
        HttpHandler.getJson(Common.APIBASE + "/schedule?small", headers, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                try {
                    ArrayList<Clazz> clazzes = new ArrayList<>();
                    for (int i=0; i<resp.getJSONObject("data").getJSONArray("classes").length(); i++) {
                        JSONObject currentObj = resp.getJSONObject("data").getJSONArray("classes").getJSONObject(i);
                        GsonBuilder gsonBuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
                        gsonBuilder.registerTypeAdapter(Clazz.class, new ClazzDeserializer());
                        Clazz c = gsonBuilder.create().fromJson(currentObj.toString(), Clazz.class);
                        clazzes.add(c);
                    }

                    // Commit
                    DataStore ds = new DataStore(ctxt, profile.getCardid(), Common.SQLCRYPT_PWD);
                    ds.upsertClassData(clazzes);
                    ds.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadAnnouncements(profile);
                        }
                    });
                } catch (DecryptionException e) {e.printStackTrace();}
            }

            @Override
            public void onFailure(int localizedError)  {
                askCachedVersion(profile, localizedError);
            }
        });
    }

    private void loadAnnouncements(final Profile profile) {
        changeProgressStatus(R.string.loading_announcements);
        HttpHandler.getJson(Common.APIBASE + "/announcements", headers, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                try {
                    ArrayList<Announcement> announcements = new ArrayList<>();
                    for (int i=0; i<resp.getJSONArray("data").length(); i++) {
                        JSONObject currentObj = resp.getJSONArray("data").getJSONObject(i);
                        Announcement a = new Gson().fromJson(currentObj.toString(), Announcement.class);
                        announcements.add(a);
                    }

                    // Commit
                    DataStore ds = new DataStore(ctxt, profile.getCardid(), Common.SQLCRYPT_PWD);
                    ds.upsertAnnouncementsData(announcements);
                    ds.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadFinish(profile);
                        }
                    });
                } catch (DecryptionException e) {e.printStackTrace();}
            }

            @Override
            public void onFailure(int localizedError) {
                askCachedVersion(profile, localizedError);
            }
        });
    }

    private void loadFinish(Profile profile) {
        Intent it = new Intent(ProfileListActivity.this, MainActivity.class);
        it.putExtra("profile", profile);
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
                        cacheDialog.setMessage(R.string.dialog_show_cached);
                        cacheDialog.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadFinish(profile);
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

    public void showProgress(boolean doShow) {
        mViewFlipper.setDisplayedChild(doShow ? 1 : 0);
    }

    public void changeProgressStatus(@StringRes final int message) {
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

    public void goToNewProfile(View v) {
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
