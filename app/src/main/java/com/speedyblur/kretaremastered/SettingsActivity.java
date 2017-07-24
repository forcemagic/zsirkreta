package com.speedyblur.kretaremastered;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;
import com.speedyblur.shared.Vars;

public class SettingsActivity extends AppCompatActivity {

    private int STATE_OF_PASSCODE = 0;
    private String CURRENT_PASSCODE;
    private SharedPreferences shPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Some global finals
        final Context ctxt = this;
        shPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Load saved
        int authType = shPrefs.getInt("security", -1);
        if (authType == Vars.AuthType.FINGERPRINT) {
            RadioButton cbToSelect = (RadioButton) findViewById(R.id.setting_security_fingerprint);
            cbToSelect.setChecked(true);
        } else if (authType == Vars.AuthType.PASSCODE) {
            RadioButton cbToSelect = (RadioButton) findViewById(R.id.setting_security_passcode);
            cbToSelect.setChecked(true);
        }

        // Set RG hooks
        RadioGroup rg = (RadioGroup) findViewById(R.id.setting_security_group);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                // Hide other UI components that are not needed (we'll show them later if needed)
                final EditText etPasswd = (EditText) findViewById(R.id.setting_passcode_entry);
                final TextView tvStatus = (TextView) findViewById(R.id.setting_passcode_status);
                Button btnOk = (Button) findViewById(R.id.setting_passcode_okbtn);
                etPasswd.setVisibility(View.INVISIBLE);
                tvStatus.setVisibility(View.INVISIBLE);
                btnOk.setVisibility(View.INVISIBLE);

                // Fallback RadioBtn on fatal exception
                final RadioButton cbToSelect = (RadioButton) findViewById(R.id.setting_security_none);

                if (i == R.id.setting_security_fingerprint) {
                    Reprint.initialize(ctxt);
                    // Check for basics
                    if (!Reprint.isHardwarePresent()) {
                        Snackbar.make(findViewById(R.id.settings_coord_view), R.string.fingerprint_fatal_no_hw, Snackbar.LENGTH_LONG).show();
                        cbToSelect.setChecked(true);
                        modAuthType(Vars.AuthType.NONE);
                        return;
                    }
                    if (!Reprint.hasFingerprintRegistered()) {
                        Snackbar.make(findViewById(R.id.settings_coord_view), R.string.fingerprint_fatal_no_fingerprints, Snackbar.LENGTH_LONG).show();
                        cbToSelect.setChecked(true);
                        modAuthType(Vars.AuthType.NONE);
                        return;
                    }

                    // Wait for fingerprint
                    Snackbar.make(findViewById(R.id.settings_coord_view), R.string.fingerprint_dialog_waiting, Snackbar.LENGTH_INDEFINITE).show();
                    Reprint.authenticate(new AuthenticationListener() {
                        @Override
                        public void onSuccess(int moduleTag) {
                            modAuthType(Vars.AuthType.FINGERPRINT);
                            Snackbar.make(findViewById(R.id.settings_coord_view), R.string.fingerprint_auth_success, Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(AuthenticationFailureReason failureReason, boolean fatal, CharSequence errorMessage, int moduleTag, int errorCode) {
                            if (failureReason == AuthenticationFailureReason.LOCKED_OUT) {
                                Snackbar.make(findViewById(R.id.settings_coord_view), R.string.fingerprint_fatal_locked_out, Snackbar.LENGTH_LONG).show();
                                modAuthType(Vars.AuthType.NONE);
                                cbToSelect.setChecked(true);
                            } else if (fatal) {
                                Snackbar.make(findViewById(R.id.settings_coord_view), R.string.fingerprint_fatal_unknown_error, Snackbar.LENGTH_LONG).show();
                                modAuthType(Vars.AuthType.NONE);
                                cbToSelect.setChecked(true);
                            }
                        }
                    });
                } else if (i == R.id.setting_security_passcode) {
                    etPasswd.setVisibility(View.VISIBLE);
                    tvStatus.setVisibility(View.VISIBLE);
                    btnOk.setVisibility(View.VISIBLE);

                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (STATE_OF_PASSCODE == 0) {
                                String passwd = etPasswd.getText().toString();
                                if (passwd.length() == 4) {
                                    CURRENT_PASSCODE = passwd;
                                    STATE_OF_PASSCODE = 1;
                                    tvStatus.setText(R.string.settings_passcode_status_again);
                                } else {
                                    tvStatus.setText(R.string.settings_passcode_status_lengtherr);
                                    cbToSelect.setChecked(true);
                                    modAuthType(Vars.AuthType.NONE);
                                }
                            } else if (STATE_OF_PASSCODE == 1) {
                                String againPasswd = etPasswd.getText().toString();
                                if (CURRENT_PASSCODE.equals(againPasswd)) {
                                    tvStatus.setText(R.string.settings_passcode_status_saved);

                                    SharedPreferences.Editor shEdit = shPrefs.edit();
                                    shEdit.putString("passcode", CURRENT_PASSCODE);
                                    shEdit.putInt("security", Vars.AuthType.PASSCODE);
                                    shEdit.apply();

                                    CURRENT_PASSCODE = null;
                                    STATE_OF_PASSCODE = 0;
                                } else {
                                    tvStatus.setText(R.string.settings_passcode_status_nomatch);
                                    STATE_OF_PASSCODE = 0;
                                    cbToSelect.setChecked(true);
                                    modAuthType(Vars.AuthType.NONE);
                                }
                            }
                            etPasswd.setText("");
                        }
                    });
                } else if (i == R.id.setting_security_none) {
                    modAuthType(Vars.AuthType.NONE);
                }
            }
        });
    }

    // Give it a Vars.AuthType.*, and it will set the "security" sharedPref to the supplied value.
    private void modAuthType(int authType) {
        SharedPreferences.Editor shEdit = shPrefs.edit();
        shEdit.putInt("security", authType);
        shEdit.apply();
    }
}
