package com.speedyblur.kretaremastered;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.github.ajalt.reprint.core.Reprint;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Context ctxt = this;

        RadioGroup rg = (RadioGroup) findViewById(R.id.setting_security_group);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (i == R.id.setting_security_fingerprint) {
                    Reprint.initialize(ctxt);
                    if (!Reprint.isHardwarePresent()) {
                        Snackbar.make(findViewById(R.id.settings_coord_view), R.string.fingerprint_fatal_no_hw, Snackbar.LENGTH_LONG).show();
                        RadioButton cbToSelect = (RadioButton) findViewById(R.id.setting_security_none);
                        cbToSelect.setChecked(true);
                    }
                    if (!Reprint.hasFingerprintRegistered()) {
                        Snackbar.make(findViewById(R.id.settings_coord_view), R.string.fingerprint_fatal_no_fingerprints, Snackbar.LENGTH_LONG).show();
                        RadioButton cbToSelect = (RadioButton) findViewById(R.id.setting_security_none);
                        cbToSelect.setChecked(true);
                    }

                } else if (i == R.id.setting_security_passcode) {

                }
            }
        });
    }
}
