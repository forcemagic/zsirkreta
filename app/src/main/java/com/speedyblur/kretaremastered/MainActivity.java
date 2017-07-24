package com.speedyblur.kretaremastered;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.speedyblur.adapters.AverageAdapter;
import com.speedyblur.adapters.SubjectAdapter;
import com.speedyblur.models.Average;
import com.speedyblur.models.Subject;
import com.speedyblur.shared.HttpHandler;
import com.speedyblur.shared.Vars;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.support.design.widget.Snackbar.make;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private ArrayMap<String, String> heads;
    private final Context sharedCtxt = this;

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

        //OkHttpClient htcli = new OkHttpClient();

        heads = new ArrayMap<>();
        heads.put("X-Auth-Token", Vars.AUTHTOKEN);

        fetchGrades();
    }

    private void fetchGrades() {
        make(findViewById(R.id.main_coord_view), R.string.loading_grades, Snackbar.LENGTH_INDEFINITE).show();
        HttpHandler.getJson(Vars.APIBASE + "/grades", heads, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                final ArrayList<Subject> subjects = Subject.fromJson(resp.getJSONArray("grades"));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ExpandableListView lv = (ExpandableListView) findViewById(R.id.mainGradeView);
                        lv.setAdapter(new SubjectAdapter(sharedCtxt, subjects));
                        fetchAverages();
                    }
                });
            }

            @Override
            public void onFailure(final int localizedError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        make(findViewById(R.id.main_coord_view), localizedError, Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void fetchAverages() {
        final Snackbar snackHandle = Snackbar.make(findViewById(R.id.main_coord_view), R.string.loading_averages, Snackbar.LENGTH_INDEFINITE);
        snackHandle.show();
        HttpHandler.getJson(Vars.APIBASE + "/avg", heads, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                final ArrayList<Average> averages = Average.fromJson(resp.getJSONArray("averages"));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView lv = (ListView) findViewById(R.id.avg_list);
                        lv.setAdapter(new AverageAdapter(sharedCtxt, averages));
                        snackHandle.dismiss();
                    }
                });
            }

            @Override
            public void onFailure(final int localizedError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        make(findViewById(R.id.main_coord_view), localizedError, Snackbar.LENGTH_LONG).show();
                    }
                });
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
