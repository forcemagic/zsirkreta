package com.speedyblur.kretaremastered.shared;

import android.content.Context;
import android.util.Log;

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
            throw new DecryptionException("Unable to open database. (Is encrypted or is not a DB)");
        }
        db.execSQL("CREATE TABLE IF NOT EXISTS accounts(cardid VARCHAR(12) PRIMARY KEY NOT NULL, friendlyname VARCHAR(16), passwd VARCHAR(30) NOT NULL);");
    }

    public void addAccount(Profile p) throws SQLiteConstraintException {
        db.execSQL("INSERT INTO accounts VALUES (?, ?, ?);", new String[] {p.getCardid(), p.getFriendlyName(), p.getPasswd()});
        Log.d(LOGTAG, "DB Commit OK. Added 1 record.");
    }

    public ArrayList<Profile> getAccounts() {
        Log.d(LOGTAG, "Fetching accounts...");
        Cursor c = db.rawQuery("SELECT * FROM accounts", null);
        c.moveToFirst();
        if (c.getCount() != 0) {
            ArrayList<Profile> profiles = new ArrayList<>();
            do {
                Profile cProf = new Profile(
                        c.getString(c.getColumnIndex("cardid")),
                        c.getString(c.getColumnIndex("passwd")),
                        c.getString(c.getColumnIndex("friendlyname"))
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
        Log.d(LOGTAG, "Preparing to drop account "+cardid);
        db.execSQL("DELETE FROM accounts WHERE cardid='"+cardid+"'");

        try {
            DataStore ds = new DataStore(ctxt, cardid, Common.SQLCRYPT_PWD);
            ds.purgeEverything();
        } catch (DecryptionException e) {e.printStackTrace();}

        Log.d(LOGTAG, "Account dropped successfully.");
    }

    public void close() {
        db.close();
    }
}
