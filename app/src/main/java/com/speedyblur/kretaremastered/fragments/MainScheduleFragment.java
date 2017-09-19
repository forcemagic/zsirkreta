package com.speedyblur.kretaremastered.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.MainActivity;
import com.speedyblur.kretaremastered.adapters.ClazzAdapter;
import com.speedyblur.kretaremastered.models.AllDayEvent;
import com.speedyblur.kretaremastered.models.Clazz;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainScheduleFragment extends Fragment {
    // TODO: Implement this
    private ArrayList<AllDayEvent> allDayEvents;
    private ArrayList<Clazz> clazzes;
    private CalendarDay selectedScheduleDate;
    private Timer timer;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_main_schedule, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity parent = (MainActivity) getActivity();

        // Fetching DataStore
        try {
            DataStore ds = new DataStore(getContext(), parent.p.getCardid(), Common.SQLCRYPT_PWD);
            allDayEvents = ds.getAllDayEventsData();
            clazzes = ds.getClassesData();
            ds.close();
        } catch (DecryptionException e) {e.printStackTrace();}
        Collections.sort(clazzes, new Comparator<Clazz>() {
            @Override
            public int compare(Clazz c1, Clazz c2) {
                return c1.getBeginTime() - c2.getEndTime();
            }
        });

        // Setting up listeners
        parent.findViewById(R.id.scheduleMondaySelector).setOnClickListener(new BulletClick());
        parent.findViewById(R.id.scheduleTuesdaySelector).setOnClickListener(new BulletClick());
        parent.findViewById(R.id.scheduleWednesdaySelector).setOnClickListener(new BulletClick());
        parent.findViewById(R.id.scheduleThursdaySelector).setOnClickListener(new BulletClick());
        parent.findViewById(R.id.scheduleFridaySelector).setOnClickListener(new BulletClick());
        parent.findViewById(R.id.calendarImageButton).setOnClickListener(new CalendarClick());

        ListView schedList = (ListView) parent.findViewById(R.id.scheduleList);
        schedList.setEmptyView(parent.findViewById(R.id.noSchoolView));
        schedList.setOnTouchListener(new SwipeDetector());
        schedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Clazz c = (Clazz) adapterView.getItemAtPosition(pos);
                View dialView = LayoutInflater.from(getContext()).inflate(R.layout.class_information_dialog, null);

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

                mTheme.setText(c.getTheme());
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
        parent.findViewById(R.id.noSchoolView).setOnTouchListener(new SwipeDetector());

        selectedScheduleDate = CalendarDay.from(Calendar.getInstance());
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();

        timer = new Timer();
        TimerTask doRefreshSchedule = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAbsenceListForDate(selectedScheduleDate);
                    }
                });
            }
        };
        timer.schedule(doRefreshSchedule, 0, 30000);
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

    private void showAbsenceListForDate(CalendarDay day) {
        selectedScheduleDate = day;

        Calendar c = day.getCalendar();
        ArrayList<Clazz> listElements = new ArrayList<>();
        for (int i=0; i<clazzes.size(); i++) {
            Calendar toCompare = Calendar.getInstance();
            toCompare.setTimeInMillis((long)clazzes.get(i).getBeginTime()*1000);
            if (toCompare.get(Calendar.YEAR) == c.get(Calendar.YEAR) && toCompare.get(Calendar.DAY_OF_YEAR) == c.get(Calendar.DAY_OF_YEAR))
                listElements.add(clazzes.get(i));
        }

        resetSelectBullet(c.get(Calendar.DAY_OF_WEEK));

        ListView lv = getActivity().findViewById(R.id.scheduleList);
        lv.setAdapter(new ClazzAdapter(getContext(), listElements));

        TextView currentDate = getActivity().findViewById(R.id.currentScheduleDate);
        Typeface tFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Light.ttf");
        currentDate.setTypeface(Typeface.create(tFace, Typeface.BOLD));
        currentDate.setText(new SimpleDateFormat("MMMM dd.", Locale.getDefault()).format(c.getTime()));

        Calendar postCal = (Calendar) day.getCalendar().clone();
        TextView currentWeek = getActivity().findViewById(R.id.scheduleCurrentWeek);
        SimpleDateFormat weekFmt = new SimpleDateFormat("MMM. dd.", Locale.getDefault());
        postCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); String dateMonday = weekFmt.format(postCal.getTime());
        postCal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY); String dateFriday = weekFmt.format(postCal.getTime());
        currentWeek.setText(getString(R.string.current_week, dateMonday, dateFriday));
    }

    private class SwipeDetector implements View.OnTouchListener {
        private int minDist = 100;
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
                            final Calendar c = selectedScheduleDate.getCalendar();
                            if (deltaX > 0) {
                                c.add(Calendar.DATE, 1);
                                view.animate().translationX(-100f).alpha(0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        animation.removeListener(this);
                                        showAbsenceListForDate(CalendarDay.from(c));
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
                                        showAbsenceListForDate(CalendarDay.from(c));
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
            Calendar c = selectedScheduleDate.getCalendar();
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
            showAbsenceListForDate(CalendarDay.from(c));
        }
    }

    private class CalendarClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder calDialog = new AlertDialog.Builder(getContext());

            // Calendar setup
            View inflView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_calendar, null);
            final MaterialCalendarView cView = inflView.findViewById(R.id.absenceCalendar);
            final ArrayList<CalendarDay> provenDates = new ArrayList<>();
            final ArrayList<CalendarDay> unprovenDates = new ArrayList<>();
            for (int i=0; i<clazzes.size(); i++) {
                if (clazzes.get(i).isAbsent()) {
                    if (clazzes.get(i).isAbsent() && clazzes.get(i).getAbsenceDetails().isProven())
                        provenDates.add(CalendarDay.from(new Date((long) clazzes.get(i).getBeginTime() * 1000)));
                    else if (clazzes.get(i).isAbsent() && !clazzes.get(i).getAbsenceDetails().isProven())
                        unprovenDates.add(CalendarDay.from(new Date((long) clazzes.get(i).getBeginTime() * 1000)));
                }
            }

            // TODO: This is kind of ugly, but it's the only way I know to do this.
            cView.addDecorators(new DayViewDecorator() {
                @Override
                public boolean shouldDecorate(CalendarDay day) {
                    return provenDates.contains(day);
                }

                @Override
                public void decorate(DayViewFacade view) {
                    view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.calendar_goodbullet));
                }
            }, new DayViewDecorator() {
                @Override
                public boolean shouldDecorate(CalendarDay day) {
                    return unprovenDates.contains(day);
                }

                @Override
                public void decorate(DayViewFacade view) {
                    view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.calendar_badbullet));
                }
            }, new DayViewDecorator() {
                @Override
                public boolean shouldDecorate(CalendarDay day) {
                    return day.getCalendar().get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                            && day.getCalendar().get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
                            && !provenDates.contains(day) && !unprovenDates.contains(day);
                }

                @Override
                public void decorate(DayViewFacade view) {
                    view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.current_day_calendar_icon_black));
                }
            }, new DayViewDecorator() {
                @Override
                public boolean shouldDecorate(CalendarDay day) {
                    return day.getCalendar().get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                            && day.getCalendar().get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
                            && provenDates.contains(day);
                }

                @Override
                public void decorate(DayViewFacade view) {
                    view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cal_current_day_goodbullet));
                }
            }, new DayViewDecorator() {
                @Override
                public boolean shouldDecorate(CalendarDay day) {
                    return day.getCalendar().get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                            && day.getCalendar().get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
                            && unprovenDates.contains(day);
                }

                @Override
                public void decorate(DayViewFacade view) {
                    view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cal_current_day_badbullet));
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
    }
}
