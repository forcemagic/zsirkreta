package com.speedyblur.kretaremastered.activities;

import android.content.Intent;
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

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.fragments.MainBulletinsFragment;
import com.speedyblur.kretaremastered.fragments.MainAveragesFragment;
import com.speedyblur.kretaremastered.fragments.MainGradesFragment;
import com.speedyblur.kretaremastered.fragments.MainScheduleFragment;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.shared.AccountStore;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DecryptionException;
import com.speedyblur.kretaremastered.shared.IRefreshHandler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener {
    private final static int INTENT_REQ_NEWPROF = 1;
    private final static int INTENT_REQ_DELPROF = 2;

    private IRefreshHandler irh;
    private boolean shouldShowMenu = true;
    private ArrayList<Profile> profiles;
    public Profile p;

    // UI ref
    private Toolbar toolbar;
    private Menu menu;
    private FragmentManager fragManager;
    private SwipeRefreshLayout swipeRefresh;
    private AccountHeader accHeader;
    private Drawer drawer;

    // TODO: Implement savedInstanceState handling
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar setup
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_grades);
        setSupportActionBar(toolbar);

        fetchAccounts();

        String lastUsedProfile = getSharedPreferences("main", MODE_PRIVATE).getString("lastUsedProfile", "");
        if (!lastUsedProfile.equals("")) {
            for (int i = 0; i < profiles.size(); i++) {
                if (profiles.get(i).getCardid().equals(lastUsedProfile)) {
                    p = profiles.get(i);
                    break;
                }
            }
        }
        if (p == null && profiles.size() > 0)
            p = profiles.get(0);

        assert p != null;

        // Drawer & Account Header setup
        accHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_background)
                .withOnlyMainProfileImageVisible(true)
                .build();
        populateProfiles();

        drawer = new DrawerBuilder().withActivity(this).withToolbar(toolbar)
                .withAccountHeader(accHeader)
                .inflateMenu(R.menu.activity_main_drawer)
                .withOnDrawerItemClickListener(this)
                .build();

        // SwipeRefreshLayout setup
        swipeRefresh = findViewById(R.id.master_swiperefresh);
        swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doProfileUpdate();
            }
        });

        // Fragment setup
        fragManager = getSupportFragmentManager();
        fragManager.beginTransaction().add(R.id.master_fragment, new MainGradesFragment()).commit();

        accHeader.setActiveProfile(Long.parseLong(p.getCardid()));

        doProfileUpdate();
    }

    private void fetchAccounts() {
        // Fetch Accounts (UI block, sorry about that :/)
        try {
            AccountStore as = new AccountStore(this, Common.SQLCRYPT_PWD);
            profiles = as.getAccounts();
            as.close();
        } catch (DecryptionException e) {e.printStackTrace();}
    }

    private void doProfileUpdate() {
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

    // This code escalated quickly. TODO: Refactor.
    private void populateProfiles() {
        List<IProfile> finalProfiles = new ArrayList<>();
        for (int i=0; i<profiles.size(); i++) {
            final Profile cProfile = profiles.get(i);

            finalProfiles.add(new ProfileDrawerItem()
                    .withName(cProfile.hasFriendlyName() ? cProfile.getFriendlyName() : cProfile.getCardid())
                    .withEmail(cProfile.hasFriendlyName() ? cProfile.getCardid() : null)
                    .withIdentifier(Long.parseLong(cProfile.getCardid()))
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            p = cProfile;
                            irh.onRefreshComplete();
                            doProfileUpdate();
                            return true;
                        }
                    })
            );
        }

        accHeader.clear();
        accHeader.setProfiles(finalProfiles);
        accHeader.addProfiles(new ProfileSettingDrawerItem()
                .withName(R.string.profile_add)
                .withIcon(R.drawable.add_profile_icon_black)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Intent it = new Intent(MainActivity.this, NewProfileActivity.class);
                        startActivityForResult(it, INTENT_REQ_NEWPROF);
                        return true;
                    }
                }), new ProfileSettingDrawerItem()
                .withName(R.string.profile_manage)
                .withIcon(R.drawable.settings_icon_black)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Intent it = new Intent(MainActivity.this, ManageProfilesActivity.class);
                        it.putExtra("currentProfileId", p.getCardid());
                        startActivityForResult(it, INTENT_REQ_DELPROF);
                        return true;
                    }
                })
        );
    }

    public void setRefreshHandler(IRefreshHandler irh) {
        this.irh = irh;
    }

    public void setSwipeRefreshEnabled(boolean b) {
        swipeRefresh.setEnabled(b);
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

    // TODO: Possibly... refactor?
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

            fragManager.beginTransaction().replace(R.id.master_fragment, new MainBulletinsFragment()).commit();
        } else return true;
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQ_NEWPROF && data != null) {
            fetchAccounts();
            populateProfiles();
            accHeader.setActiveProfile(Long.parseLong(data.getStringExtra("profileId")));
            for (int i=0; i<profiles.size(); i++) {
                if (profiles.get(i).getCardid().equals(data.getStringExtra("profileId")))
                    p = profiles.get(i);
            }
            fragManager.beginTransaction().replace(R.id.master_fragment, new MainGradesFragment()).commit();
            doProfileUpdate();
        } else if (requestCode == INTENT_REQ_DELPROF) {
            fetchAccounts();
            populateProfiles();
            fragManager.beginTransaction().replace(R.id.master_fragment, new MainGradesFragment()).commit();
            doProfileUpdate();
        }
    }
}
