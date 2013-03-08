/*
 * Copyright (C) 2012 Benedict Lau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groundupworks.lib.photobooth.wings;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import com.groundupworks.lib.photobooth.R;
import com.groundupworks.lib.photobooth.dropbox.DropboxHelper;
import com.groundupworks.lib.photobooth.facebook.FacebookHelper;
import com.groundupworks.lib.photobooth.helpers.LogsHelper;

/**
 * An {@link IntentService} that processes {@link ShareRequest}. To ensure the device does not sleep before the service
 * is started or during {@link #onHandleIntent(Intent)}, use {@link #startWakefulService(Context)} instead of
 * {@link Context#startService(Intent)} to start this service.
 * 
 * @author Benedict Lau
 */
public class WingsService extends IntentService {

    private static final String NAME = "com.groundupworks.flyingphotobooth.wings.WingsService";

    /**
     * Static {@link WakeLock} to ensure device does not sleep before service starts and completes its work.
     */
    private static volatile PowerManager.WakeLock sWakeLock = null;

    /**
     * Constructor.
     */
    public WingsService() {
        super(NAME);
        setIntentRedelivery(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Acquire lock when the service is started from a redelivered intent.
        if ((flags & START_FLAG_REDELIVERY) != 0) {
            acquireWakeLock(getApplicationContext());
        }

        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Context appContext = getApplicationContext();
            WingsDbHelper wingsDbHelper = WingsDbHelper.getInstance(appContext);

            // Reset all records that somehow got stuck in a processing state.
            wingsDbHelper.resetProcessingShareRequests();

            // Process share requests to Facebook.
            FacebookHelper facebookHelper = new FacebookHelper();
            if (facebookHelper.isLinked(appContext)) {
                IWingsNotification notification = facebookHelper.processShareRequests(appContext);
                if (notification != null) {
                    sendNotification(appContext, notification);
                }
            }

            // Process share requests to Dropbox.
            DropboxHelper dropboxHelper = new DropboxHelper();
            if (dropboxHelper.isLinked(appContext)) {
                IWingsNotification notification = dropboxHelper.processShareRequests(appContext);
                if (notification != null) {
                    sendNotification(appContext, notification);
                }
            }

            // Purge share requests.
            if (wingsDbHelper.purge() > 0) {
                // Some share requests failed. Schedule next attempt to share.
                scheduleRetry();
            } else {
                // All share requests completed successfully. Reset retry policy.
                RetryPolicy.reset(this);
            }
        } catch (Exception e) {
            // An unexpected exception occurred. Schedule next attempt to share.
            scheduleRetry();
        } finally {
            releaseWakeLock();
        }
    }

    //
    // Private methods.
    //

    /**
     * Acquires a wake lock.
     * 
     * @param context
     *            the {@link Context}.
     */
    private synchronized static void acquireWakeLock(Context context) {
        // Setup wake lock.
        if (sWakeLock == null) {
            PowerManager powerManager = (PowerManager) context.getApplicationContext().getSystemService(
                    Context.POWER_SERVICE);
            sWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, NAME);
            sWakeLock.setReferenceCounted(true);
        }

        // Acquire lock.
        sWakeLock.acquire();

        LogsHelper.log(WingsService.class, "acquireWakeLock", "sWakeLock=" + sWakeLock);
    }

    /**
     * Releases the wake lock if one is held.
     */
    private synchronized static void releaseWakeLock() {
        if (sWakeLock != null) {
            if (sWakeLock.isHeld()) {
                sWakeLock.release();

                // Clear static reference if the lock is no longer held after the release() call.
                if (!sWakeLock.isHeld()) {
                    sWakeLock = null;
                }
            } else {
                // The lock is not held, just clear the static reference.
                sWakeLock = null;
            }
        }

        LogsHelper.log(WingsService.class, "releaseWakeLock", "sWakeLock=" + sWakeLock);
    }

    /**
     * Schedules a retry in the future. This method figures out how far in the future the next attempt should be.
     */
    private void scheduleRetry() {
        Context appContext = getApplicationContext();
        long nextRetry = System.currentTimeMillis() + RetryPolicy.incrementAndGetTime(appContext);
        scheduleWingsService(appContext, nextRetry);
    }

    /**
     * Sends a {@link IWingsNotification} to the notification bar.
     */
    private void sendNotification(Context context, IWingsNotification wingsNotification) {
        // Construct pending intent. The wrapped Intent must not be null as some versions of Android require it.
        Intent intent = wingsNotification.getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        // Construct notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder.setSmallIcon(R.drawable.notification)
                .setContentTitle(wingsNotification.getTitle()).setContentText(wingsNotification.getMessage())
                .setTicker(wingsNotification.getTicker()).setAutoCancel(true).setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent).build();

        // Send notification.
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(wingsNotification.getId(), notification);
        }
    }

    //
    // Package private methods.
    //

    /**
     * Schedules an alarm to start the {@link WingsService}.
     * 
     * @param context
     *            the {@link Context}.
     * @param delay
     *            how far in the future to schedule the alarm.
     */
    static void scheduleWingsService(Context context, long delay) {
        Context appContext = context.getApplicationContext();

        // Create pending intent.
        Intent intent = new Intent(appContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Set alarm.
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, delay, pendingIntent);
    }

    //
    // Public methods.
    //

    /**
     * Starts this {@link IntentService}, ensuring the device does not sleep before the service is started or during
     * {@link #onHandleIntent(Intent)}.
     * 
     * @param context
     *            the {@link Context}.
     */
    public static void startWakefulService(Context context) {
        acquireWakeLock(context);
        context.startService(new Intent(context, WingsService.class));
    }
}
