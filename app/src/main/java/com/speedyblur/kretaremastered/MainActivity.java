package com.speedyblur.kretaremastered;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.ViewFlipper;

import com.speedyblur.adapters.SubjectAdapter;
import com.speedyblur.models.Subject;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(this.getClass().getSimpleName(), "Setting up View...");
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_grades);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Init ViewFlipper
        ((ViewFlipper) findViewById(R.id.main_viewflipper)).setDisplayedChild(0);
        Log.d(this.getClass().getSimpleName(), "Done setting up.");

        OkHttpClient htcli = new OkHttpClient();
        final Context sharedCtxt = this;

        Request req = new Request.Builder().header("X-Auth-Token", Vars.AUTHTOKEN).url(Vars.APIBASE+"/grades").build();
        htcli.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            @SuppressWarnings("ConstantConditions")     // TODO: Get rid of this ASAP.
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    Log.d("HttpClient", "Got 200 OK.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("PopulateView", "Populating ExpandableListView...");
                            try {
                                ExpandableListView lv = (ExpandableListView) findViewById(R.id.mainGradeView);
                                JSONArray jsObj = new JSONArray(response.body().string());
                                lv.setAdapter(new SubjectAdapter(sharedCtxt, Subject.fromJson(jsObj)));
                            } catch (JSONException | IOException e) {
                                dispatchError("Unable to populate ExpandableListView.", R.string.http_req_error);
                                e.printStackTrace();
                            }
                            Log.d("PopulateView", "Done populating.");
                        }
                    });
                }
            }
        });
        Log.d("HttpClient", "Enqueued request. Waiting for response...");

        // TODO: /avg request
    }

    private void dispatchError(String message, final int localizedMsgId) {
        Log.e("CoreHandler", message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(R.id.main_coord_view), localizedMsgId, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            this.startActivity(settingsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.main_viewflipper);

        if (id == R.id.nav_avgs) {
            toolbar.setTitle(R.string.title_activity_avgs);
            vf.setDisplayedChild(1);
        } else if (id == R.id.nav_grades) {
            toolbar.setTitle(R.string.title_activity_grades);
            vf.setDisplayedChild(0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
