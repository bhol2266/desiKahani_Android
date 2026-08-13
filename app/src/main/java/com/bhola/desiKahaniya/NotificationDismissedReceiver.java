package com.bhola.desiKahaniya;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationDismissedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Swiping the player notification away means "stop", whether or not the
        // app is still alive. Doing nothing in the app-alive case used to leave
        // the service running with no way to reach it: its only visible control
        // had just been dismissed.
        context.stopService(new Intent(context, AudioPlayerService.class));
    }
}
