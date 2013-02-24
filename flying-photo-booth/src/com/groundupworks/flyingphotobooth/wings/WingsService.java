package com.groundupworks.flyingphotobooth.wings;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import com.groundupworks.flyingphotobooth.R;
import com.groundupworks.flyingphotobooth.dropbox.DropboxHelper;
import com.groundupworks.flyingphotobooth.facebook.FacebookHelper;

public class WingsService extends IntentService {

    private static final String NAME = "com.groundupworks.flyingphotobooth.wings.WingsService";

    private static volatile PowerManager.WakeLock sWakeLock = null;

    /**
     * Constructor.
     */
    public WingsService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
        WingsDbHelper.getInstance(appContext).purge();
    }

    //
    // Private methods.
    //

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

        // Construct notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder.setSmallIcon(R.drawable.notification).setContentTitle(title)
                .setContentText(msg).setTicker(ticker).setAutoCancel(true).setWhen(System.currentTimeMillis()).build();

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

        // Construct notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder.setSmallIcon(R.drawable.notification).setContentTitle(title)
                .setContentText(msg).setTicker(ticker).setAutoCancel(true).setWhen(System.currentTimeMillis()).build();

        // Send notification.
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(ShareRequest.DESTINATION_DROPBOX, notification);
        }
    }
}
