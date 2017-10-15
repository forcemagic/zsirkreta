package com.speedyblur.kretaremastered.shared;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.speedyblur.kretaremastered.models.Bulletin;
import com.speedyblur.kretaremastered.models.Average;
import com.speedyblur.kretaremastered.models.AvgGraphData;
import com.speedyblur.kretaremastered.models.AvgGraphDataDeserializer;
import com.speedyblur.kretaremastered.models.Clazz;
import com.speedyblur.kretaremastered.models.ClazzDeserializer;
import com.speedyblur.kretaremastered.models.Grade;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.receivers.BirthdayReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Common {
    public static final String APIBASE = "https://www.speedyblur.com/kretaapi/v6";
    public static String SQLCRYPT_PWD = "weeee";

    public static String getLocalizedSubjectName(Context context, String subject) {
        int gotResxId = context.getResources().getIdentifier("subject_" + subject, "string", context.getPackageName());
        return gotResxId == 0 ? subject : context.getResources().getString(gotResxId);
    }

    // Account fetching
    public static void fetchAccountAsync(final Activity a, final Profile profile, final IFetchAccount ifa) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                                ArrayList<Grade> grades = new ArrayList<>();
                                ArrayList<Average> averages = new ArrayList<>();
                                ArrayList<AvgGraphData> avgGraphDatas = new ArrayList<>();
                                ArrayList<Clazz> clazzes = new ArrayList<>();
                                ArrayList<Bulletin> bulletins = new ArrayList<>();

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
                                    Bulletin a = new Gson().fromJson(currentObj.toString(), Bulletin.class);
                                    bulletins.add(a);
                                }

                                try {
                                    DataStore ds = new DataStore(a, profile.getCardid(), Common.SQLCRYPT_PWD);
                                    ds.putGradesData(grades);
                                    ds.putAveragesData(averages);
                                    ds.putAverageGraphData(avgGraphDatas);
                                    ds.upsertClassData(clazzes);
                                    ds.upsertAnnouncementsData(bulletins);
                                    ds.close();
                                } catch (DecryptionException e) {e.printStackTrace();}

                                final long loadEndTime = System.currentTimeMillis();
                                Log.v("KretaApi", "Fetch completed in " + String.valueOf(loadEndTime - loadBeginTime) + "ms");
                                a.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ifa.onFetchComplete();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(final int localizedError) {
                                a.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ifa.onFetchError(localizedError);
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onFailure(final int localizedError) {
                        a.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ifa.onFetchError(localizedError);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    public interface IFetchAccount {
        void onFetchComplete();
        void onFetchError(int localizedErrorMsg);
    }

    // Happy birthday reminder
    public static void registerBirthdayReminder(Context ctxt, Calendar date) {
        if (PendingIntent.getBroadcast(ctxt, 0, new Intent(ctxt, BirthdayReceiver.class), PendingIntent.FLAG_NO_CREATE) != null) {
            Log.w("AlarmManager", "Alarm already set, not setting another one.");
            return;
        }
        AlarmManager alarmMgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent it = new Intent(ctxt, BirthdayReceiver.class);
        PendingIntent pit = PendingIntent.getBroadcast(ctxt, 0, it, 0);

        alarmMgr.set(AlarmManager.RTC_WAKEUP, date.getTimeInMillis(), pit);
        Log.v("AlarmManager", String.format(Locale.ENGLISH, "Will fire BDay notfication at T-%d", (date.getTimeInMillis() - Calendar.getInstance().getTimeInMillis())));
    }
}
