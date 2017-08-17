package com.speedyblur.kretaremastered;

import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.speedyblur.adapters.AverageAdapter;
import com.speedyblur.adapters.DatedGradeAdapter;
import com.speedyblur.adapters.GroupedGradeAdapter;
import com.speedyblur.models.Average;
import com.speedyblur.models.Grade;
import com.speedyblur.models.GradeGroup;
import com.speedyblur.shared.HttpHandler;
import com.speedyblur.shared.Vars;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.support.design.widget.Snackbar.make;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private ArrayMap<String, String> heads;
    private ArrayList<Average> averages;
    private ArrayList<Grade> allGrades;
    private final Context sharedCtxt = this;
    private double loadTime;
    private boolean shouldShowMenu = true;
    private String lastMenuState;

    // UI ref
    private ViewFlipper vf;
    private ViewFlipper gVf;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_grades);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.mainNav);
        navigationView.setNavigationItemSelectedListener(this);

        // Set profile name to drawer header
        TextView profNameHead = navigationView.getHeaderView(0).findViewById(R.id.profileNameHead);
        profNameHead.setText(getIntent().getStringExtra("profileName"));

        vf = (ViewFlipper) findViewById(R.id.main_viewflipper);
        gVf = (ViewFlipper) findViewById(R.id.gradeOrderFlipper);

        heads = new ArrayMap<>();
        heads.put("X-Auth-Token", Vars.AUTHTOKEN);

        if (savedInstanceState != null) {
            allGrades = savedInstanceState.getParcelableArrayList("allGrades");
            averages = savedInstanceState.getParcelableArrayList("averages");

            // Repopulate views
            ExpandableListView gradeList = (ExpandableListView) findViewById(R.id.mainGradeView);
            gradeList.setDividerHeight(0);
            gradeList.setAdapter(new GroupedGradeAdapter(sharedCtxt, allGrades, "subject", new GradeGroup.FormatHelper() {
                @Override
                public String doFormat(String in) {
                    int gotResxId = getResources().getIdentifier("subject_" + in, "string", getPackageName());
                    return gotResxId == 0 ? in : getResources().getString(gotResxId);
                }
            }, new GradeGroup.SameGroupComparator() {
                @Override
                public boolean compare(String id1, String id2) {
                    return id1.equals(id2);
                }
            }));

            ListView avgList = (ListView) findViewById(R.id.avg_list);
            avgList.setAdapter(new AverageAdapter(sharedCtxt, averages));

            ListView dateOrderedLv = (ListView) findViewById(R.id.datedGradeList);
            try {
                dateOrderedLv.setAdapter(new DatedGradeAdapter(sharedCtxt, GradeGroup.assembleGroups(allGrades, "gotDate", new GradeGroup.SameGroupComparator() {
                    @Override
                    public boolean compare(String id1, String id2) {
                        Calendar cal1 = Calendar.getInstance();
                        Calendar cal2 = Calendar.getInstance();
                        cal1.setTimeInMillis(Long.parseLong(id1)*1000);
                        cal2.setTimeInMillis(Long.parseLong(id2)*1000);
                        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH));
                    }
                }, false, true)));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            // Viewflipper reset
            vf.setDisplayedChild(savedInstanceState.getInt("viewFlipperState"));
            gVf.setDisplayedChild(savedInstanceState.getInt("gradeViewFlipperState"));
            if (shouldShowMenu) lastMenuState = savedInstanceState.getString("sortingTitle");
        } else {
            vf.setDisplayedChild(0);
            gVf.setDisplayedChild(0);
            fetchGrades();
        }
    }

    private void fetchGrades() {
        make(findViewById(R.id.main_coord_view), R.string.loading_grades, Snackbar.LENGTH_INDEFINITE).show();
        HttpHandler.getJson(Vars.APIBASE + "/grades", heads, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                allGrades = Grade.fromJson(resp.getJSONArray("data"));
                loadTime = resp.getDouble("fetch_time");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Subject-ordered
                        ExpandableListView lv = (ExpandableListView) findViewById(R.id.mainGradeView);
                        lv.setDividerHeight(0);
                        lv.setAdapter(new GroupedGradeAdapter(sharedCtxt, allGrades, "subject", new GradeGroup.FormatHelper() {
                            @Override
                            public String doFormat(String in) {
                                int gotResxId = getResources().getIdentifier("subject_"+in, "string", getPackageName());
                                return gotResxId == 0 ? in : getResources().getString(gotResxId);
                            }
                        }, new GradeGroup.SameGroupComparator() {
                            @Override
                            public boolean compare(String id1, String id2) {
                                return id1.equals(id2);
                            }
                        }));

                        // Date-ordered
                        ListView dateOrderedLv = (ListView) findViewById(R.id.datedGradeList);
                        try {
                            dateOrderedLv.setAdapter(new DatedGradeAdapter(sharedCtxt, GradeGroup.assembleGroups(allGrades, "gotDate", new GradeGroup.SameGroupComparator() {
                                @Override
                                public boolean compare(String id1, String id2) {
                                    Calendar cal1 = Calendar.getInstance();
                                    Calendar cal2 = Calendar.getInstance();
                                    cal1.setTimeInMillis(Long.parseLong(id1)*1000);
                                    cal2.setTimeInMillis(Long.parseLong(id2)*1000);
                                    return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH));
                                }
                            }, false, true)));
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }

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
        Snackbar.make(findViewById(R.id.main_coord_view), R.string.loading_averages, Snackbar.LENGTH_INDEFINITE).show();
        HttpHandler.getJson(Vars.APIBASE + "/avg", heads, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                averages = Average.fromJson(resp.getJSONArray("data"));
                loadTime += resp.getDouble("fetch_time");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView lv = (ListView) findViewById(R.id.avg_list);
                        lv.setAdapter(new AverageAdapter(sharedCtxt, averages));
                        fetchAverageGraph();
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

    private void fetchAverageGraph() {
        HttpHandler.getJson(Vars.APIBASE + "/avggraph", heads, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                Utils.init(getApplicationContext());
                JSONArray graphDataCollection = resp.getJSONArray("data");
                for (int i=0; i<graphDataCollection.length(); i++) {
                    JSONObject graphData = graphDataCollection.getJSONObject(i);
                    List<Entry> graphDataEntries = new ArrayList<>();
                    for (int j=0; j<graphData.getJSONArray("points").length(); j++) {
                        JSONObject current = graphData.getJSONArray("points").getJSONObject(j);
                        graphDataEntries.add(new Entry((float)current.getDouble("x"), (float)current.getDouble("y")));
                        if (current.getBoolean("ishalftermgrade")) {
                            Vars.halfTermTimes.put(graphData.getString("subject"), (int)current.getDouble("x"));
                        }
                    }
                    Vars.averageGraphData.put(graphData.getString("subject"), new LineDataSet(graphDataEntries, graphData.getString("subject")));
                }
                loadTime += resp.getDouble("fetch_time");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(findViewById(R.id.main_coord_view), getResources().getString(R.string.main_load_complete, (float)loadTime), Snackbar.LENGTH_LONG).show();
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
    protected void onSaveInstanceState(Bundle b) {
        b.putParcelableArrayList("allGrades", allGrades);
        b.putParcelableArrayList("averages", averages);
        b.putInt("viewFlipperState", vf.getDisplayedChild());
        b.putInt("gradeViewFlipperState", gVf.getDisplayedChild());
        if (shouldShowMenu) b.putString("sortingTitle", menu.getItem(0).getTitle().toString());
        super.onSaveInstanceState(b);
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
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mChangeSort = menu.getItem(0);
        mChangeSort.setTitle(lastMenuState == null ? getResources().getString(R.string.action_sortbydate) : lastMenuState);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return shouldShowMenu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Currently this check is not necessary, but let's do it anyway
        if (item.getItemId() == R.id.action_changesort) {
            if (item.getTitle() == getResources().getString(R.string.action_sortbydate)) {
                gVf.setDisplayedChild(1);
                item.setTitle(R.string.action_sortbysubject);
            } else {
                gVf.setDisplayedChild(0);
                item.setTitle(R.string.action_sortbydate);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_avgs) {
            toolbar.setTitle(R.string.title_activity_avgs);
            shouldShowMenu = false;
            invalidateOptionsMenu();
            vf.setDisplayedChild(1);
        } else if (id == R.id.nav_grades) {
            toolbar.setTitle(R.string.title_activity_grades);
            getMenuInflater().inflate(R.menu.main, menu);
            shouldShowMenu = true;
            invalidateOptionsMenu();
            vf.setDisplayedChild(0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
