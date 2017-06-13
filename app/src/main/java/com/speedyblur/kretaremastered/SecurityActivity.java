package com.speedyblur.kretaremastered;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import java.security.Security;

public class SecurityActivity extends AppCompatActivity {

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        ViewFlipper vf = (ViewFlipper) findViewById(R.id.auth_view_flipper);
        SharedPreferences shPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        if (shPrefs.getBoolean("fingerprintingEnabled", false)) {
            vf.setDisplayedChild(0);
            Reprint.initialize(this);
            Reprint.authenticate(new AuthenticationListener() {
                @Override
                public void onSuccess(int moduleTag) {
                    proceed();
                }

                @Override
                public void onFailure(AuthenticationFailureReason failureReason, boolean fatal, CharSequence errorMessage, int moduleTag, int errorCode) {
                    TextView fingerStatus = (TextView) findViewById(R.id.fingerprint_status);
                    if (failureReason == AuthenticationFailureReason.LOCKED_OUT) {
                        fingerStatus.setText(R.string.fingerprint_fatal_locked_out);
                    } else {
                        fingerStatus.setText(R.string.fingerprint_fatal_unknown_error);
                    }
                }
            });
        } else if (shPrefs.getBoolean("keycodingEnabled", false)) {
            vf.setDisplayedChild(1);
        } else {
            proceed();
        }
    }

    private void proceed() {
        Intent it = new Intent(SecurityActivity.this, MainLogin.class);
        this.startActivity(it);
    }
}
