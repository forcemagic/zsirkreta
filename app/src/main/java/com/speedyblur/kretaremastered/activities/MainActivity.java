package com.speedyblur.kretaremastered.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Profile;

// TODO FOREST!!!
/*
 * #1 Félévi és évvégi jegyek iconjának megnövelése
 * #2 Portrait mode lock telefonon, de nem tableten
 * #3 Grafikonon a félévi és évvégi átlag kiemelése eltérõ színnel
 * #4 Naptár kicserélése az absences pageen nagyobbra
 */
// TODO FOREST END!
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private boolean shouldShowMenu = true;
    private String lastMenuState;
    public Profile p;

    // UI ref
    private Toolbar toolbar;
    private Menu menu;
    private ViewFlipper vf;

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
        profNameHead.setText(getIntent().getStringExtra("profileName"));

        vf = (ViewFlipper) findViewById(R.id.main_viewflipper);

        if (savedInstanceState != null) {
            vf.setDisplayedChild(savedInstanceState.getInt("viewFlipperState"));
            shouldShowMenu = savedInstanceState.getBoolean("shouldShowMenu");
            if (shouldShowMenu) lastMenuState = savedInstanceState.getString("sortingTitle");
            toolbar.setTitle(savedInstanceState.getString("toolbarTitle"));
        } else {
            vf.setDisplayedChild(0);
        }
    }

    /* UNUSED METHODS START HERE

    private void showAbsenceListForDate(CalendarDay day) {
        lastAbsenceDate = day;

        Calendar c = day.getCalendar();
        ArrayList<Absence> listElements = new ArrayList<>();
        for (int i=0; i<absences.size(); i++) {
            Calendar toCompare = Calendar.getInstance();
            toCompare.setTimeInMillis((long)absences.get(i).date*1000);
            if (toCompare.get(Calendar.DAY_OF_YEAR) == c.get(Calendar.DAY_OF_YEAR)) listElements.add(absences.get(i));
        }
        Collections.sort(listElements, new Comparator<Absence>() {
            @Override
            public int compare(Absence t1, Absence t2) {
                return t1.classNum - t2.classNum;
            }
        });

        // "Select" dayofweek
        // TODO: Optimize code! There are things like these in bulletSelectWeekday(...)
        findViewById(R.id.absenceMondaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        findViewById(R.id.absenceTuesdaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        findViewById(R.id.absenceWednesdaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        findViewById(R.id.absenceThursdaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        findViewById(R.id.absenceFridaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        findViewById(R.id.absenceSaturdaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        Drawable selectedBullet = ContextCompat.getDrawable(this, R.drawable.weekday_selector).mutate();
        selectedBullet.setColorFilter(ContextCompat.getColor(this, R.color.weekdayActive), PorterDuff.Mode.SRC_ATOP);
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            findViewById(R.id.absenceMondaySelector).setBackground(selectedBullet);
        } else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) {
            findViewById(R.id.absenceTuesdaySelector).setBackground(selectedBullet);
        } else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
            findViewById(R.id.absenceWednesdaySelector).setBackground(selectedBullet);
        } else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
            findViewById(R.id.absenceThursdaySelector).setBackground(selectedBullet);
        } else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            findViewById(R.id.absenceFridaySelector).setBackground(selectedBullet);
        } else if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            findViewById(R.id.absenceSaturdaySelector).setBackground(selectedBullet);
        }

        ListView lv = (ListView)findViewById(R.id.absenceList);
        lv.setAdapter(new AbsenceAdapter(this, listElements));

        TextView currentDate = (TextView) findViewById(R.id.currentAbsenceListDate);
        currentDate.setText(new SimpleDateFormat("YYYY. MMMM dd.", Locale.getDefault()).format(c.getTime()));
    }

    public void openAbsenceCalendar(View v) {
        AlertDialog.Builder calDialog = new AlertDialog.Builder(sharedCtxt);

        // Calendar setup
        View inflView = LayoutInflater.from(this).inflate(R.layout.dialog_calendar, null);
        final MaterialCalendarView cView = inflView.findViewById(R.id.absenceCalendar);
        final ArrayList<CalendarDay> provenDates = new ArrayList<>();
        final ArrayList<CalendarDay> unprovenDates = new ArrayList<>();
        for (int i=0; i<absences.size(); i++) {
            if (absences.get(i).proven) provenDates.add(CalendarDay.from(new Date((long) absences.get(i).date*1000)));
            else unprovenDates.add(CalendarDay.from(new Date((long) absences.get(i).date*1000)));
        }
        cView.addDecorators(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return provenDates.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(sharedCtxt, R.drawable.calendar_goodbullet));
            }
        }, new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return unprovenDates.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(sharedCtxt, R.drawable.calendar_badbullet));
            }
        });

        calDialog.setView(inflView);
        calDialog.setTitle(R.string.select_date);
        calDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (cView.getSelectedDate() != null) showAbsenceListForDate(cView.getSelectedDate());
                dialogInterface.dismiss();
            }
        });
        calDialog.show();
    }

    public void bulletSelectWeekday(View v) {
        // Setting "selected" background
        findViewById(R.id.absenceMondaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        findViewById(R.id.absenceTuesdaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        findViewById(R.id.absenceWednesdaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        findViewById(R.id.absenceThursdaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        findViewById(R.id.absenceFridaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        findViewById(R.id.absenceSaturdaySelector).setBackground(ContextCompat.getDrawable(this, R.drawable.weekday_selector));
        Drawable selectedBullet = ContextCompat.getDrawable(this, R.drawable.weekday_selector).mutate();
        selectedBullet.setColorFilter(ContextCompat.getColor(this, R.color.weekdayActive), PorterDuff.Mode.SRC_ATOP);
        v.setBackground(selectedBullet);

        Calendar c = lastAbsenceDate.getCalendar();
        if (v.getId() == R.id.absenceMondaySelector) {
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        } else if (v.getId() == R.id.absenceTuesdaySelector) {
            c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        } else if (v.getId() == R.id.absenceWednesdaySelector) {
            c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        } else if (v.getId() == R.id.absenceThursdaySelector) {
            c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        } else if (v.getId() == R.id.absenceFridaySelector) {
            c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        } else if (v.getId() == R.id.absenceSaturdaySelector) {
            c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        }
        showAbsenceListForDate(CalendarDay.from(c));
    }
    // UNUSED METHODS END HERE */

    /**
     * Saves instance to a bundle
     * @param b The bundle to put things in
     */
    @Override
    protected void onSaveInstanceState(Bundle b) {
        b.putInt("viewFlipperState", vf.getDisplayedChild());
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
            // TODO: Implement changesort
            if (item.getTitle() == getResources().getString(R.string.action_sortbydate)) {
                item.setTitle(R.string.action_sortbysubject);
            } else {
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
        } else if (id == R.id.nav_absences) {
            toolbar.setTitle(R.string.title_activity_absences);
            shouldShowMenu = false;
            invalidateOptionsMenu();
            vf.setDisplayedChild(2);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showOnSnackbar(@StringRes final int message, final int length) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(R.id.main_coord_view), message, length).show();
            }
        });
    }
}
