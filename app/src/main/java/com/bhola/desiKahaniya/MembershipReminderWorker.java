package com.bhola.desiKahaniya;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Fires a local notification a couple of days before a VIP membership lapses,
 * scheduled by MembershipReminderScheduler. Runs even if the app hasn't been
 * reopened since the reminder was scheduled.
 */
public class MembershipReminderWorker extends Worker {

    private static final String CHANNEL_ID = "membership_reminder_channel";
    private static final int NOTIFICATION_ID = 200;

    public MembershipReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // The membership may have been renewed to a later date, cancelled, or
        // already lapsed since this work was scheduled - re-check the live
        // state instead of blindly firing.
        SharedPreferences sp = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        if (!SplashScreen.isMembershipActive(sp)) {
            return Result.success();
        }

        String body = context.getString(R.string.membership_reminder_body);

        Intent resultIntent = new Intent(context, VipMembership.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int piFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, resultIntent, piFlags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(context.getString(R.string.membership_reminder_title))
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Membership Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(CHANNEL_ID);
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        return Result.success();
    }
}
