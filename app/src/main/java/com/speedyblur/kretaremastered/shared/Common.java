package com.speedyblur.kretaremastered.shared;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Announcement;
import com.speedyblur.kretaremastered.models.Average;
import com.speedyblur.kretaremastered.models.AvgGraphData;
import com.speedyblur.kretaremastered.models.AvgGraphDataDeserializer;
import com.speedyblur.kretaremastered.models.Clazz;
import com.speedyblur.kretaremastered.models.ClazzDeserializer;
import com.speedyblur.kretaremastered.models.Grade;
import com.speedyblur.kretaremastered.models.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Common {
    public static final String APIBASE = "https://www.speedyblur.com/kretaapi/v6";
    public static String SQLCRYPT_PWD = "weeee";

    public static String getLocalizedSubjectName(Context context, String subject) {
        int gotResxId = context.getResources().getIdentifier("subject_" + subject, "string", context.getPackageName());
        return gotResxId == 0 ? subject : context.getResources().getString(gotResxId);
    }

    public static void fetchAccountAsync(final Activity a, final Profile profile, final IFetchAccount ifa) {
        final int notifId = 1;
        final NotificationManager nmgr = (NotificationManager) a.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notif = new NotificationCompat.Builder(a);
        notif.setOngoing(true).setProgress(0, 0, true).setContentTitle(a.getString(R.string.loading_saving))
            .setSmallIcon(android.R.drawable.ic_popup_sync);

        nmgr.notify(1, notif.build());
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
                                    DataStore ds = new DataStore(a, profile.getCardid(), Common.SQLCRYPT_PWD);
                                    ds.putGradesData(grades);
                                    ds.putAveragesData(averages);
                                    ds.putAverageGraphData(avgGraphDatas);
                                    ds.upsertClassData(clazzes);
                                    ds.upsertAnnouncementsData(announcements);
                                    ds.close();
                                } catch (DecryptionException e) {e.printStackTrace();}

                                final long loadEndTime = System.currentTimeMillis();
                                Log.v("KretaApi", "Fetch completed in " + String.valueOf(loadEndTime - loadBeginTime) + "ms");
                                nmgr.cancel(notifId);
                                a.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ifa.onFetchComplete();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(final int localizedError) {
                                nmgr.cancel(notifId);
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
                        nmgr.cancel(notifId);
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
}
