package com.speedyblur.kretaremastered.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.fragments.MainAnnouncementsFragment;
import com.speedyblur.kretaremastered.fragments.MainAveragesFragment;
import com.speedyblur.kretaremastered.fragments.MainGradesFragment;
import com.speedyblur.kretaremastered.fragments.MainScheduleFragment;
import com.speedyblur.kretaremastered.models.Profile;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private boolean shouldShowMenu = true;
    private String lastMenuState;
    public Profile p;

    // UI ref
    private Toolbar toolbar;
    private Menu menu;
    private FragmentManager fragManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        p = getIntent().getParcelableExtra("profile");

        // Toolbar setup
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_grades);
        setSupportActionBar(toolbar);

        // Drawer setup
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // NavView setup
        NavigationView navigationView = (NavigationView) findViewById(R.id.mainNav);
        navigationView.setNavigationItemSelectedListener(this);

        // Set profile name to drawer header
        TextView profNameHead = navigationView.getHeaderView(0).findViewById(R.id.profileNameHead);
        profNameHead.setText(p.getFriendlyName().equals("") ? p.getCardid() : p.getFriendlyName());

        // Fragment setup
        fragManager = getSupportFragmentManager();
        fragManager.beginTransaction().add(R.id.master_fragment, new MainGradesFragment()).commit();

        // Popup stats
        int timeTaken = getIntent().getIntExtra("loadtime", -1);
        if (timeTaken != -1)
            Snackbar.make(findViewById(R.id.main_coord_view), getString(R.string.main_load_complete, (float) timeTaken/1000), Snackbar.LENGTH_SHORT).show();

        if (savedInstanceState != null) {
            // TODO: 10/4/17 Implement fragment state save
            shouldShowMenu = savedInstanceState.getBoolean("shouldShowMenu");
            if (shouldShowMenu) lastMenuState = savedInstanceState.getString("sortingTitle");
            toolbar.setTitle(savedInstanceState.getString("toolbarTitle"));
        }
    }

    /**
     * Saves instance to a bundle
     * @param b The bundle to put things in
     */
    @Override
    protected void onSaveInstanceState(Bundle b) {
        b.putBoolean("shouldShowMenu", shouldShowMenu);
        if (shouldShowMenu) b.putString("sortingTitle", menu.getItem(0).getTitle().toString());
        b.putString("toolbarTitle", toolbar.getTitle().toString());
        super.onSaveInstanceState(b);
    }

    /**
     * Closes drawer if it's open.
     */
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
        if (lastMenuState != null) mChangeSort.setTitle(lastMenuState);
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
            ViewFlipper gradeVf = (ViewFlipper) findViewById(R.id.gradeOrderFlipper);
            if (item.getTitle() == getResources().getString(R.string.action_sortbysubject)) {
                item.setTitle(R.string.action_sortbydate);
                gradeVf.setDisplayedChild(1);
            } else {
                item.setTitle(R.string.action_sortbysubject);
                gradeVf.setDisplayedChild(0);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_grades) {
            toolbar.setTitle(R.string.title_activity_grades);
            getMenuInflater().inflate(R.menu.main, menu);
            shouldShowMenu = true;
            invalidateOptionsMenu();

            fragManager.beginTransaction().replace(R.id.master_fragment, new MainGradesFragment()).commitNow();
            ((ViewFlipper) findViewById(R.id.gradeOrderFlipper)).setDisplayedChild(0);
        } else if (id == R.id.nav_avgs) {
            toolbar.setTitle(R.string.title_activity_avgs);
            shouldShowMenu = false;
            invalidateOptionsMenu();

            fragManager.beginTransaction().replace(R.id.master_fragment, new MainAveragesFragment()).commitNow();
        } else if (id == R.id.nav_schedule) {
            toolbar.setTitle(R.string.title_activity_schedule);
            shouldShowMenu = false;
            invalidateOptionsMenu();

            fragManager.beginTransaction().replace(R.id.master_fragment, new MainScheduleFragment()).commitNow();
        } else if (id == R.id.nav_announcements) {
            toolbar.setTitle(R.string.title_activity_announcements);
            shouldShowMenu = false;
            invalidateOptionsMenu();

            fragManager.beginTransaction().replace(R.id.master_fragment, new MainAnnouncementsFragment()).commitNow();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
