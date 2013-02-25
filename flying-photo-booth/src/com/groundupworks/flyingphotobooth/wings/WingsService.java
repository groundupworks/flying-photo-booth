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
package com.groundupworks.flyingphotobooth.wings;

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
import com.groundupworks.flyingphotobooth.R;
import com.groundupworks.flyingphotobooth.dropbox.DropboxHelper;
import com.groundupworks.flyingphotobooth.facebook.FacebookHelper;

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
        return (START_REDELIVER_INTENT);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Context appContext = getApplicationContext();

            // Process share requests to Facebook.
            FacebookHelper facebookHelper = new FacebookHelper();
            if (facebookHelper.isLinked(appContext)) {
                int shared = facebookHelper.processShareRequests(appContext);
                if (shared > 0) {
                    sendFacebookNotification(appContext, facebookHelper, shared);
                }
            }

            // Process share requests to Dropbox.
            DropboxHelper dropboxHelper = new DropboxHelper();
            if (dropboxHelper.isLinked(appContext)) {
                int shared = dropboxHelper.processShareRequests(appContext);
                if (shared > 0) {
                    sendDropboxNotification(appContext, dropboxHelper, shared);
                }
            }

            // Purge share requests.
            if (WingsDbHelper.getInstance(appContext).purge() > 0) {
                // Some share requests failed. Schedule next attempt to share.
                scheduleRetry();
            } else {
                // All share requests completed successfully. Reset retry policy.
                RetryPolicy.reset(this);
            }
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

            // One release is sufficient to release this lock.
            sWakeLock.setReferenceCounted(false);
        }

        // Acquire lock.
        if (!sWakeLock.isHeld()) {
            sWakeLock.acquire();
        }
    }

    /**
     * Releases the wake lock if one is held.
     */
    private synchronized static void releaseWakeLock() {
        if (sWakeLock != null && sWakeLock.isHeld()) {
            sWakeLock.release();
            sWakeLock = null;
        }
    }

    /**
     * Schedules a retry in the future. This method figures out how far in the future the next attempt should be.
     */
    private void scheduleRetry() {
        Context appContext = getApplicationContext();
        long nextRetry = System.currentTimeMillis() + RetryPolicy.incrementAndGetTime(appContext);

        // Create pending intent.
        Intent intent = new Intent(appContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Set alarm.
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextRetry, pendingIntent);
    }

    /**
     * Sends a notification of the Facebook share results to the notification bar.
     * 
     * @param context
     *            the {@link Context}.
     * @param helper
     *            the {@link FacebookHelper}.
     * @param shared
     *            the number of successful shares. Must be larger than 0.
     */
    private void sendFacebookNotification(Context context, FacebookHelper helper, int shared) {
        // Construct notification title and message text.
        String title = getString(R.string.facebook__notification_shared_title);
        String albumName = helper.getLinkedAlbumName(context);
        String msg;
        if (shared > 1) {
            msg = getString(R.string.facebook__notification_shared_msg_multi, shared, albumName);
        } else {
            msg = getString(R.string.facebook__notification_shared_msg_single, albumName);
        }
        String ticker = getString(R.string.facebook__notification_shared_ticker);

        // Construct blank pending intent as some versions of Android require it.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);

        // Construct notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder.setSmallIcon(R.drawable.notification).setContentTitle(title)
                .setContentText(msg).setTicker(ticker).setAutoCancel(true).setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent).build();

        // Send notification.
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(ShareRequest.DESTINATION_FACEBOOK, notification);
        }
    }

    /**
     * Sends a notification of the Dropbox share results to the notification bar.
     * 
     * @param context
     *            the {@link Context}.
     * @param helper
     *            the {@link DropboxHelper}.
     * @param shared
     *            the number of successful shares. Must be larger than 0.
     */
    private void sendDropboxNotification(Context context, DropboxHelper helper, int shared) {
        // Construct notification title and message text.
        String title = getString(R.string.dropbox__notification_shared_title);
        String shareUrl = helper.getLinkedShareUrl(context);
        String msg;
        if (shared > 1) {
            msg = getString(R.string.dropbox__notification_shared_msg_multi, shared, shareUrl);
        } else {
            msg = getString(R.string.dropbox__notification_shared_msg_single, shareUrl);
        }
        String ticker = getString(R.string.dropbox__notification_shared_ticker);

        // Construct blank pending intent as some versions of Android require it.
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);

        // Construct notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder.setSmallIcon(R.drawable.notification).setContentTitle(title)
                .setContentText(msg).setTicker(ticker).setAutoCancel(true).setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent).build();

        // Send notification.
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(ShareRequest.DESTINATION_DROPBOX, notification);
        }
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
