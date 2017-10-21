package com.speedyblur.kretaremastered.shared;

import android.content.Context;
import android.util.Log;

import com.speedyblur.kretaremastered.models.Institute;
import com.speedyblur.kretaremastered.models.Profile;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteConstraintException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import java.io.File;
import java.util.ArrayList;

public class AccountStore {
    private final static String LOGTAG = "AccountStore";

    private SQLiteDatabase db;
    private final Context ctxt;

    public AccountStore(Context ctxt, String dbpasswd) throws DecryptionException {
        this.ctxt = ctxt;

        SQLiteDatabase.loadLibs(ctxt);
        File dbFile = ctxt.getDatabasePath("accounts.db");
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, dbpasswd, null);
        } catch (SQLiteException e) {
            Log.e(LOGTAG, "Unable to open DB. (Assuming incorrect password) Got error: "+e.getMessage());
            e.printStackTrace();
            throw new DecryptionException();
        }

        db.execSQL("CREATE TABLE IF NOT EXISTS accounts(cardid VARCHAR(12) PRIMARY KEY NOT NULL, friendlyname VARCHAR(16), passwd VARCHAR(30) NOT NULL, institutename TEXT, instituteid TEXT);");
        if (!columnExists("institutename") && !columnExists("instituteid")) {
            db.execSQL("ALTER TABLE accounts ADD COLUMN institutename TEXT DEFAULT 'Dobó Katalin Gimnázium'");
            db.execSQL("ALTER TABLE accounts ADD COLUMN instituteid TEXT DEFAULT klik031937001");
        }
    }

    public void addAccount(Profile p) throws SQLiteConstraintException {
        db.execSQL("INSERT INTO accounts VALUES (?, ?, ?, ?, ?);", new String[] {p.getCardid(), p.getFriendlyName(), p.getPasswd(), p.getInstitute().getName(), p.getInstitute().getId()});
    }

    public ArrayList<Profile> getAccounts() {
        Cursor c = db.rawQuery("SELECT * FROM accounts", null);
        c.moveToFirst();
        if (c.getCount() != 0) {
            ArrayList<Profile> profiles = new ArrayList<>();
            do {
                Profile cProf = new Profile(
                        c.getString(c.getColumnIndex("cardid")),
                        c.getString(c.getColumnIndex("passwd")),
                        c.getString(c.getColumnIndex("friendlyname")),
                        new Institute(c.getString(c.getColumnIndex("institutename")), c.getString(c.getColumnIndex("instituteid")))
                );
                profiles.add(cProf);
            } while (c.moveToNext());
            c.close();
            return profiles;
        } else {
            c.close();
            return new ArrayList<>();
        }
    }

    public void dropAccount(String cardid) {
        db.execSQL("DELETE FROM accounts WHERE cardid='"+cardid+"'");

        try {
            DataStore ds = new DataStore(ctxt, cardid, Common.SQLCRYPT_PWD);
            ds.purgeEverything();
        } catch (DecryptionException e) {e.printStackTrace();}
    }

    private boolean columnExists(String colName) {
        Cursor c = db.rawQuery("SELECT * FROM accounts LIMIT 1", null);
        c.moveToFirst();
        boolean isPresent = c.getColumnIndex(colName) != -1;
        c.close();
        return isPresent;
    }

    public void close() {
        db.close();
    }
}
