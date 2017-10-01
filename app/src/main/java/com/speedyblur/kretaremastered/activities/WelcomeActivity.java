package com.speedyblur.kretaremastered.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.mtramin.rxfingerprint.RxFingerprint;
import com.mtramin.rxfingerprint.data.FingerprintDecryptionResult;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.fragments.WelcomeSlideEndFragment;
import com.speedyblur.kretaremastered.fragments.WelcomeSlideFingerprintFragment;
import com.speedyblur.kretaremastered.fragments.WelcomeSlideFirstFragment;
import com.speedyblur.kretaremastered.fragments.WelcomeSlideSetsqlpassFragment;
import com.speedyblur.kretaremastered.shared.AccountStore;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DecryptionException;

import io.reactivex.functions.Consumer;

// This code is kind of a mess (though it works) - and linting says no problems found :)
// TODO: Refactor
public class WelcomeActivity extends AppCompatActivity {

    private int currentPage;
    private SharedPreferences shPrefs;

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shPrefs = getSharedPreferences("main", MODE_PRIVATE);
        if (shPrefs.getBoolean("isFirstStart", true)) {
            // Deleting leftover DB
            getDatabasePath("accounts.db").mkdirs();
            getDatabasePath("accounts.db").delete();
            getDatabasePath("userdata.db").mkdirs();
            getDatabasePath("userdata.db").delete();

            setContentView(R.layout.activity_welcome);

            getSupportFragmentManager().beginTransaction().add(R.id.welcomeFragment, new WelcomeSlideFirstFragment()).commit();
            currentPage = 0;
        } else {
            if (!canDecryptSqlite(Common.SQLCRYPT_PWD)) {
                setContentView(R.layout.activity_unlockdb);

                final ViewFlipper vf = (ViewFlipper) findViewById(R.id.unlockdbFlipper);
                final TextView statusText = (TextView) findViewById(R.id.fingerprintUnlockDbTitle);

                // Handle Fingerprint stuff
                if (shPrefs.getBoolean("doUseFingerprint", false)) {
                    vf.setDisplayedChild(0);

                    RxFingerprint.decrypt(this, shPrefs.getString("encryptedPwd", "")).subscribe(new Consumer<FingerprintDecryptionResult>() {
                        @Override
                        public void accept(FingerprintDecryptionResult fdr) throws Exception {
                            switch (fdr.getResult()) {
                                case FAILED:
                                    statusText.setText(R.string.welcome_unknown_fingerprint);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            statusText.setText(R.string.fingerprint_unlockdb_title);
                                        }
                                    }, 2000);
                                    break;
                                case HELP:
                                    statusText.setText(fdr.getMessage());
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            statusText.setText(R.string.fingerprint_unlockdb_title);
                                        }
                                    }, 2000);
                                    break;
                                case AUTHENTICATED:
                                    Common.SQLCRYPT_PWD = fdr.getDecrypted();
                                    if (canDecryptSqlite(Common.SQLCRYPT_PWD)) {
                                        finish();
                                        Intent it = new Intent(WelcomeActivity.this, ProfileListActivity.class);
                                        startActivity(it);
                                    } else {
                                        showOnStatusbar(getString(R.string.decrypt_database_fail));
                                    }
                                    break;
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            showOnStatusbar(throwable.getLocalizedMessage());
                            vf.setDisplayedChild(1);
                        }
                    });

                    findViewById(R.id.fingerprintUnlockDbPasswdBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            vf.setDisplayedChild(1);
                        }
                    });
                } else vf.setDisplayedChild(1);

                // Set up "Enter" key action
                EditText mPasswdView = (EditText) findViewById(R.id.unlockDbPassword);
                mPasswdView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        if (i == R.id.unlockDb || i == EditorInfo.IME_NULL) {
                            tryDecryptSqlite(null);
                            return true;
                        }
                        return false;
                    }
                });
            } else {
                // We can use the default password, as nothing else has been provided previously
                finish();
                Intent it = new Intent(WelcomeActivity.this, ProfileListActivity.class);
                startActivity(it);
            }
        }
    }

    public void commitSqlPassword(final View v) {
        EditText mSqlPass = (EditText) findViewById(R.id.sqlPass);
        String gotPasswd = mSqlPass.getText().toString().trim();
        if (!TextUtils.isEmpty(gotPasswd)) {
            Common.SQLCRYPT_PWD = gotPasswd;
            goToNext(v);
        } else {
            AlertDialog.Builder useEmpty = new AlertDialog.Builder(this)
                .setTitle(R.string.welcome_useempty_title)
                .setMessage(R.string.welcome_useempty_desc)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        goToNext(v);
                    }
                })
                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
            useEmpty.show();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void goToNext(@SuppressWarnings("UnusedParameters") View v) {
        if (currentPage < 3) {
            Fragment frag;
            if (currentPage == 0) {
                frag = new WelcomeSlideSetsqlpassFragment();
            } else if (currentPage == 1) {
                frag = new WelcomeSlideFingerprintFragment();
            } else {
                frag = new WelcomeSlideEndFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.welcomeFragment, frag).commit();
            currentPage++;
        } else {
            shPrefs.edit().putBoolean("isFirstStart", false).apply();
            finish();
            Intent it = new Intent(WelcomeActivity.this, ProfileListActivity.class);
            startActivity(it);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void tryDecryptSqlite(@SuppressWarnings("UnusedParameters") View v) {
        EditText mDbPasswd = (EditText) findViewById(R.id.unlockDbPassword);
        if (canDecryptSqlite(mDbPasswd.getText().toString())) {
            Common.SQLCRYPT_PWD = mDbPasswd.getText().toString();
            finish();
            Intent it = new Intent(WelcomeActivity.this, ProfileListActivity.class);
            startActivity(it);
        } else {
            mDbPasswd.setError(getResources().getString(R.string.decrypt_database_fail));
        }
    }

    private boolean canDecryptSqlite(String passwd) {
        try {
            AccountStore ash = new AccountStore(this, passwd);
            ash.close();
            Log.d("DecryptDB", "Decryption succeeded.");
            return true;
        } catch (DecryptionException e) {
            Log.w("DecryptDB", "Decryption failed.");
            return false;
        }
    }

    public void showOnStatusbar(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(R.id.welcomeCoordinator), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
