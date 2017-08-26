package com.speedyblur.kretaremastered.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.shared.AccountStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;
import com.speedyblur.kretaremastered.shared.HttpHandler;
import com.speedyblur.kretaremastered.shared.Vars;

import net.sqlcipher.database.SQLiteConstraintException;

import org.json.JSONException;
import org.json.JSONObject;

public class NewProfileActivity extends AppCompatActivity {

    // UI references.
    private EditText mFriendlyNameView;
    private EditText mIdView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newprofile);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.profile_add);

        // Set up the login form.
        mIdView = (EditText) findViewById(R.id.studentid);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mFriendlyNameView = (EditText) findViewById(R.id.friendlyname);

        Button mEmailSignInButton = (Button) findViewById(R.id.login_btn);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Sign in method
     */
    private void attemptLogin() {
        mIdView.setError(null); mPasswordView.setError(null);

        String studentId = mIdView.getText().toString();
        String password = mPasswordView.getText().toString();
        String friendlyName = mFriendlyNameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Validations
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView; cancel = true;
        }
        if (TextUtils.isEmpty(studentId)) {
            mIdView.setError(getString(R.string.error_field_required));
            focusView = mIdView; cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            doLoginSaveIfValid(studentId, password, friendlyName);
        }
    }

    /**
     * This function ONLY CHECKS for login data validity and saves it into the cryptSql DB. It DOES NOT save the authentication token!
     * @param studentId the student's card's ID
     * @param passwd the password to use
     * @param friendlyName a friendly name for the profile
     */
    private void doLoginSaveIfValid(final String studentId, final String passwd, final String friendlyName) {
        // Populate a JSONObject with the payload
        JSONObject payload = new JSONObject();
        try {
            payload.put("username", studentId);
            payload.put("password", passwd);
        } catch (JSONException e) { e.printStackTrace(); }

        // Enqueue request
        HttpHandler.postJson(Vars.APIBASE + "/auth", payload, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                try {
                    Profile p = new Profile(studentId, passwd, friendlyName);

                    AccountStore ash = new AccountStore(getApplicationContext(), Vars.SQLCRYPT_PWD);
                    ash.addAccount(p);
                    ash.close();

                    Intent i = getIntent();
                    i.putExtra("profile", p);
                    setResult(RESULT_OK, i);
                    finish();
                } catch (DecryptionException e) {
                    showOnSnackbar(R.string.decrypt_database_fail, Snackbar.LENGTH_LONG);
                } catch (SQLiteConstraintException e) {
                    showOnSnackbar(R.string.profile_exists, Snackbar.LENGTH_LONG);
                }
            }

            @Override
            public void onFailure(final int localizedError) {
                showOnSnackbar(localizedError, Snackbar.LENGTH_LONG);
            }
        });
    }

    /**
     * Shows a message on the Snackbar.
     * @param message the message to show
     * @param length Snackbar.LENGTH_*
     */
    private void showOnSnackbar(@StringRes final int message, final int length) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(R.id.login_coord_view), message, length).show();
            }
        });
    }


    /**
     * Shows the progress UI and hides the login form (or, if you want, the other way around)
     * @param show whether to show the progress or not
     */
    private void showProgress(final boolean show) {
        // Fade-in progress bar
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}

