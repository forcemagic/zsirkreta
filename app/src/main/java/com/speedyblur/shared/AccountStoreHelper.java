package com.speedyblur.shared;

import android.content.Context;
import android.util.Log;

import com.speedyblur.models.Profile;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteConstraintException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;

import java.io.File;
import java.util.ArrayList;

public class AccountStoreHelper {

    private SQLiteDatabase db;

    public AccountStoreHelper(Context ctxt, String dbpasswd) throws DatabaseDecryptionException {
        SQLiteDatabase.loadLibs(ctxt);
        File dbFile = ctxt.getDatabasePath("accounts.db");
        if (dbFile.mkdirs() && dbFile.delete()) {
            Log.d("AccountStore", "DB INIT: File delete ok.");
        }
        try {
            db = SQLiteDatabase.openOrCreateDatabase(dbFile, dbpasswd, null);
        } catch (SQLiteException e) {
            Log.e("AccountStore", "DB INIT: Unable to open DB. (Assuming incorrect password)");
            throw new DatabaseDecryptionException("Unable to open database. (Is encrypted or is not a DB)");
        }
        db.execSQL("CREATE TABLE IF NOT EXISTS accounts(cardid VARCHAR(12) PRIMARY KEY NOT NULL, friendlyname VARCHAR(50), passwd VARCHAR(50) NOT NULL);");
        Log.d("AccountStore", "DB INIT: OK.");
    }

    public void addAccount(Profile p) throws SQLiteConstraintException {
        if (p.cardid.length() > 12 || p.friendlyName.length() > 50 || p.getPasswd().length() > 50) {
            throw new SQLiteException("Unable to create record: parameter length(s) mismatch");
        }
        db.rawQuery("INSERT INTO accounts VALUES (?, ?, ?);", new String[] {p.cardid, p.friendlyName, p.getPasswd()});
        Log.d("AccountStore", "DB Commit OK. Added 1 record.");
    }

    public ArrayList<Profile> getAccounts() {
        Log.d("AccountStore", "Fetching accounts...");
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
        Log.d("AccountStore", "Preparing to drop an account...");
        db.execSQL("DELETE FROM accounts WHERE cardid='"+cardid+"'");
    }

    public void close() {
        db.close();
    }

    public class DatabaseDecryptionException extends Exception {
        public DatabaseDecryptionException(String explanation) {
            super(explanation);
        }
    }
}
