package com.speedyblur.kretaremastered.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.adapters.ProfileAdapter;
import com.speedyblur.kretaremastered.models.Profile;
import com.speedyblur.kretaremastered.shared.AccountStore;
import com.speedyblur.kretaremastered.shared.Common;
import com.speedyblur.kretaremastered.shared.DecryptionException;
import com.speedyblur.kretaremastered.shared.GradeSeparatorDecoration;

import java.util.ArrayList;

public class ManageProfilesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_profiles);

        // Back button
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.profile_manage);

        final String currentProfileId = getIntent().getStringExtra("currentProfileId");
        final RecyclerView profList = findViewById(R.id.manageProfilesList);
        profList.setLayoutManager(new LinearLayoutManager(this));
        profList.addItemDecoration(new GradeSeparatorDecoration(this));

        // Fetch accounts (asynchronously!)
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AccountStore as = new AccountStore(ManageProfilesActivity.this, Common.SQLCRYPT_PWD);
                    final ArrayList<Profile> profiles = as.getAccounts();
                    as.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            profList.setAdapter(new ProfileAdapter(profiles, currentProfileId));
                        }
                    });
                } catch (DecryptionException e) {e.printStackTrace();}
            }
        }).run();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }
}
