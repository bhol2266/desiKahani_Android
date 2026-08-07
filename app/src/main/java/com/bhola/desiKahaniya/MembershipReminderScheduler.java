package com.bhola.desiKahaniya;

import android.content.Context;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Schedules the local, on-device pre-expiry membership reminder
 * (MembershipReminderWorker). No backend involved - this repo has no server
 * component to drive a scheduled FCM push, so the notification is scheduled
 * client-side whenever the app learns a purchase's expiry date (new purchase
 * ack or a restore). WorkManager persists the enqueued job across process
 * death and reboots, so it still fires even if the app isn't reopened.
 */
final class MembershipReminderScheduler {

    private static final String UNIQUE_WORK_NAME = "membership_expiry_reminder";
    private static final long LEAD_DAYS = 2;

    private MembershipReminderScheduler() {
    }

    /** expiryMillis: epoch millis when the current purchase's validity window ends. */
    static void schedule(Context context, long expiryMillis) {
        long fireAt = expiryMillis - TimeUnit.DAYS.toMillis(LEAD_DAYS);
        long delay = fireAt - System.currentTimeMillis();

        WorkManager workManager = WorkManager.getInstance(context);
        if (delay <= 0) {
            // Already inside the reminder window, or membership already lapsed -
            // the home-grid banner and the SplashScreen toast cover that case.
            // Nothing useful to schedule.
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME);
            return;
        }

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MembershipReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        // REPLACE: renewals/restores keep pushing the expiry date out, so each
        // save must supersede whatever was scheduled before it.
        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request);
    }
}
