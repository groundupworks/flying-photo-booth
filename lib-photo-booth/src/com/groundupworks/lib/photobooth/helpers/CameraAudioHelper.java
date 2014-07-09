package com.groundupworks.lib.photobooth.helpers;

import android.content.Context;
import android.media.MediaPlayer;

import java.lang.ref.WeakReference;

public class CameraAudioHelper {

    private final WeakReference<Context> mReference;

    private final int mBeepId;

    private transient MediaPlayer mBeepPlayer;

    public CameraAudioHelper(final Context context, final int beepId) {
        mReference = new WeakReference<Context>(context);
        mBeepId = beepId;
    }

    public synchronized void prepare() {
        final Context context = mReference.get();
        if (context == null) {
            return;
        }

        mBeepPlayer = MediaPlayer.create(context.getApplicationContext(), mBeepId);
    }

    public synchronized void release() {
        final MediaPlayer beepPlayer = mBeepPlayer;
        if (beepPlayer != null) {
            beepPlayer.stop();
            beepPlayer.release();
        }
        mBeepPlayer = null;
    }

    public synchronized void beep() {
        final MediaPlayer beepPlayer = mBeepPlayer;
        if (beepPlayer != null) {
            if (beepPlayer.isPlaying()) {
                beepPlayer.pause();
            }

            beepPlayer.seekTo(0);
            beepPlayer.start();
        }
    }
}
