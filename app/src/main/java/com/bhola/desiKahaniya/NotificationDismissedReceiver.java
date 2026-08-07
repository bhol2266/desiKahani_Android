package com.bhola.desiKahaniya;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class NotificationDismissedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        boolean appAlive = isAppAlive(context, context.getPackageName());

        if (appAlive) {
            // App is in foreground or background — pause audio only

        } else {
            // App is killed — stop service
            context.stopService(new Intent(context, AudioPlayerService.class));
        }
    }


    public static boolean isAppAlive(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) return false;

        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        if (processInfos == null) return false;

        for (ActivityManager.RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.processName.equals(packageName)
                    && processInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY) {
                return true;
            }
        }
        return false;
    }


}
