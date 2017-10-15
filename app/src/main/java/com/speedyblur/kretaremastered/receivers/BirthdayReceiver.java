package com.speedyblur.kretaremastered.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.speedyblur.kretaremastered.R;
import com.speedyblur.kretaremastered.shared.Common;

import java.util.Calendar;

public class BirthdayReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");

        wl.acquire();

        String notifChannel = "zsirkreta";

        NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notifManager.createNotificationChannel(new NotificationChannel(notifChannel, "ZsírKréta", NotificationManager.IMPORTANCE_DEFAULT));
        }

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context, notifChannel)
                .setLights(0xFF00FF00, 500, 100)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setContentTitle(context.getString(R.string.happy_birthday))
                .setContentText(context.getString(R.string.happy_birthday_content))
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(context.getString(R.string.happy_birthday))
                        .bigText(context.getString(R.string.happy_birthday_content)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.notification_icon);
        notifManager.notify(1, notifBuilder.build());

        SharedPreferences shPrefs = context.getSharedPreferences("main", Context.MODE_PRIVATE);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(shPrefs.getLong("birthday", 0));
        c.add(Calendar.YEAR, 1);
        shPrefs.edit().putLong("birthday", c.getTimeInMillis()).apply();
        Common.registerBirthdayReminder(context, c);

        wl.release();
    }
}
