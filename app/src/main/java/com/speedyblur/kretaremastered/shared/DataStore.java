package com.speedyblur.kretaremastered.shared;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.speedyblur.kretaremastered.models.AbsenceDetails;
import com.speedyblur.kretaremastered.models.AllDayEvent;
import com.speedyblur.kretaremastered.models.Bulletin;
import com.speedyblur.kretaremastered.models.Average;
import com.speedyblur.kretaremastered.models.AvgGraphData;
import com.speedyblur.kretaremastered.models.Clazz;
import com.speedyblur.kretaremastered.models.Grade;

import net.sqlcipher.Cursor;
import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class DataStore {
    private final static String LOGTAG = "DataStore";
    private SQLiteDatabase db;
    private final String profileName;

    /**
     * Initializes a new userdata store
     * @param ctxt Context
     * @param passwd DB's password
     */
    public DataStore(Context ctxt, String profileName, String passwd) throws DecryptionException {
        this.profileName = profileName;

        SQLiteDatabase.loadLibs(ctxt);
        File dbFile = ctxt.getDatabasePath("userdata.db");
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, passwd, null);
        } catch (SQLException e) {
            Log.e(LOGTAG, "Unable to decrypt DB. (Assuming incorrect password) Got error: "+e.getMessage());
            throw new DecryptionException();
        }
    }

    public void putGradesData(ArrayList<Grade> grades) {
        truncateTableIfExists("grades_"+profileName);
        db.execSQL("CREATE TABLE IF NOT EXISTS grades_"+profileName+"(id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, grade INTEGER, gotdate INTEGER, " +
                " type TEXT, theme TEXT, teacher TEXT)");

        for (int i=0; i<grades.size(); i++) {
            Grade g = grades.get(i);
            db.execSQL("INSERT INTO grades_"+profileName+" (subject, grade, gotdate, type, theme, teacher) VALUES (?, ?, ?, ?, ?, ?)", new String[] {
                    g.getSubject(), String.valueOf(g.getGrade()), String.valueOf(g.getDate()), g.getType(), g.getTheme(), g.getTeacher()});
        }

        updateLastSave();
    }

    public void putAveragesData(ArrayList<Average> averages) {
        truncateTableIfExists("averages_"+profileName);
        db.execSQL("CREATE TABLE IF NOT EXISTS averages_"+profileName+"(id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, average REAL, classaverage REAL)");

        for (int i=0; i<averages.size(); i++) {
            Average avg = averages.get(i);
            db.execSQL("INSERT INTO averages_"+profileName+" (subject, average, classaverage) VALUES (?, ?, ?)",
                    new String[] {avg.getSubject(), String.valueOf(avg.getAverage()), String.valueOf(avg.getClassAverage())});
        }

        updateLastSave();
    }

    public void putAverageGraphData(ArrayList<AvgGraphData> graphData) {
        truncateTableIfExists("avggraph_"+profileName);
        db.execSQL("CREATE TABLE IF NOT EXISTS avggraph_"+profileName+"(id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, x REAL, y INTEGER, isspecial INTEGER)");

        for (int i=0; i<graphData.size(); i++) {
            AvgGraphData avgData = graphData.get(i);
            for (int j=0; j<avgData.getEntries().size(); j++) {
                Entry e = avgData.getEntries().get(j);
                db.execSQL("INSERT INTO avggraph_" + profileName + " (subject, x, y, isspecial) VALUES (?, ?, ?, ?)",
                        new String[]{avgData.getSubject(), String.valueOf((int)e.getX()), String.valueOf(e.getY()), String.valueOf((boolean) e.getData() ? 1 : 0)});
            }
        }

        updateLastSave();
    }

    public void putAllDayEventsData(ArrayList<AllDayEvent> allDayEvents) {
        truncateTableIfExists("alldayevents_"+profileName);
        db.execSQL("CREATE TABLE IF NOT EXISTS alldayevents_"+profileName+"(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, date INTEGER)");

        for (int i=0; i<allDayEvents.size(); i++) {
            AllDayEvent ade = allDayEvents.get(i);
            db.execSQL("INSERT INTO alldayevents_"+profileName+" (name, date) VALUES (?, ?)", new String[] {ade.getName(), String.valueOf(ade.getDate())});
        }

        updateLastSave();
    }

    public void putClassesData(ArrayList<Clazz> clazzes, InsertProcessCallback ipc) {
        truncateTableIfExists("clazzes_"+profileName);
        db.execSQL("CREATE TABLE IF NOT EXISTS clazzes_"+profileName+"(id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, grp TEXT, teacher TEXT," +
                " room TEXT, classnum INTEGER, begin INTEGER, end INTEGER, theme TEXT, isheld INTEGER, isabsent INTEGER, absencetype TEXT, absenceprovementtype TEXT," +
                " proven INTEGER, UNIQUE (subject, begin, end) ON CONFLICT IGNORE);");

        db.beginTransaction();
        try {
            for (int i = 0; i < clazzes.size(); i++) {
                Clazz c = clazzes.get(i);
                if (c.isAbsent()) {
                    db.execSQL("INSERT INTO clazzes_" + profileName + " (subject, grp, teacher, room, classnum, begin, end, theme, isheld, " +
                            "isabsent, absencetype, absenceprovementtype, proven) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{
                            c.getSubject(), c.getGroup(), c.getTeacher(), c.getRoom(), String.valueOf(c.getClassnum()),
                            String.valueOf(c.getBeginTime()), String.valueOf(c.getEndTime()), c.getTheme(), c.isHeld() ? "1" : "0", "1", c.getAbsenceDetails().getType(),
                            c.getAbsenceDetails().getProvementType(), c.getAbsenceDetails().isProven() ? "1" : "0"
                    });
                } else {
                    db.execSQL("INSERT INTO clazzes_" + profileName + " (subject, grp, teacher, room, classnum, begin, end, theme, isheld, isabsent) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{
                            c.getSubject(), c.getGroup(), c.getTeacher(), c.getRoom(), String.valueOf(c.getClassnum()),
                            String.valueOf(c.getBeginTime()), String.valueOf(c.getEndTime()), c.getTheme(), c.isHeld() ? "1" : "0", "0"
                    });
                }

                ipc.onInsertComplete(i + 1, clazzes.size());
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(LOGTAG, "Unable to end DB transaction.");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        updateLastSave();
    }

    public void upsertClassData(ArrayList<Clazz> clazzes) {
        if (!tableExists("clazzes_"+profileName)) {
            // Note the UNIQUE constraint!
            db.execSQL("CREATE TABLE clazzes_"+profileName+"(id INTEGER PRIMARY KEY AUTOINCREMENT, subject TEXT, grp TEXT, teacher TEXT," +
                    " room TEXT, classnum INTEGER, begin INTEGER, end INTEGER, theme TEXT, isheld INTEGER, isabsent INTEGER, absencetype TEXT, absenceprovementtype TEXT," +
                    " proven INTEGER, UNIQUE (subject, begin, end) ON CONFLICT IGNORE);");
        }

        for (int i=0; i<clazzes.size(); i++) {
            Clazz c = clazzes.get(i);
            if (c.isAbsent()) {
                db.execSQL("UPDATE clazzes_"+profileName+" SET grp=?, teacher=?, room=?, classnum=?, theme=?, isheld=?, isabsent=?, absencetype=?, absenceprovementtype=?, proven=? " +
                        "WHERE subject=? AND begin=? AND end=?", new String[] {c.getGroup(), c.getTeacher(), c.getRoom(), String.valueOf(c.getClassnum()), c.getTheme(),
                            c.isHeld() ? "1" : "0", "1", c.getAbsenceDetails().getType(), c.getAbsenceDetails().getProvementType(), c.getAbsenceDetails().isProven() ? "1" : "0",
                            c.getSubject(), String.valueOf(c.getBeginTime()), String.valueOf(c.getEndTime())});
                db.execSQL("INSERT INTO clazzes_"+profileName+" (subject, grp, teacher, room, classnum, begin, end, theme, isheld, " +
                        "isabsent, absencetype, absenceprovementtype, proven) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{
                            c.getSubject(), c.getGroup(), c.getTeacher(), c.getRoom(), String.valueOf(c.getClassnum()),
                            String.valueOf(c.getBeginTime()), String.valueOf(c.getEndTime()), c.getTheme(), c.isHeld() ? "1" : "0", "1", c.getAbsenceDetails().getType(),
                            c.getAbsenceDetails().getProvementType(), c.getAbsenceDetails().isProven() ? "1" : "0"});
            } else {
                db.execSQL("UPDATE clazzes_"+profileName+" SET grp=?, teacher=?, room=?, classnum=?, theme=?, isheld=?, isabsent=? WHERE subject=? AND begin=? AND end=?",
                        new String[] {c.getGroup(), c.getTeacher(), c.getRoom(), String.valueOf(c.getClassnum()), c.getTheme(), c.isHeld() ? "1" : "0", "0",
                        c.getSubject(), String.valueOf(c.getBeginTime()), String.valueOf(c.getEndTime())});
                db.execSQL("INSERT INTO clazzes_"+profileName+" (subject, grp, teacher, room, classnum, begin, end, theme, isheld, isabsent) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{
                        c.getSubject(), c.getGroup(), c.getTeacher(), c.getRoom(), String.valueOf(c.getClassnum()),
                        String.valueOf(c.getBeginTime()), String.valueOf(c.getEndTime()), c.getTheme(), c.isHeld() ? "1" : "0", "0"});
            }
        }
        updateLastSave();
    }

    public void upsertAnnouncementsData(ArrayList<Bulletin> bulletins) {
        upsertAnnouncementsData(bulletins, false);
    }

    public void upsertAnnouncementsData(ArrayList<Bulletin> bulletins, boolean doUpdate) {
        if (!tableExists("announcements_"+profileName)) {
            // Note the UNIQUE constraint!
            db.execSQL("CREATE TABLE announcements_"+profileName+"(id INTEGER PRIMARY KEY AUTOINCREMENT, teacher TEXT, content TEXT, date INTEGER, isseen INTEGER,"+
                    "UNIQUE (teacher, content, date) ON CONFLICT IGNORE)");
        }

        for (int i = 0; i< bulletins.size(); i++) {
            Bulletin a = bulletins.get(i);
            if (doUpdate)
                db.execSQL("UPDATE announcements_"+profileName+" SET isseen=? WHERE teacher=? AND content=? AND date=?",
                        new String[] {a.isSeen() ? "1" : "0", a.getTeacher(), a.getContent(), String.valueOf(a.getDate())});
            db.execSQL("INSERT INTO announcements_"+profileName+" (teacher, content, date, isseen) VALUES (?, ?, ?, ?)",
                    new String[] {a.getTeacher(), a.getContent(), String.valueOf(a.getDate()), a.isSeen() ? "1" : "0"});
        }
        updateLastSave();
    }

    public ArrayList<Grade> getGradesData() {
        if (!tableExists("grades_"+profileName)) return new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM grades_"+profileName, null);
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
        if (!tableExists("averages_"+profileName)) return new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM averages_"+profileName, null);
        c.moveToFirst();
        if (c.getCount() != 0) {
            ArrayList<Average> averages = new ArrayList<>();
            do {
                Average avg = new Average(
                        c.getString(c.getColumnIndex("subject")),
                        c.getDouble(c.getColumnIndex("average")),
                        c.getDouble(c.getColumnIndex("classaverage"))
                );
                avg.setGraphData(getAverageGraphData(c.getString(c.getColumnIndex("subject"))));
                averages.add(avg);
            } while (c.moveToNext());
            c.close();
            return averages;
        } else {
            c.close();
            return new ArrayList<>();
        }
    }

    public AvgGraphData getAverageGraphData(String subject) {
        if (!tableExists("avggraph_"+profileName)) return null;
        Cursor c = db.rawQuery("SELECT x,y,isspecial FROM avggraph_"+profileName+" WHERE subject=?", new String[] {subject});
        c.moveToFirst();
        if (c.getCount() != 0) {
            ArrayList<Entry> entries = new ArrayList<>();
            do {
                entries.add(new Entry(c.getFloat(c.getColumnIndex("x")), c.getFloat(c.getColumnIndex("y")), c.getInt(c.getColumnIndex("isspecial")) == 1));
            } while (c.moveToNext());
            c.close();
            return new AvgGraphData(subject, entries);
        } else {
            c.close();
            return null;
        }
    }

    // TODO: Implement this (see MainScheduleFragment for details) and remove SuppressWarnings
    @SuppressWarnings("unused")
    public ArrayList<AllDayEvent> getAllDayEventsData() {
        if (!tableExists("alldayevents_"+profileName)) return new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM alldayevents_"+profileName, null);
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
        if (!tableExists("clazzes_"+profileName)) return new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM clazzes_"+profileName, null);
        c.moveToFirst();
        if (c.getCount() != 0) {
            ArrayList<Clazz> clazzes = new ArrayList<>();
            do {
                Clazz clazz;
                if (c.getInt(c.getColumnIndex("isabsent")) == 0) {
                    clazz = new Clazz(
                            c.getString(c.getColumnIndex("subject")),
                            c.getString(c.getColumnIndex("grp")),
                            c.getString(c.getColumnIndex("teacher")),
                            c.getString(c.getColumnIndex("room")),
                            c.getInt(c.getColumnIndex("classnum")),
                            c.getInt(c.getColumnIndex("begin")),
                            c.getInt(c.getColumnIndex("end")),
                            c.getString(c.getColumnIndex("theme")),
                            c.getInt(c.getColumnIndex("isheld")) == 1,
                            false, null
                    );
                } else {
                    clazz = new Clazz(
                            c.getString(c.getColumnIndex("subject")),
                            c.getString(c.getColumnIndex("grp")),
                            c.getString(c.getColumnIndex("teacher")),
                            c.getString(c.getColumnIndex("room")),
                            c.getInt(c.getColumnIndex("classnum")),
                            c.getInt(c.getColumnIndex("begin")),
                            c.getInt(c.getColumnIndex("end")),
                            c.getString(c.getColumnIndex("theme")),
                            c.getInt(c.getColumnIndex("isheld")) == 1,
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

    public ArrayList<Bulletin> getAnnouncementsData() {
        if (!tableExists("announcements_"+profileName)) return new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM announcements_"+profileName, null);
        c.moveToFirst();
        if (c.getCount() != 0) {
            ArrayList<Bulletin> bulletins = new ArrayList<>();
            do {
                Bulletin a = new Bulletin(
                        c.getString(c.getColumnIndex("teacher")),
                        c.getString(c.getColumnIndex("content")),
                        c.getInt(c.getColumnIndex("date")),
                        c.getInt(c.getColumnIndex("isseen")) == 1
                );
                bulletins.add(a);
            } while (c.moveToNext());
            c.close();
            return bulletins;
        } else {
            c.close();
            return new ArrayList<>();
        }
    }

    public Calendar getLastSave() {
        if (tableExists("lastsave")) {
            Calendar cal = Calendar.getInstance();

            boolean changed = false;
            Cursor c = db.rawQuery("SELECT * FROM lastsave", null);
            c.moveToFirst();
            do {
                if (c.getCount() > 0 && c.getString(c.getColumnIndex("userid")).equals(profileName)) {
                    cal.setTimeInMillis(c.getInt(c.getColumnIndex("stamp")));
                    changed = true;
                }
            } while (c.moveToNext());
            c.close();

            return changed ? cal : null;
        } else {
            return null;
        }
    }

    void purgeEverything() {
        Log.d(LOGTAG, "Purging userdata for "+profileName);
        if (tableExists("grades_"+profileName)) db.execSQL("DROP TABLE grades_"+profileName);
        if (tableExists("averages_"+profileName)) db.execSQL("DROP TABLE averages_"+profileName);
        if (tableExists("alldayevents_"+profileName)) db.execSQL("DROP TABLE alldayevents_"+profileName);
        if (tableExists("clazzes_"+profileName)) db.execSQL("DROP TABLE clazzes_"+profileName);
        if (tableExists("announcements_"+profileName)) db.execSQL("DROP TABLE announcements_"+profileName);
        if (tableExists("lastsave")) db.execSQL("DELETE FROM lastsave WHERE userid=?", new String[] {profileName});
        close();
    }

    // TODO: Call this on every SUCCESSFUL save. Not on every update.
    // TODO: Make this public after this (^) todo is complete
    private void updateLastSave() {
        db.execSQL("CREATE TABLE IF NOT EXISTS lastsave(userid TEXT, stamp INTEGER);");
        db.execSQL("INSERT INTO lastsave (userid, stamp) VALUES (?, ?);", new String[] {profileName, String.valueOf(System.currentTimeMillis())});
    }

    private void truncateTableIfExists(String tableName) {
        if (tableExists(tableName)) {
            db.execSQL("DELETE FROM "+tableName);
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE name=?", new String[] {tableName});
        }
    }

    private boolean tableExists(String tableName) {
        Cursor c = db.rawQuery("SELECT count(*) FROM SQLITE_MASTER WHERE type='table' AND name=?", new String[] {tableName});
        c.moveToFirst();
        if (c.getCount() == 1 && c.getInt(0) == 1) {
            c.close();
            return true;
        } else {
            c.close();
            return false;
        }
    }

    public void close() {
        db.close();
    }

    public interface InsertProcessCallback {
        void onInsertComplete(int current, int total);
    }

    public static void asyncQuery(final Activity a, final String sid, final String passwd, final IDataStore ids) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DataStore ds = new DataStore(a, sid, passwd);
                    final Object al = ids.requestFromStore(ds);
                    ds.close();
                    a.runOnUiThread(new Runnable() {
                        @Override
                        @SuppressWarnings("unchecked")
                        public void run() {
                            ids.processRequest(al);
                        }
                    });
                } catch (DecryptionException e) {
                    ids.onDecryptionFailure(e);
                }
            }
        }).start();
    }
}
