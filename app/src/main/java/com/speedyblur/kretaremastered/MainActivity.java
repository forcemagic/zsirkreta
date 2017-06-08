package com.speedyblur.kretaremastered;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Grades");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //
        ((ViewFlipper) findViewById(R.id.main_viewflipper)).setDisplayedChild(0);

        // Start request
        RequestQueue mReqQueue;
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        mReqQueue = new RequestQueue(cache, new BasicNetwork(new HurlStack()));
        mReqQueue.start();

        final Context localCtxt = this;

        JsonArrayRequest jsoReqGrades = new JsonArrayRequest(Request.Method.GET, Vars.APIBASE + "/grades", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(final JSONArray response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ExpandableListView lv = (ExpandableListView) findViewById(R.id.mainGradeView);
                        lv.setAdapter(new SubjectAdapter(localCtxt, Subject.fromJson(response)));
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Login ERR: "+error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("X-Auth-Token", Vars.AUTHTOKEN);
                return params;
            }
        };

        JsonArrayRequest jsoReqAvgs = new JsonArrayRequest(Request.Method.GET, Vars.APIBASE + "/avg", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(final JSONArray response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView lv = (ListView) findViewById(R.id.avg_list);
                        lv.setAdapter(new AverageAdapter(localCtxt, Average.fromJson(response)));
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Login ERR: "+error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("X-Auth-Token", Vars.AUTHTOKEN);
                return params;
            }
        };

        mReqQueue.add(jsoReqGrades);
        mReqQueue.add(jsoReqAvgs);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.main_viewflipper);

        if (id == R.id.nav_avgs) {
            vf.setDisplayedChild(1);
        } else if (id == R.id.nav_grades) {
            vf.setDisplayedChild(0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
