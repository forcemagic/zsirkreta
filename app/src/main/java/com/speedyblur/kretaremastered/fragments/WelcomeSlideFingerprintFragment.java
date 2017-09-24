package com.speedyblur.kretaremastered.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mtramin.rxfingerprint.RxFingerprint;
import com.mtramin.rxfingerprint.data.FingerprintEncryptionResult;
import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.activities.WelcomeActivity;
import com.speedyblur.kretaremastered.shared.Common;

import io.reactivex.functions.Consumer;

import static android.content.Context.MODE_PRIVATE;

public class WelcomeSlideFingerprintFragment extends Fragment {

    private Button mContinueBtn;
    private TextView mTitleView;
    private TextView mSubtitleView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_welcome_fingerprint, container, false);
        mTitleView = v.findViewById(R.id.welcomeFingerprintTitle);
        mSubtitleView = v.findViewById(R.id.welcomeFingerprintSubtitle);
        mContinueBtn = v.findViewById(R.id.welcomeFingerprintCancel);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final WelcomeActivity parent = (WelcomeActivity) getActivity();

        if (RxFingerprint.isAvailable(getContext())) {
            RxFingerprint.encrypt(getContext(), Common.SQLCRYPT_PWD).subscribe(new Consumer<FingerprintEncryptionResult>() {
                @Override
                public void accept(FingerprintEncryptionResult fer) throws Exception {
                    switch (fer.getResult()) {
                        case FAILED:
                            parent.showOnStatusbar(getString(R.string.welcome_unknown_fingerprint));
                            break;
                        case HELP:
                            parent.showOnStatusbar(fer.getMessage());
                            break;
                        case AUTHENTICATED:
                            // We're good to go
                            SharedPreferences.Editor shPrefs = getContext().getSharedPreferences("main", MODE_PRIVATE).edit();
                            shPrefs.putString("encryptedPwd", fer.getEncrypted());
                            shPrefs.putBoolean("doUseFingerprint", true);
                            shPrefs.apply();

                            mTitleView.setText(R.string.welcome_fingerprint_complete_title);
                            mSubtitleView.setText(R.string.welcome_fingerprint_complete_subtitle);
                            mContinueBtn.setText(R.string.welcome_fingerprint_continue);
                            break;
                    }
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    // Unrecoverable error
                    mTitleView.setText(R.string.welcome_fingerprint_fatal_title);
                    mSubtitleView.setText(throwable.getLocalizedMessage());
                    mContinueBtn.setText(R.string.welcome_fingerprint_fatal_continue);
                }
            });
        } else {
            mTitleView.setText(R.string.welcome_fingerprint_notavailable_title);
            mSubtitleView.setText(R.string.welcome_fingerprint_notavailable_subtitle);
            mContinueBtn.setText(R.string.welcome_fingerprint_continue);
        }
    }
}
