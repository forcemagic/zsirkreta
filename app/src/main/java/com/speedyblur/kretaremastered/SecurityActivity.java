package com.speedyblur.kretaremastered;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

public class SecurityActivity extends AppCompatActivity {

    SharedPreferences shPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        // Hide soft keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ViewFlipper vf = (ViewFlipper) findViewById(R.id.auth_view_flipper);
        shPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        int authType = shPrefs.getInt("security", -1);

        if (authType == Vars.AuthType.FINGERPRINT) {
            vf.setDisplayedChild(1);
            Reprint.initialize(this);
            Reprint.authenticate(new AuthenticationListener() {
                @Override
                public void onSuccess(int moduleTag) {
                    proceed();
                }

                @Override
                public void onFailure(AuthenticationFailureReason failureReason, boolean fatal, CharSequence errorMessage, int moduleTag, int errorCode) {
                    final TextView fingerStatus = (TextView) findViewById(R.id.fingerprint_status);
                    if (failureReason == AuthenticationFailureReason.AUTHENTICATION_FAILED) {
                        fingerStatus.setText(R.string.fingerprint_wrong_finger);
                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        fingerStatus.setText(R.string.fingerprint_dialog_waiting);
                                    }
                                });
                            }
                        }, 1500);
                    } else if (failureReason == AuthenticationFailureReason.LOCKED_OUT) {
                        fingerStatus.setText(R.string.fingerprint_fatal_locked_out);
                    } else {
                        fingerStatus.setText(R.string.fingerprint_fatal_unknown_error);
                    }
                }
            });
        } else if (authType == Vars.AuthType.PASSCODE) {
            vf.setDisplayedChild(0);
        } else {
            proceed();
        }
    }

    // Keypad buttons' onClick listeners
    public void addNum1(View v) {
        addNum("1");
    }
    public void addNum2(View v) {
        addNum("2");
    }
    public void addNum3(View v) {
        addNum("3");
    }
    public void addNum4(View v) {
        addNum("4");
    }
    public void addNum5(View v) {
        addNum("5");
    }
    public void addNum6(View v) {
        addNum("6");
    }
    public void addNum7(View v) {
        addNum("7");
    }
    public void addNum8(View v) {
        addNum("8");
    }
    public void addNum9(View v) {
        addNum("9");
    }
    public void addNum0(View v) {
        addNum("0");
    }
    public void remLastChar(View v) {
        EditText passcodeIn = (EditText) findViewById(R.id.security_passcode_input);
        if (passcodeIn.getText().length() <= 1) {
            passcodeIn.setText("");
        } else {
            passcodeIn.setText(passcodeIn.getText().toString().substring(0, passcodeIn.getText().length()-1));
        }
    }

    // Helper for the onClick listeners above
    private void addNum(String toAdd) {
        EditText passcodeIn = (EditText) findViewById(R.id.security_passcode_input);
        if (passcodeIn.getText().length() < 4) {
            passcodeIn.setText(passcodeIn.getText().append(toAdd));
        }
    }

    // Authenticate w/ passcode
    public void validatePasscode(View v) {
        EditText passcodeIn = (EditText) findViewById(R.id.security_passcode_input);
        if (passcodeIn.getText().toString().equals(shPrefs.getString("passcode", "!@#$"))) {
            proceed();
        } else {
            passcodeIn.setError(getResources().getString(R.string.auth_passcode_error));
            passcodeIn.setText("");
        }
    }

    // Everything's OK. Let's go!
    private void proceed() {
        finish();
        Intent it = new Intent(SecurityActivity.this, MainLogin.class);
        this.startActivity(it);
    }
}
