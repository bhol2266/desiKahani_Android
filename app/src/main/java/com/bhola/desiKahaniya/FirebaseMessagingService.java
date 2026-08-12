package com.bhola.desiKahaniya;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.net.URL;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    NotificationManager mNotificationManager;

    /**
     * Every field of an incoming message is optional. A data-only push has no
     * notification block at all, and even a notification push usually carries no
     * icon - so nothing here may be dereferenced without a check. Reading them
     * blind crashed the app whenever a push arrived.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        RemoteMessage.Notification payload = remoteMessage.getNotification();

        String title = payload != null ? payload.getTitle() : null;
        String body = payload != null ? payload.getBody() : null;
        String iconName = payload != null ? payload.getIcon() : null;

        // A push with neither title nor body has nothing to show.
        if (title == null && body == null) {
            Log.d(SplashScreen.TAG, "push ignored: no title or body");
            return;
        }

        // Alert tone and vibration. Both are best-effort - a device with no
        // default tone, or no vibrator, must not take the app down with it.
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            if (r != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    r.setLooping(false);
                }
                r.play();
            }

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                v.vibrate(new long[]{100, 300, 300, 300}, -1);
            }
        } catch (Exception e) {
            Log.w(SplashScreen.TAG, "push alert (sound/vibration) failed", e);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID");

        // The payload icon name is only a hint; fall back to the app icon when it
        // is absent or names a drawable this build does not have.
        int resourceImage = 0;
        if (iconName != null) {
            resourceImage = getResources().getIdentifier(iconName, "drawable", getPackageName());
        }
        builder.setSmallIcon(resourceImage != 0 ? resourceImage : R.drawable.app_icon);

        try {
            URL url = new URL(SplashScreen.Notification_ImageURL);
            Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            if (bitmap != null) builder.setLargeIcon(bitmap);
        } catch (Exception e) {
            // No large icon is fine - the notification still shows.
            Log.w(SplashScreen.TAG, "push image could not be loaded", e);
        }

        if ("Notification_Story".equals(remoteMessage.getData().get("KEY1"))) {
            SplashScreen.Notification_Intent_Firebase = "active";
        }
        Intent resultIntent = new Intent(this, SplashScreen.class);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //Above or Equal to SDK 31
            pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_MUTABLE);
        } else {
            //Below SDK 31
            pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        builder.setContentTitle(title);
        builder.setContentText(body);
        builder.setContentIntent(pendingIntent);
        if (body != null) {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));
        }
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);

        mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

// notificationId is a unique int for each notification that you must define
        mNotificationManager.notify(100, builder.build());
    }

}
