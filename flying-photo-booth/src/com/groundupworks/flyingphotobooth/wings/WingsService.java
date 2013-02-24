package com.groundupworks.flyingphotobooth.wings;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
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
            facebookHelper.processShareRequests(appContext);
        }

        // Process share requests to Dropbox.
        DropboxHelper dropboxHelper = new DropboxHelper();
        if (dropboxHelper.isLinked(appContext)) {
            dropboxHelper.processShareRequests(appContext);
        }

        // Purge share requests.
        WingsDbHelper.getInstance(appContext).purge();
    }
}
