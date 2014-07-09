/*
 * Copyright (C) 2013 Benedict Lau
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
package com.groundupworks.lib.photobooth.views;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.view.View;

/**
 * Provides a callback when a non-looping {@link AnimationDrawable} completes its animation sequence. More precisely,
 * {@link #onAnimationComplete()} is triggered when {@link View#invalidateDrawable(Drawable)} has been called on the
 * last frame.
 *
 * @author Benedict Lau
 */
public abstract class AnimationDrawableCallback implements Callback {

    private final int mFrameCount;

    private int mCurrentFrame;
    /**
     * The last frame of {@link Drawable} in the {@link AnimationDrawable}.
     */
    private Drawable mLastFrame;

    /**
     * The client's {@link Callback} implementation. All calls are proxied to this wrapped {@link Callback}
     * implementation after intercepting the events we need.
     */
    private Callback mWrappedCallback;

    /**
     * Flag to ensure that {@link #onAnimationComplete()} is called only once, since
     * {@link #invalidateDrawable(Drawable)} may be called multiple times.
     */
    private boolean mIsCallbackTriggered = false;

    /**
     * Constructor.
     *
     * @param animationDrawable the {@link AnimationDrawable}.
     * @param callback          the client's {@link Callback} implementation. This is usually the {@link View} the has the
     *                          {@link AnimationDrawable} as background.
     */
    public AnimationDrawableCallback(AnimationDrawable animationDrawable, Callback callback) {
        mFrameCount = animationDrawable.getNumberOfFrames();
        mLastFrame = animationDrawable.getFrame(mFrameCount - 1);
        mWrappedCallback = callback;
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        if (mWrappedCallback != null) {
            mWrappedCallback.invalidateDrawable(who);
        }

        if (!mIsCallbackTriggered && mLastFrame != null && mLastFrame.equals(who.getCurrent())) {
            mIsCallbackTriggered = true;
            onAnimationComplete();
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (mWrappedCallback != null) {
            mWrappedCallback.scheduleDrawable(who, what, when);
        }

        onAnimationAdvanced(mCurrentFrame, mFrameCount);
        mCurrentFrame++;
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (mWrappedCallback != null) {
            mWrappedCallback.unscheduleDrawable(who, what);
        }
    }

    //
    // Public methods.
    //

    /**
     * Callback triggered when {@link View#invalidateDrawable(Drawable)} has been called on the last frame, which marks
     * the end of a non-looping animation sequence.
     */
    public abstract void onAnimationComplete();

    public abstract void onAnimationAdvanced(int currentFrame, int totalFrame);
}
