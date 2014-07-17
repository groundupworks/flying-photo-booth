package com.groundupworks.lib.photobooth.helpers;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CameraAudioHelper {

    private final WeakReference<Context> mReference;

    private final int mBeepId;

    private final Handler mHandler;

    private volatile MediaPlayer mBeepPlayer;

    private final ReentrantReadWriteLock mLock;

    public CameraAudioHelper(final Context context, final int beepId, Handler handler) {
        mReference = new WeakReference<Context>(context);
        mBeepId = beepId;
        mHandler = handler;
        mLock = new ReentrantReadWriteLock();
    }

    public void prepare() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final Context context = mReference.get();
                if (context == null) {
                    return;
                }
                mBeepPlayer = MediaPlayer.create(context.getApplicationContext(), mBeepId);
            }
        });
    }

    public void release() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final MediaPlayer beepPlayer = mBeepPlayer;
                if (beepPlayer == null) {
                    return;
                }

                final ReentrantReadWriteLock.WriteLock writeLock = mLock.writeLock();
                writeLock.lock();
                try {
                    beepPlayer.stop();
                    beepPlayer.release();
                    mBeepPlayer = null;
                } finally {
                    writeLock.unlock();
                }
            }
        });
    }

    public void beep() {
        final MediaPlayer beepPlayer = mBeepPlayer;
        if (beepPlayer == null) {
            return;
        }

        final ReentrantReadWriteLock.ReadLock readLock = mLock.readLock();
        readLock.lock();
        try {
            if (beepPlayer.isPlaying()) {
                beepPlayer.pause();
            }

            beepPlayer.seekTo(0);
            beepPlayer.start();
        } finally {
            readLock.unlock();
        }
    }
}
