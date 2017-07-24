package com.speedyblur.kretaremastered;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.speedyblur.adapters.ProfileAdapter;
import com.speedyblur.models.Profile;
import com.speedyblur.shared.HttpHandler;
import com.speedyblur.shared.Vars;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * A login screen that offers login via email/password.
 */
public class MainLogin extends AppCompatActivity {

    // UI references.
    private EditText mIdView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private SharedPreferences shPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        shPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

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

        Button mEmailSignInButton = (Button) findViewById(R.id.login_btn);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // Populate Spinner
        Spinner regSpinner = (Spinner) findViewById(R.id.reg_profile_selector);
        regSpinner.setAdapter(new ProfileAdapter(this, Profile.fromSet(shPrefs.getStringSet("profiles", new ArraySet<String>()))));
        regSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Profile item = (Profile) adapterView.getSelectedItem();

                mIdView.setText(item.id);
                mPasswordView.setText(item.pwd);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Sign in method
     */
    private void attemptLogin() {
        mIdView.setError(null); mPasswordView.setError(null);

        final String studentId = mIdView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Password validation
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView; cancel = true;
        }

        // Student ID validation
        if (TextUtils.isEmpty(studentId)) {
            mIdView.setError(getString(R.string.error_field_required));
            focusView = mIdView; cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);

            // Populate a JSONObject with the payload
            JSONObject payload = new JSONObject();
            try {
                payload.put("username", studentId);
                payload.put("password", password);
            } catch (JSONException e) { e.printStackTrace(); }

            HttpHandler.postJson(Vars.APIBASE + "/auth", payload, new HttpHandler.JsonRequestCallback() {
                @Override
                public void onComplete(JSONObject resp) throws JSONException {
                    // Login successful, saving login data (if needed)
                    CheckBox remCheck = (CheckBox) findViewById(R.id.remember_data_check);
                    if (remCheck.isChecked()) {
                        Log.d("SharedPreferences", "Saving login data...");
                        Set<String> profiles = shPrefs.getStringSet("profiles", new ArraySet<String>());
                        if (!profiles.contains(studentId + "@" + password)) {
                            profiles.add(studentId + "@" + password);
                        }
                        SharedPreferences.Editor shEdit = shPrefs.edit();
                        shEdit.putStringSet("profiles", profiles);
                        shEdit.apply();
                        Log.d("SharedPreferences", "Sent apply().");
                    }

                    // Capturing auth token
                    String authToken = resp.getString("token");
                    Log.d("Shared", "Auth token is "+authToken.substring(0,10)+"...");
                    Vars.AUTHTOKEN = authToken;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                        }
                    });

                    // Opening new activity
                    Log.d("Shared", "Creating new activity 'MainLogin'.");
                    Intent it = new Intent(MainLogin.this, MainActivity.class);
                    startActivity(it);
                }

                @Override
                public void onFailure(final int localizedError) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(findViewById(R.id.login_coord_view), localizedError, Snackbar.LENGTH_LONG).show();
                            showProgress(false);
                        }
                    });
                }
            });
        }
    }

    /**
     * Shows the progress UI and hides the login form.
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

