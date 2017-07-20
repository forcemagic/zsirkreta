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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

            OkHttpClient htcli = new OkHttpClient();
            Request loginReq = new Request.Builder()
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload.toString()))
                    .url(Vars.APIBASE+"/auth")
                    .build();

            htcli.newCall(loginReq).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    dispatchError("Unable to connect: "+e.getLocalizedMessage(), R.string.login_other_error);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d("HttpClient", "Got 200 OK.");

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
                        try {
                            String authToken = new JSONObject(response.body().string()).getString("token");
                            Log.d("Shared", "Auth token is "+authToken.substring(0,10)+"...");
                            Vars.AUTHTOKEN = authToken;
                        } catch (JSONException e) {
                            dispatchError("Got invalid JSON from server.", R.string.login_myserver_unresponsive);
                            return;
                        }
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
                    } else {
                        if (response.code() == 403) {
                            dispatchError("Unable to authenticate with Kreta servers.", R.string.login_incorrect_account_details);
                        } else if (response.code() == 502) {
                            dispatchError(String.format("Kreta is unresponsive. (%s)", response), R.string.login_kreta_unresponsive);
                        } else {
                            dispatchError(String.format("Unknown server error. (%s)", response), R.string.login_myserver_unresponsive);
                        }
                    }
                }
            });
            Log.d("HttpClient", "Enqueued request. Waiting for response...");
        }
    }

    private void dispatchError(String message, final int localizedMsgId) {
        Log.e("CoreHandler", message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(findViewById(R.id.login_coord_view), localizedMsgId, Snackbar.LENGTH_LONG).show();
                showProgress(false);
            }
        });
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

