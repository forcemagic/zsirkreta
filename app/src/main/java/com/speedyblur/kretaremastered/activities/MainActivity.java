package com.speedyblur.kretaremastered.activities;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ViewFlipper;

import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.fragments.MainAnnouncementsFragment;
import com.speedyblur.kretaremastered.fragments.MainAveragesFragment;
import com.speedyblur.kretaremastered.fragments.MainGradesFragment;
import com.speedyblur.kretaremastered.fragments.MainScheduleFragment;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.IRefreshHandler;

public class MainActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener {
    private IRefreshHandler irh;
    private boolean shouldShowMenu = true;
    private String lastMenuState;
    public Profile p;

    // UI ref
    private Toolbar toolbar;
    private Menu menu;
    private FragmentManager fragManager;
    private SwipeRefreshLayout swipeRefresh;
    private Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_inner);

        p = getIntent().getParcelableExtra("profile");

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_grades);
        setSupportActionBar(toolbar);

        // Drawer setup
        AccountHeaderBuilder accHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.side_nav_bar)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(p.hasFriendlyName() ? p.getFriendlyName() : p.getCardid())
                                .withEmail(p.hasFriendlyName() ? p.getCardid() : null)
                                .withIcon(R.mipmap.ic_launcher_round)
                );
        drawer = new DrawerBuilder().withActivity(this).withToolbar(toolbar)
                .withAccountHeader(accHeader.build())
                .inflateMenu(R.menu.activity_main_drawer)
                .withOnDrawerItemClickListener(this)
                .build();

        // Fragment setup
        fragManager = getSupportFragmentManager();
        fragManager.beginTransaction().add(R.id.master_fragment, new MainGradesFragment()).commit();

        // SwipeRefreshLayout setup
        swipeRefresh = findViewById(R.id.master_swiperefresh);
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doProfileUpdate();
            }
        });

        if (savedInstanceState != null) {
            shouldShowMenu = savedInstanceState.getBoolean("shouldShowMenu");
            if (shouldShowMenu) lastMenuState = savedInstanceState.getString("sortingTitle");
            toolbar.setTitle(savedInstanceState.getString("toolbarTitle"));
        } else doProfileUpdate();
    }

    public void doProfileUpdate() {
        swipeRefresh.setRefreshing(true);
        Common.fetchAccountAsync(this, p, new Common.IFetchAccount() {
            @Override
            public void onFetchComplete() {
                irh.onRefreshComplete();
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFetchError(int localizedErrorMsg) {
                Snackbar.make(findViewById(R.id.main_coord_view), localizedErrorMsg, Snackbar.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    public void setRefreshHandler(IRefreshHandler irh) {
        this.irh = irh;
    }

    public void setSwipeRefreshEnabled(boolean b) {
        swipeRefresh.setEnabled(b);
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

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) drawer.closeDrawer();
        else super.onBackPressed();
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
            ViewFlipper gradeVf = findViewById(R.id.gradeOrderFlipper);
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
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        if (position == 1) {
            toolbar.setTitle(R.string.title_activity_grades);
            getMenuInflater().inflate(R.menu.main, menu);
            shouldShowMenu = true;
            invalidateOptionsMenu();

            fragManager.beginTransaction().replace(R.id.master_fragment, new MainGradesFragment()).commit();
        } else if (position == 2) {
            toolbar.setTitle(R.string.title_activity_avgs);
            shouldShowMenu = false;
            invalidateOptionsMenu();

            fragManager.beginTransaction().replace(R.id.master_fragment, new MainAveragesFragment()).commit();
        } else if (position == 3) {
            toolbar.setTitle(R.string.title_activity_schedule);
            shouldShowMenu = false;
            invalidateOptionsMenu();

            fragManager.beginTransaction().replace(R.id.master_fragment, new MainScheduleFragment()).commit();
        } else if (position == 4) {
            toolbar.setTitle(R.string.title_activity_announcements);
            shouldShowMenu = false;
            invalidateOptionsMenu();

            fragManager.beginTransaction().replace(R.id.master_fragment, new MainAnnouncementsFragment()).commit();
        }

        return false;
    }
}
