package com.speedyblur.kretaremastered.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.graphics.Typeface;
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
import java.util.List;
import java.util.Locale;

public class MainScheduleFragment extends Fragment {
    // TODO: Implement this
    //private ArrayList<AllDayEvent> allDayEvents;
    private ArrayList<Clazz> clazzes;
    private Calendar selectedScheduleDate;
    private MainActivity parent;
    private CompactCalendarView calendarView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_main_schedule, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        parent = (MainActivity) getActivity();
        calendarView = parent.findViewById(R.id.scheduleCalendarView);
        final ListView schedList = parent.findViewById(R.id.scheduleList);

        updateFromDS(parent);
        parent.setRefreshHandler(new IRefreshHandler() {
            @Override
            public void onRefreshComplete() {
                updateFromDS(parent);
            }
        });

        // Support for vectors
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // ListView setup
        schedList.setEmptyView(parent.findViewById(R.id.noSchoolView));
        schedList.setOnTouchListener(new SwipeDetector());
        schedList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if (calendarView.getHeight() > 0)
                    parent.setSwipeRefreshEnabled(false);
                else
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
        parent.findViewById(R.id.scheduleTopBarLayout).setOnClickListener(new CalendarClick());
        parent.findViewById(R.id.noSchoolView).setOnTouchListener(new SwipeDetector());
        parent.findViewById(R.id.moveToCurrentDateIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAbsenceListForDate(Calendar.getInstance());
            }
        });
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

    private void showAbsenceListForDate(Calendar day) {
        selectedScheduleDate = day;
        calendarView.setCurrentDate(day.getTime());

        ArrayList<Clazz> listElements = new ArrayList<>();
        for (int i=0; i<clazzes.size(); i++) {
            Calendar toCompare = Calendar.getInstance();
            toCompare.setTimeInMillis((long)clazzes.get(i).getBeginTime()*1000);
            if (toCompare.get(Calendar.YEAR) == day.get(Calendar.YEAR) && toCompare.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR))
                listElements.add(clazzes.get(i));
        }

        ListView lv = getActivity().findViewById(R.id.scheduleList);
        lv.setAdapter(new ClazzAdapter(getContext(), listElements));

        TextView currentDate = getActivity().findViewById(R.id.currentScheduleDate);
        Typeface tFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Light.ttf");
        currentDate.setTypeface(Typeface.create(tFace, Typeface.BOLD));
        currentDate.setText(new SimpleDateFormat("yyyy. MMMM dd.", Locale.ENGLISH).format(day.getTime()));

        TextView currentDayOfWeek = getActivity().findViewById(R.id.scheduleCurrentDayOfWeek);
        currentDayOfWeek.setText(new SimpleDateFormat("E", Locale.ENGLISH).format(day.getTime()).substring(0,1));
    }

    private class SwipeDetector implements View.OnTouchListener {
        private final int minDist = 100;
        private float downX, downY, upX, upY;

        @Override
        public boolean onTouch(final View view, MotionEvent motionEvent) {
            if (clazzes.size() == 0)
                return false;

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

    private class CalendarClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            parent.setSwipeRefreshEnabled(false);

            calendarView.shouldDrawIndicatorsBelowSelectedDays(true);
            if (calendarView.getHeight() > 0) {
                calendarView.hideCalendarWithAnimation();
                parent.setSwipeRefreshEnabled(true);
            }

            calendarView.removeAllEvents();
            for (int i=0; i<clazzes.size(); i++) {
                Clazz c = clazzes.get(i);

                // TODO: Optimize & Refactor
                if (c.isAbsent()) {
                    List<Event> evts = calendarView.getEvents((long) c.getBeginTime()*1000);

                    if (c.getAbsenceDetails().isProven()) {
                        boolean alreadyHasProvenEvent = false;
                        for (int j=0; j<evts.size(); j++) {
                            if (ContextCompat.getColor(getContext(), R.color.goodGrade) == evts.get(j).getColor()) {
                                alreadyHasProvenEvent = true;
                                break;
                            }
                        }
                        if (!alreadyHasProvenEvent)
                                calendarView.addEvent(new Event(ContextCompat.getColor(getContext(), R.color.goodGrade),
                                        (long) c.getBeginTime() * 1000), false);
                    } else {
                        boolean alreadyHasNonProvenEvent = false;
                        for (int j=0; j<evts.size(); j++) {
                            if (ContextCompat.getColor(getContext(), R.color.badGrade) == evts.get(j).getColor()) {
                                alreadyHasNonProvenEvent = true;
                                break;
                            }
                        }
                        if (!alreadyHasNonProvenEvent)
                            calendarView.addEvent(new Event(ContextCompat.getColor(getContext(), R.color.badGrade),
                                    (long) c.getBeginTime() * 1000), false);
                    }
                }
            }

            calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
                @Override
                public void onDayClick(Date dateClicked) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(dateClicked);
                    showAbsenceListForDate(c);
                    parent.setSwipeRefreshEnabled(true);
                    if (calendarView.isAnimating()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                calendarView.hideCalendarWithAnimation();
                            }
                        }, 600);
                    } else calendarView.hideCalendarWithAnimation();
                }

                @Override
                public void onMonthScroll(final Date firstDayOfNewMonth) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Calendar c = Calendar.getInstance();
                            c.setTime(firstDayOfNewMonth);
                            showAbsenceListForDate(c);
                        }
                    }, 200);
                }
            });
            calendarView.showCalendarWithAnimation();
            calendarView.invalidate();
        }
    }
}
