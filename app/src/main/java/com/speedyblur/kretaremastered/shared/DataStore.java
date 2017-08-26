package com.speedyblur.kretaremastered.shared;

import android.content.Context;
import android.util.Log;

import com.speedyblur.kretaremastered.models.AbsenceDetails;
import com.speedyblur.kretaremastered.models.AllDayEvent;
import com.speedyblur.kretaremastered.models.Average;
import com.speedyblur.kretaremastered.models.Clazz;
import com.speedyblur.kretaremastered.models.Grade;

import net.sqlcipher.Cursor;
import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

public class DataStore {
    private final static String LOGTAG = "DataStore";
    private SQLiteDatabase db;
    private String profileName;

    /**
     * Initializes a new userdata store
     * @param ctxt Context
     * @param passwd DB's password
     */
    public DataStore(Context ctxt, String profileName, String passwd) throws DecryptionException {
        this.profileName = profileName;

        SQLiteDatabase.loadLibs(ctxt);
        File dbFile = ctxt.getDatabasePath("userdata.db");
        if (!dbFile.mkdirs() || !dbFile.delete()) Log.w(LOGTAG, "Call mkdirs() or delete() failed. [NON-FATAL]");
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, passwd, null);
        } catch (SQLException e) {
            Log.e(LOGTAG, "Unable to decrypt DB. (Assuming incorrect password)");
            throw new DecryptionException("Unable to open database. (Is encrypted or is not a DB)");
        }
        Log.d(LOGTAG, "Successfully opened userdata DB.");
    }

    public void putGradesData(ArrayList<Grade> grades) {
        // Truncate table
        db.execSQL("DELETE FROM ?", new String[] {"grades_"+profileName});
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name=?", new String[] {"grades_"+profileName});

        db.execSQL("CREATE TABLE IF NOT EXISTS ?(id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, grade INTEGER, gotdate INTEGER, " +
                " type TEXT, theme TEXT, teacher TEXT);", new String[] {"grades_"+profileName});
        Log.d(LOGTAG, "Created grades table (IF NOT EXISTS) for profile "+profileName);

        Log.d(LOGTAG, "About to put "+grades.size()+" grades into DB");
        for (int i=0; i<grades.size(); i++) {
            Grade g = grades.get(i);
            db.execSQL("INSERT INTO ? (subject, grade, gotdate, type, theme, teacher) VALUES (?, ?, ?, ?, ?, ?)", new String[] {"grades_"+profileName,
                    g.getSubject(), String.valueOf(g.getGrade()), String.valueOf(g.getGotDate()), g.getType(), g.getTheme(), g.getTeacher()});
        }
        Log.d(LOGTAG, "INSERT INTO statements finished.");
    }

    public void putAveragesData(ArrayList<Average> averages) {
        // Truncate table
        db.execSQL("DELETE FROM ?", new String[] {"averages_"+profileName});
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name=?", new String[] {"averages_"+profileName});

        db.execSQL("CREATE TABLE IF NOT EXISTS ?(id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, average REAL, classaverage REAL);",
                new String[] {"averages_"+profileName});
        Log.d(LOGTAG, "Created averages table (IF NOT EXISTS) for profile "+profileName);

        Log.d(LOGTAG, "About to put "+averages.size()+" averages into DB");
        for (int i=0; i<averages.size(); i++) {
            Average avg = averages.get(i);
            db.execSQL("INSERT INTO ? (subject, average, classaverage) VALUES (?, ?, ?);",
                    new String[] {"averages_"+profileName, String.valueOf(avg.getAverage()), String.valueOf(avg.getClassAverage())});
        }
        Log.d(LOGTAG, "INSERT INTO statements finished.");
    }

    public void putAllDayEventsData(ArrayList<AllDayEvent> allDayEvents) {
        // Truncate table
        db.execSQL("DELETE FROM ?", new String[] {"alldayevents_"+profileName});
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name=?", new String[] {"alldayevents_"+profileName});

        db.execSQL("CREATE TABLE IF NOT EXISTS ?(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, date INTEGER);", new String[] {"alldayevents_"+profileName});
        Log.d(LOGTAG, "Created all-day events table (IF NOT EXISTS) for profile "+profileName);

        Log.d(LOGTAG, "About to put "+allDayEvents.size()+" all-day events into DB");
        for (int i=0; i<allDayEvents.size(); i++) {
            AllDayEvent ade = allDayEvents.get(i);
            db.execSQL("INSERT INTO ? (name, date) VALUES (?, ?);", new String[] {"alldayevents_"+profileName, ade.getName(), String.valueOf(ade.getDate())});
        }
        Log.d(LOGTAG, "INSERT INTO statements finished.");
    }

    public void putClassesData(ArrayList<Clazz> clazzes) {
        // Truncate table
        db.execSQL("DELETE FROM ?", new String[] {"clazzes_"+profileName});
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name=?", new String[] {"clazzes_"+profileName});

        db.execSQL("CREATE TABLE IF NOT EXISTS ?(id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, group TEXT, teacher TEXT," +
                " room TEXT, classnum INTEGER, begin INTEGER, end INTEGER, theme TEXT, isabsent INTEGER, absencetype TEXT, absenceprovementtype TEXT," +
                " proven INTEGER);", new String[] {"clazzes_"+profileName});
        Log.d(LOGTAG, "Created all-day events table (IF NOT EXISTS) for profile "+profileName);

        Log.d(LOGTAG, "About to put "+clazzes.size()+" all-day events into DB");
        for (int i=0; i<clazzes.size(); i++) {
            Clazz c = clazzes.get(i);
            if (c.isAbsent()) {
                db.execSQL("INSERT INTO ? (subject, group, teacher, room, classnum, begin, end, theme, isabsent, absencetype, absenceprovementtype, proven) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{
                        "clazzes_"+profileName, c.getSubject(), c.getGroup(), c.getTeacher(), c.getRoom(), String.valueOf(c.getClassnum()),
                        String.valueOf(c.getBeginTime()), String.valueOf(c.getEndTime()), c.getTheme(), "1", c.getAbsenceDetails().getType(),
                        c.getAbsenceDetails().getProvementType(), c.getAbsenceDetails().isProven() ? "1" : "0"
                });
            } else {
                db.execSQL("INSERT INTO ? (subject, group, teacher, room, classnum, begin, end, theme, isabsent) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{
                        "clazzes_"+profileName, c.getSubject(), c.getGroup(), c.getTeacher(), c.getRoom(), String.valueOf(c.getClassnum()),
                        String.valueOf(c.getBeginTime()), String.valueOf(c.getEndTime()), c.getTheme(), "0"
                });
            }
        }
        Log.d(LOGTAG, "INSERT INTO statements finished.");
    }

    public ArrayList<Grade> getGradesData() {
        Cursor c = db.rawQuery("SELECT * FROM ?", new String[] {"grades_"+profileName});
        c.moveToFirst();
        if (c.getCount() != 0) {
            ArrayList<Grade> grades = new ArrayList<>();
            do {
                Grade g = new Grade(
                        c.getString(c.getColumnIndex("subject")),
                        c.getInt(c.getColumnIndex("grade")),
                        c.getInt(c.getColumnIndex("gotdate")),
                        c.getString(c.getColumnIndex("type")),
                        c.getString(c.getColumnIndex("theme")),
                        c.getString(c.getColumnIndex("teacher"))
                );
                grades.add(g);
            } while (c.moveToNext());
            c.close();
            return grades;
        } else {
            c.close();
            return new ArrayList<>();
        }
    }

    public ArrayList<Average> getAveragesData() {
        Cursor c = db.rawQuery("SELECT * FROM ?", new String[] {"averages_"+profileName});
        c.moveToFirst();
        if (c.getCount() != 0) {
            ArrayList<Average> averages = new ArrayList<>();
            do {
                Average avg = new Average(
                        c.getString(c.getColumnIndex("subject")),
                        c.getDouble(c.getColumnIndex("average")),
                        c.getDouble(c.getColumnIndex("classaverage"))
                );
                averages.add(avg);
            } while (c.moveToNext());
            c.close();
            return averages;
        } else {
            c.close();
            return new ArrayList<>();
        }
    }

    public ArrayList<AllDayEvent> getAllDayEventsData() {
        Cursor c = db.rawQuery("SELECT * FROM ?", new String[] {"alldayevents_"+profileName});
        c.moveToFirst();
        if (c.getCount() != 0) {
            ArrayList<AllDayEvent> allDayEvents = new ArrayList<>();
            do {
                AllDayEvent ade = new AllDayEvent(
                        c.getString(c.getColumnIndex("name")),
                        c.getInt(c.getColumnIndex("date"))
                );
                allDayEvents.add(ade);
            } while (c.moveToNext());
            c.close();
            return allDayEvents;
        } else {
            c.close();
            return new ArrayList<>();
        }
    }

    public ArrayList<Clazz> getClassesData() {
        Cursor c = db.rawQuery("SELECT * FROM ?", new String[] {"clazzes_"+profileName});
        c.moveToFirst();
        if (c.getCount() != 0) {
            ArrayList<Clazz> clazzes = new ArrayList<>();
            do {
                Clazz clazz;
                if (c.getInt(c.getColumnIndex("isabsent")) == 0) {
                    clazz = new Clazz(
                            c.getString(c.getColumnIndex("subject")),
                            c.getString(c.getColumnIndex("group")),
                            c.getString(c.getColumnIndex("teacher")),
                            c.getString(c.getColumnIndex("room")),
                            c.getInt(c.getColumnIndex("classnum")),
                            c.getInt(c.getColumnIndex("begin")),
                            c.getInt(c.getColumnIndex("end")),
                            c.getString(c.getColumnIndex("theme")),
                            false, null
                    );
                } else {
                    clazz = new Clazz(
                            c.getString(c.getColumnIndex("subject")),
                            c.getString(c.getColumnIndex("group")),
                            c.getString(c.getColumnIndex("teacher")),
                            c.getString(c.getColumnIndex("room")),
                            c.getInt(c.getColumnIndex("classnum")),
                            c.getInt(c.getColumnIndex("begin")),
                            c.getInt(c.getColumnIndex("end")),
                            c.getString(c.getColumnIndex("theme")),
                            true, new AbsenceDetails(c.getString(c.getColumnIndex("absencetype")),
                                            c.getString(c.getColumnIndex("absenceprovementtype")), c.getInt(c.getColumnIndex("proven")) == 1)
                    );
                }
                clazzes.add(clazz);
            } while (c.moveToNext());
            c.close();
            return clazzes;
        } else {
            c.close();
            return new ArrayList<>();
        }
    }

    public void close() {
        db.close();
    }
}
