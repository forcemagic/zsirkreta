package com.speedyblur.kretaremastered.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.MainActivity;
import com.speedyblur.kretaremastered.adapters.ClazzAdapter;
import com.speedyblur.kretaremastered.models.Clazz;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;
import com.speedyblur.kretaremastered.shared.IDataStore;
import com.speedyblur.kretaremastered.shared.IRefreshHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class MainScheduleFragment extends Fragment {
    // TODO: Implement this
    //private ArrayList<AllDayEvent> allDayEvents;
    private ArrayList<Clazz> clazzes;
    private Calendar selectedScheduleDate;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_main_schedule, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final MainActivity parent = (MainActivity) getActivity();
        final ListView schedList = parent.findViewById(R.id.scheduleList);

        updateFromDS(parent);
        parent.setRefreshHandler(new IRefreshHandler() {
            @Override
            public void onRefreshComplete() {
                updateFromDS(parent);
            }
        });

        // ListView setup
        schedList.setEmptyView(parent.findViewById(R.id.noSchoolView));
        schedList.setOnTouchListener(new SwipeDetector());
        schedList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                parent.setSwipeRefreshEnabled(!absListView.canScrollVertically(-1));
            }
        });
        schedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Clazz c = (Clazz) adapterView.getItemAtPosition(pos);
                View dialView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_class_details, null);

                ImageView mClassIcon = dialView.findViewById(R.id.classInfoIcon);
                TextView mSubject = dialView.findViewById(R.id.classInfoSubject);
                TextView mTheme = dialView.findViewById(R.id.classInfoTheme);
                TextView mTeacher = dialView.findViewById(R.id.classInfoTeacher);
                TextView mTime = dialView.findViewById(R.id.classInfoTime);
                TextView mRoom = dialView.findViewById(R.id.classInfoRoom);

                mClassIcon.setImageDrawable(c.getIcon(getContext()));

                mSubject.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Light.ttf"));
                String classNum = getString(R.string.class_number, c.getClassnum());
                SpannableStringBuilder ssb = new SpannableStringBuilder(classNum+" "+Common.getLocalizedSubjectName(getContext(), c.getSubject()));
                ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, classNum.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                mSubject.setText(ssb);

                if (c.getTheme().equals("")) mTheme.setText(R.string.japansmile);
                else mTheme.setText(c.getTheme());
                mTeacher.setText(c.getTeacher());
                SimpleDateFormat fmt = new SimpleDateFormat("h:mm a", Locale.getDefault());
                mTime.setText(fmt.format(new Date((long) c.getBeginTime()*1000))+" - "+fmt.format(new Date((long) c.getEndTime()*1000)));
                mRoom.setText(c.getRoom().replace("(", "").replace(")", ""));

                new AlertDialog.Builder(getContext()).setView(dialView)
                        .setPositiveButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

        // Setting up listeners
        parent.findViewById(R.id.scheduleMondaySelector).setOnClickListener(new BulletClick());
        parent.findViewById(R.id.scheduleTuesdaySelector).setOnClickListener(new BulletClick());
        parent.findViewById(R.id.scheduleWednesdaySelector).setOnClickListener(new BulletClick());
        parent.findViewById(R.id.scheduleThursdaySelector).setOnClickListener(new BulletClick());
        parent.findViewById(R.id.scheduleFridaySelector).setOnClickListener(new BulletClick());
        parent.findViewById(R.id.currentScheduleDate).setOnClickListener(new CalendarClick());
        parent.findViewById(R.id.noSchoolView).setOnTouchListener(new SwipeDetector());
    }

    private void updateFromDS(MainActivity parent) {
        DataStore.asyncQuery(parent, parent.p.getCardid(), Common.SQLCRYPT_PWD, new IDataStore<ArrayList<Clazz>>() {

            @Override
            public ArrayList<Clazz> requestFromStore(DataStore ds) {
                ArrayList<Clazz> clazzes = ds.getClassesData();
                Collections.sort(clazzes, new Comparator<Clazz>() {
                    @Override
                    public int compare(Clazz c1, Clazz c2) {
                        if (c1.getBeginTime() == c2.getBeginTime() && c1.getEndTime() == c2.getEndTime()) return 0;
                        return new Date((long) c1.getBeginTime()*1000).compareTo(new Date((long) c2.getEndTime()*1000));
                    }
                });
                return clazzes;
            }

            @Override
            public void processRequest(ArrayList<Clazz> data) {
                clazzes = data;
                if (selectedScheduleDate == null) selectedScheduleDate = Calendar.getInstance();
                showAbsenceListForDate(selectedScheduleDate);
            }

            @Override
            public void onDecryptionFailure(DecryptionException e) {
                e.printStackTrace();
            }
        });
    }

    private void resetSelectBullet(int day) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        getActivity().findViewById(R.id.scheduleMondaySelector).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.weekday_selector));
        getActivity().findViewById(R.id.scheduleTuesdaySelector).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.weekday_selector));
        getActivity().findViewById(R.id.scheduleWednesdaySelector).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.weekday_selector));
        getActivity().findViewById(R.id.scheduleThursdaySelector).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.weekday_selector));
        getActivity().findViewById(R.id.scheduleFridaySelector).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.weekday_selector));

        Drawable selectedBullet = ContextCompat.getDrawable(getContext(), R.drawable.weekday_selector).mutate();
        selectedBullet.setColorFilter(ContextCompat.getColor(getContext(), R.color.weekdayActive), PorterDuff.Mode.SRC_ATOP);
        if (day == Calendar.MONDAY) {
            getActivity().findViewById(R.id.scheduleMondaySelector).setBackground(selectedBullet);
        } else if (day == Calendar.TUESDAY) {
            getActivity().findViewById(R.id.scheduleTuesdaySelector).setBackground(selectedBullet);
        } else if (day == Calendar.WEDNESDAY) {
            getActivity().findViewById(R.id.scheduleWednesdaySelector).setBackground(selectedBullet);
        } else if (day == Calendar.THURSDAY) {
            getActivity().findViewById(R.id.scheduleThursdaySelector).setBackground(selectedBullet);
        } else if (day == Calendar.FRIDAY) {
            getActivity().findViewById(R.id.scheduleFridaySelector).setBackground(selectedBullet);
        }
    }

    private void showAbsenceListForDate(Calendar day) {
        selectedScheduleDate = day;

        ArrayList<Clazz> listElements = new ArrayList<>();
        for (int i=0; i<clazzes.size(); i++) {
            Calendar toCompare = Calendar.getInstance();
            toCompare.setTimeInMillis((long)clazzes.get(i).getBeginTime()*1000);
            if (toCompare.get(Calendar.YEAR) == day.get(Calendar.YEAR) && toCompare.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR))
                listElements.add(clazzes.get(i));
        }

        resetSelectBullet(day.get(Calendar.DAY_OF_WEEK));

        ListView lv = getActivity().findViewById(R.id.scheduleList);
        lv.setAdapter(new ClazzAdapter(getContext(), listElements));

        TextView currentDate = getActivity().findViewById(R.id.currentScheduleDate);
        Typeface tFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Light.ttf");
        currentDate.setTypeface(Typeface.create(tFace, Typeface.BOLD));
        currentDate.setText(new SimpleDateFormat("MMMM dd.", Locale.getDefault()).format(day.getTime()));

        // TODO: 10/5/17 Implement this
        Calendar postCal = (Calendar) day.clone();
        SimpleDateFormat weekFmt = new SimpleDateFormat("MMM. dd.", Locale.getDefault());
        postCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); String dateMonday = weekFmt.format(postCal.getTime());
        postCal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY); String dateFriday = weekFmt.format(postCal.getTime());
    }

    private class SwipeDetector implements View.OnTouchListener {
        private final int minDist = 100;
        private float downX, downY, upX, upY;

        @Override
        public boolean onTouch(final View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = motionEvent.getX();
                    downY = motionEvent.getY();
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    upX = motionEvent.getX();
                    upY = motionEvent.getY();

                    float deltaX = downX - upX;
                    float deltaY = downY - upY;

                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        if (Math.abs(deltaX) > minDist) {
                            final Calendar c = selectedScheduleDate;
                            if (deltaX > 0) {
                                c.add(Calendar.DATE, 1);
                                view.animate().translationX(-100f).alpha(0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        animation.removeListener(this);
                                        showAbsenceListForDate(c);
                                        view.animate().translationX(100f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                animation.removeListener(this);
                                                view.animate().translationX(0f).alpha(1f).setDuration(100).setListener(null).start();
                                            }
                                        }).start();
                                    }
                                });
                                return true;
                            } else if (deltaX < 0) {
                                c.add(Calendar.DATE, -1);
                                view.animate().translationX(100f).alpha(0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        animation.removeListener(this);
                                        showAbsenceListForDate(c);
                                        view.animate().translationX(-100f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                animation.removeListener(this);
                                                view.animate().translationX(0f).alpha(1f).setDuration(100).setListener(null).start();
                                            }
                                        }).start();
                                    }
                                });
                                return true;
                            } else return false;
                        } else return false;
                    }
                }
            }
            return false;
        }
    }

    private class BulletClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Calendar c = selectedScheduleDate;
            if (v.getId() == R.id.scheduleMondaySelector) {
                c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            } else if (v.getId() == R.id.scheduleTuesdaySelector) {
                c.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
            } else if (v.getId() == R.id.scheduleWednesdaySelector) {
                c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
            } else if (v.getId() == R.id.scheduleThursdaySelector) {
                c.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
            } else if (v.getId() == R.id.scheduleFridaySelector) {
                c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            }

            resetSelectBullet(c.get(Calendar.DAY_OF_WEEK));
            showAbsenceListForDate(c);
        }
    }

    private class CalendarClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final CompactCalendarView cal = getActivity().findViewById(R.id.scheduleCalendarView);
            cal.shouldDrawIndicatorsBelowSelectedDays(true);
            cal.setCurrentDate(selectedScheduleDate.getTime());
            if (cal.getHeight() > 0) {
                cal.hideCalendarWithAnimation();
            }

            ArrayList<Event> evts = new ArrayList<>();
            for (int i=0; i<clazzes.size(); i++) {
                Clazz c = clazzes.get(i);

                if (c.isAbsent()) {
                    if (c.getAbsenceDetails().isProven())
                        evts.add(new Event(ContextCompat.getColor(getContext(), R.color.goodGrade), (long) c.getBeginTime()*1000));
                    else
                        evts.add(new Event(ContextCompat.getColor(getContext(), R.color.badGrade), (long) c.getBeginTime()*1000));
                }
            }
            cal.removeAllEvents();
            cal.addEvents(evts);

            cal.showCalendarWithAnimation();
            cal.setListener(new CompactCalendarView.CompactCalendarViewListener() {
                @Override
                public void onDayClick(Date dateClicked) {
                    selectedScheduleDate.setTime(dateClicked);
                    showAbsenceListForDate(selectedScheduleDate);
                    if (cal.isAnimating()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                cal.hideCalendarWithAnimation();
                            }
                        }, 800);
                    } else cal.hideCalendarWithAnimation();
                }

                @Override
                public void onMonthScroll(Date firstDayOfNewMonth) {
                    // Method stub
                }
            });
        }
    }
}
