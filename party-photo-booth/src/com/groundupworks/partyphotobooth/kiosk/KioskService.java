/*
 * This file is part of Flying PhotoBooth.
 * 
 * Flying PhotoBooth is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Flying PhotoBooth is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Flying PhotoBooth.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.groundupworks.partyphotobooth.kiosk;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.groundupworks.partyphotobooth.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * {@link Service} that launches the {@link KioskActivity} and keeps it in the foreground.
 *
 * @author Benedict Lau
 */
public class KioskService extends Service {

    /**
     * The id for the foreground service notification.
     */
    private static final int NOTIFICATION_ID = 10881;

    /**
     * The period to schedule a {@link KioskActivity} launch {@link Intent}.
     */
    private static final int KIOSK_ACTIVITY_RELAUNCH_PERIOD = 2500;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Context appContext = getApplicationContext();

        // Construct pending intent. The wrapped Intent must not be null as some versions of Android require it.
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        // Construct notification for foreground service indication.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext);
        Notification notification = builder.setSmallIcon(com.groundupworks.lib.photobooth.R.drawable.wings__notification)
                .setContentTitle(appContext.getString(R.string.kiosk_mode__notification_title))
                .setContentText(appContext.getString(R.string.kiosk_mode__notification_msg))
                .setTicker(appContext.getString(R.string.kiosk_mode__start_msg)).setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent).build();

        // Make foreground service.
        startForeground(NOTIFICATION_ID, notification);

        // Launch KioskActivity.
        startKioskLauncher(appContext);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    //
    // Private methods.
    //

    /**
     * Starts a timer to launch and relaunch the {@link KioskActivity} in order to keep it in foreground until Kiosk
     * mode is disabled.
     *
     * @param context the {@link Context}.
     */
    private void startKioskLauncher(final Context context) {
        final KioskModeHelper kioskModeHelper = new KioskModeHelper(context);
        final Intent intent = new Intent(context, KioskActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Start timer to launch and relaunch the KioskActivity.
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (kioskModeHelper.isEnabled()) {
                    if (!KioskActivity.sIsInForeground) {
                        // Send Intent to start KioskActivity.
                        startActivity(intent);
                    }
                } else {
                    // Stop the timer and the KioskService.
                    timer.cancel();
                    stopSelf();
                }
            }
        }, 0, KIOSK_ACTIVITY_RELAUNCH_PERIOD);
    }
}
