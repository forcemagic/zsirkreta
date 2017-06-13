package com.speedyblur.kretaremastered;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SecurityActivity extends AppCompatActivity {

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        SharedPreferences shPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        if (shPrefs.getBoolean("fingerprintingEnabled", false)) {

        } else if (shPrefs.getBoolean("keycodingEnabled", false)) {

        } else {
            Intent it = new MainLogin().getIntent();
            this.startActivity(it);
        }
    }
}
