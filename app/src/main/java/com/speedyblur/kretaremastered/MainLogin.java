package com.speedyblur.kretaremastered;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

/**
 * A login screen that offers login via email/password.
 */
public class MainLogin extends AppCompatActivity {

    private SharedPreferences shPrefs;

    // UI references.
    private EditText mIdView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        shPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Set up fingerprint scan
        if (shPrefs.getBoolean("fingerprintingEnabled", true)) {
            Reprint.initialize(this);
            fingerprintAuth();
        }

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

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
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

        final String studentId = mIdView.getText().toString();
        final String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView; cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(studentId)) {
            mIdView.setError(getString(R.string.error_field_required));
            focusView = mIdView; cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);

            RequestQueue mReqQueue;
            Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
            mReqQueue = new RequestQueue(cache, new BasicNetwork(new HurlStack()));
            mReqQueue.start();

            JSONObject payload = new JSONObject();
            try {
                payload.put("username", studentId);
                payload.put("password", password);
            } catch (JSONException e) { e.printStackTrace(); }

            JsonObjectRequest jsoReq = new JsonObjectRequest(Request.Method.POST, Vars.APIBASE + "/auth", payload, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        SharedPreferences.Editor shEdit = shPrefs.edit();
                        shEdit.putString("username", studentId);
                        shEdit.putString("password", password);
                        shEdit.apply();

                        Vars.AUTHTOKEN = response.getString("token");
                        Intent it = new Intent(MainLogin.this, MainActivity.class);
                        startActivity(it);
                        showProgress(false);
                    } catch (JSONException e) {
                        showProgress(false);
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse.statusCode == HttpsURLConnection.HTTP_FORBIDDEN) {
                        Snackbar.make(findViewById(R.id.login_coord_view), R.string.login_incorrect_account_details, Snackbar.LENGTH_LONG).show();
                    } else if (error.networkResponse.statusCode == HttpsURLConnection.HTTP_BAD_GATEWAY) {
                        Snackbar.make(findViewById(R.id.login_coord_view), R.string.login_kreta_unresponsive, Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(findViewById(R.id.login_coord_view), R.string.login_other_error, Snackbar.LENGTH_LONG).show();
                    }
                    showProgress(false);
                }
            });

            mReqQueue.add(jsoReq);
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

    @SuppressWarnings("deprecation")
    private void fingerprintAuth() {
        final ProgressDialog pd = new ProgressDialog(this);

        Reprint.authenticate(new AuthenticationListener() {
            @Override
            public void onSuccess(int moduleTag) {
                pd.dismiss();
                if (shPrefs.getString("username", "").equals("")) {
                    Snackbar.make(findViewById(R.id.login_coord_view), R.string.fingerprint_auth_success_firsttime, Snackbar.LENGTH_LONG).show();
                } else {
                    mIdView.setText(shPrefs.getString("username", ""));
                    mPasswordView.setText(shPrefs.getString("password", ""));
                    Snackbar.make(findViewById(R.id.login_coord_view), R.string.fingerprint_auth_success, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(AuthenticationFailureReason failureReason, boolean fatal, CharSequence errorMessage, int moduleTag, int errorCode) {
                if (!fatal) {
                    pd.setMessage(getResources().getString(R.string.fingerprint_dialog_error));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pd.setMessage(getResources().getString(R.string.fingerprint_dialog_waiting));
                        }
                    }, 1000);
                } else {
                    pd.dismiss();
                    if (failureReason == AuthenticationFailureReason.LOCKED_OUT) {
                        Snackbar.make(findViewById(R.id.login_coord_view), R.string.fingerprint_fatal_locked_out, Snackbar.LENGTH_LONG).show();
                    } else if (failureReason == AuthenticationFailureReason.NO_FINGERPRINTS_REGISTERED) {
                        Snackbar.make(findViewById(R.id.login_coord_view), R.string.fingerprint_fatal_no_fingerprints, Snackbar.LENGTH_LONG).show();
                    } else if (failureReason == AuthenticationFailureReason.NO_HARDWARE) {
                        SharedPreferences.Editor shEdit = shPrefs.edit();
                        shEdit.putBoolean("fingerprintingEnabled", false);
                        shEdit.apply();
                        Snackbar.make(findViewById(R.id.login_coord_view), R.string.fingerprint_fatal_no_hw, Snackbar.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Whoops, a fingerprint error occurred.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        pd.setMessage(getResources().getString(R.string.fingerprint_dialog_waiting));
        pd.setIndeterminateDrawable(getResources().getDrawable(R.drawable.fingerprint_icon_black));
        pd.setIndeterminate(true);
        pd.setCancelable(true);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Reprint.cancelAuthentication();
                Snackbar.make(findViewById(R.id.login_coord_view), R.string.fingerprint_auth_cancel, Snackbar.LENGTH_LONG).show();
            }
        });
        pd.show();
    }
}

