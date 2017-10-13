package com.speedyblur.kretaremastered.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.models.AllDayEvent;
import com.speedyblur.kretaremastered.models.Clazz;
import com.speedyblur.kretaremastered.models.ClazzDeserializer;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.shared.AccountStore;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DataStore;
import com.speedyblur.kretaremastered.shared.DecryptionException;
import com.speedyblur.kretaremastered.shared.HttpHandler;

import net.sqlcipher.database.SQLiteConstraintException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewProfileActivity extends AppCompatActivity {

    private boolean doShowMenu = true;

    // UI references.
    private ViewFlipper mLoginFlipperView;
    private EditText mFriendlyNameView;
    private EditText mIdView;
    private EditText mPasswordView;
    private ProgressBar mProgressBar;
    private TextView mProgressStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newprofile);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(R.string.profile_add);

        // Set up the login form.
        mIdView = findViewById(R.id.studentid);
        mPasswordView = findViewById(R.id.password);
        mFriendlyNameView = findViewById(R.id.friendlyname);
        mLoginFlipperView = findViewById(R.id.login_flipper);
        mProgressStatusView = findViewById(R.id.login_progress_status);
        mProgressBar = findViewById(R.id.login_progress);

        mFriendlyNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (doShowMenu) {
            getMenuInflater().inflate(R.menu.new_profile, menu);
            return true;
        } else return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_create_profile) {
            attemptLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        mProgressBar.setIndeterminate(true);
        mProgressBar.setProgress(0);
        mProgressStatusView.setText(R.string.loading_logging_in);

        // Populate a JSONObject with the payload
        JSONObject payload = new JSONObject();
        try {
            payload.put("username", studentId);
            payload.put("password", passwd);
        } catch (JSONException e) { e.printStackTrace(); }

        // Enqueue request
        HttpHandler.postJson(Common.APIBASE + "/auth", payload, new HttpHandler.JsonRequestCallback() {
            @Override
            public void onComplete(JSONObject resp) throws JSONException {
                final Profile p = new Profile(studentId, passwd, friendlyName);

                ArrayMap<String, String> headers = new ArrayMap<>();
                headers.put("X-Auth-Token", resp.getString("token"));
                HttpHandler.getJson(Common.APIBASE + "/schedule", headers, new HttpHandler.JsonRequestCallback() {
                    @Override
                    public void onComplete(JSONObject resp) throws JSONException {
                        try {
                            ArrayList<AllDayEvent> allDayEvents = new ArrayList<>();
                            for (int i = 0; i < resp.getJSONObject("data").getJSONArray("allday").length(); i++) {
                                JSONObject currentObj = resp.getJSONObject("data").getJSONArray("allday").getJSONObject(i);
                                AllDayEvent ade = new Gson().fromJson(currentObj.toString(), AllDayEvent.class);
                                allDayEvents.add(ade);
                            }

                            ArrayList<Clazz> clazzes = new ArrayList<>();
                            for (int i = 0; i < resp.getJSONObject("data").getJSONArray("classes").length(); i++) {
                                JSONObject currentObj = resp.getJSONObject("data").getJSONArray("classes").getJSONObject(i);
                                GsonBuilder gsonBuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
                                gsonBuilder.registerTypeAdapter(Clazz.class, new ClazzDeserializer());
                                Clazz c = gsonBuilder.create().fromJson(currentObj.toString(), Clazz.class);
                                clazzes.add(c);
                            }

                            // I do not like setting these every time an INSERT INTO completes
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setIndeterminate(false);
                                    mProgressBar.setMax(10000);
                                }
                            });
                            // Commit
                            DataStore ds = new DataStore(getApplicationContext(), p.getCardid(), Common.SQLCRYPT_PWD);
                            ds.putAllDayEventsData(allDayEvents);
                            ds.putClassesData(clazzes, new DataStore.InsertProcessCallback() {
                                @Override
                                public void onInsertComplete(final int current, final int total) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            float percentage = (float) current / total * 100;
                                            mProgressBar.setProgress(Math.round(percentage * 100));
                                            mProgressStatusView.setText(getString(R.string.filling_schedule, percentage));
                                        }
                                    });
                                }
                            });
                            ds.close();

                            AccountStore ash = new AccountStore(getApplicationContext(), Common.SQLCRYPT_PWD);
                            ash.addAccount(p);
                            ash.close();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setResult(RESULT_OK, getIntent().putExtra("profileId", p.getCardid()));
                                    finish();
                                    if (getIntent().getBooleanExtra("doOpenMainActivity", false)) {
                                        Intent mainIntent = new Intent(NewProfileActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                    }
                                }
                            });
                        } catch (DecryptionException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false);
                                }
                            });
                            showOnSnackbar(R.string.decrypt_database_fail, Snackbar.LENGTH_LONG);
                        } catch (SQLiteConstraintException e) {
                            // TODO: Check this beforehand
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgress(false);
                                }
                            });
                            showOnSnackbar(R.string.profile_exists, Snackbar.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onFailure(int localizedError) {
                        showOnSnackbar(localizedError, Snackbar.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void onFailure(final int localizedError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                    }
                });
                showOnSnackbar(localizedError, Snackbar.LENGTH_LONG);
            }
        });
    }

    /**
     * Shows a message on the Snackbar.
     * @param message the message to show
     * @param length Snackbar.LENGTH_*
     */
    private void showOnSnackbar(@StringRes final int message, @SuppressWarnings("SameParameterValue") final int length) {
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
    private void showProgress(boolean show) {
        doShowMenu = !show;
        invalidateOptionsMenu();
        mLoginFlipperView.setDisplayedChild(show ? 1 : 0);
    }
}

