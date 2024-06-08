package com.bhola.desiKahaniya;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class TimerService extends Service {
    private static final long ONE_HOUR = 60 * 60 * 1000;
    private CountDownTimer timer;

    @Override
    public void onCreate() {
        super.onCreate();

        timer = new CountDownTimer(ONE_HOUR, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Intent intent = new Intent("timer-update");
                intent.putExtra("remainingTime", millisUntilFinished);
                sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                Intent intent = new Intent("timer-finish");
                sendBroadcast(intent);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        startForeground(1, createNotification());
        timer.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "timer_channel",
                    "Timer Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, VipMembership.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "timer_channel")
                .setContentTitle("Desi Kahaniya")
                .setContentText("VIP Membership Offer is running out")
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pendingIntent);

        return builder.build();
    }

}

