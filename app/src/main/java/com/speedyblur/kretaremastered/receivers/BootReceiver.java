package com.speedyblur.kretaremastered.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.speedyblur.kretaremastered.shared.Common;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            long dateToFire = context.getSharedPreferences("main", Context.MODE_PRIVATE).getLong("birthday", 0);
            if (dateToFire != 0) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(dateToFire);
                Common.registerBirthdayReminder(context, c);
            }
        }
    }
}
