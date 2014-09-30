/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg.views;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nasa.ecg.ParentActivity;
import com.nasa.ecg.R;
import com.nasa.ecg.views.ECGCurve.OnSizeChangedListener;
import com.nasa.ecg.views.ECGLayout.CounterFormatter;

/**
 * 
 * This is the most important class for (NASA ECG Viewer for Android Prototype
 * Animation Assembly version 1.0). This class is the animation class of the ECG
 * curve. It performs animation for (normal playing, fast forward, rewind,
 * pausing, and dragging).
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class ECGCurveAnimation extends Animation {

    /**
     * The AnimationState enumerator.
     */
    public static enum AnimationState {

        /** The Not started. */
        NotStarted,
        /** The Playing. */
        Playing,
        /** The Paused. */
        Paused,
        /** The Fast forward. */
        FastForward,
        /** The Rewind. */
        Rewind,
        /** The Finished. */
        Finished,
        /** The Dragged. */
        Dragged
    }

    /** The state before finish state. */
    private AnimationState beforeFinish;

    /** The state. */
    private AnimationState state;

    /** The normal time. */
    private int normalTime;

    /** The fast time. */
    private int fastTime;

    /** The rewind time. */
    private int rewindTime;

    /** The rewind empty width. */
    private int rewindEmptyWidth = 0;

    /** The completed percentage. */
    private float completedPercentage;

    /** The elapsed at pause. */
    private long elapsedAtPause;

    /** The paused. */
    private boolean paused = false;

    /** The curve. */
    private ECGCurve curve;

    /** The seek bar. */
    private SeekBar seekbar;

    /** The count up. */
    private TextView countUp;

    /** The count down. */
    private TextView countDown;

    /** The up formatter. */
    private CounterFormatter upFormatter;

    /** The down formatter. */
    private CounterFormatter downFormatter;

    /** The play button. */
    private ImageButton playButton;

    /** The reverse. */
    private boolean reverse = false;

    /** The current number of iterations. */
    private int currentNumberOfIterations = 0;

    /** The number of iterations. */
    private int numberOfIterations;

    /** The listener. */
    private AnimationListener listener;

    /**
     * Instantiates a new ECG curve animation instance.
     */
    public ECGCurveAnimation() {
        state = AnimationState.NotStarted;
        setInterpolator(new LinearInterpolator());

        super.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {
                if (listener != null) {
                    listener.onAnimationStart(animation);
                }
            }

            public void onAnimationRepeat(Animation animation) {
                if (listener != null) {
                    listener.onAnimationRepeat(animation);
                }
            }

            public void onAnimationEnd(Animation animation) {
                beforeFinish = state;
                state = AnimationState.Finished;
                setPlayButtonImage();

                if (reverse) {
                    if (currentNumberOfIterations > 0) {
                        currentNumberOfIterations--;
                    } else {
                        if (listener != null) {
                            listener.onAnimationEnd(animation);
                        }
                        return;
                    }
                } else {
                    if (currentNumberOfIterations < numberOfIterations - 1) {
                        currentNumberOfIterations++;
                    } else {
                        if (listener != null) {
                            listener.onAnimationEnd(animation);
                        }
                        return;
                    }
                }

                switch (beforeFinish) {
                case Playing:
                    setPosition(0);
                    play();
                    break;
                case FastForward:
                    setPosition(0);
                    fastForward();
                    break;
                case Rewind:
                    setPosition(1);
                    rewind();
                    break;
                }
            }
        });
    }

    /**
     * Sets the animation listener.
     * @see android.view.animation.Animation#setAnimationListener(android.view.animation.Animation.AnimationListener)
     */
    @Override
    public void setAnimationListener(AnimationListener listener) {
        this.listener = listener;
    }

    /**
     * This method applies the transformation.
     *
     * @param interpolatedTime the interpolated time.
     * @param t the transformation to apply.
     * @see android.view.animation.Animation#applyTransformation(float,
     * android.view.animation.Transformation)
     */
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        completedPercentage = interpolatedTime;

        final float time;

        if (isReverse()) {
            time = 1 - interpolatedTime;
            curve.setAnimationWidth((int) ((curve.getWidth() - rewindEmptyWidth) * time));
        } else {
            time = interpolatedTime;
            curve.setAnimationWidth((int) ((curve.getWidth() - curve.getFixedAnimationWidth()) * time));
        }

        float percentage = getPercentage();
        if (seekbar != null) {
            seekbar.setProgress((int) (percentage * 100));
        }
        int passedTime = (int) (normalTime * percentage);
        setTimers(passedTime);
    }

    /**
     * Sets the timers.
     *
     * @param passedTime the new timers
     */
    private void setTimers(int passedTime) {
        if (countUp != null) {
            countUp.setText(upFormatter.format(passedTime));
        }
        if (countDown != null) {
            countDown.setText(downFormatter.format(normalTime - passedTime));
        }
    }

    /**
     * This method returns the transformation.
     *
     * @param currentTime the current time.
     * @param outTransformation the output transformation.
     * @return the transformation
     * @see android.view.animation.Animation#getTransformation(long,
     * android.view.animation.Transformation)
     */
    @Override
    public boolean getTransformation(long currentTime, Transformation outTransformation) {
        if (paused && elapsedAtPause == 0) {
            elapsedAtPause = currentTime - getStartTime();
        }
        if (paused) {
            setStartTime(currentTime - elapsedAtPause);
        }
        return super.getTransformation(currentTime, outTransformation);
    }

    /**
     * The bounds will never changes because of our custom animation.
     *
     * @return true, if successful
     * @see android.view.animation.Animation#willChangeBounds()
     */
    @Override
    public boolean willChangeBounds() {
        return false;
    }

    /**
     * Sets the view.
     * 
     * @param view
     *            the new view
     */
    public void setView(ECGCurve view) {
        this.curve = view;
    }

    /**
     * Gets the view.
     * 
     * @return the view
     */
    public ECGCurve getView() {
        return curve;
    }

    /**
     * Checks if it is reverse.
     * 
     * @return true, if it is reverse
     */
    public boolean isReverse() {
        return reverse;
    }

    /**
     * Sets the reverse.
     * 
     * @param reverse
     *            the new reverse
     */
    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    /**
     * Apply rewind animation.
     */
    public void rewind() {
        switch (state) {
        case Finished:
            currentNumberOfIterations = numberOfIterations - 1;
            if (beforeFinish != AnimationState.Rewind) {
                startAnimation(rewindTime, true, 0, 0);
            } else {
                Log.d(ParentActivity.LOG_TAG, "The player won't rewind because it's at the start of the curve.");
                return;
            }
            break;
        case Paused:
            resume();
        case Dragged:
        case FastForward:
        case Playing:
            startAnimation((int) (rewindTime * getPercentage()), true, 0,
                    (int) ((1 - getPercentage()) * curve.getWidth()));
            break;
        case NotStarted:
        case Rewind:
            // do nothing.
            break;
        }

        state = AnimationState.Rewind;

        setPlayButtonImage();

        Log.d(ParentActivity.LOG_TAG, "The player starts rewinding.");
    }

    /**
     * Apply fast forward animation.
     */
    public void fastForward() {
        switch (state) {
        case Finished:
        case NotStarted:
            currentNumberOfIterations = 0;
            startAnimation(fastTime, false, 0, 0);
            break;
        case Paused:
            resume();
        case Dragged:
        case Playing:
        case Rewind:
            startAnimation((int) (fastTime * (1 - getPercentage())), false, (int) (curve.getWidth() * getPercentage()),
                    0);
            break;
        case FastForward:
            // do nothing
            break;
        }

        state = AnimationState.FastForward;
        setPlayButtonImage();

        Log.d(ParentActivity.LOG_TAG, "The player starts Fast Forwarding.");
    }

    /**
     * Apply play animation.
     */
    public void play() {
        switch (state) {
        case Finished:
        case NotStarted:
            currentNumberOfIterations = 0;
            startAnimation(normalTime, false, 0, 0);
            Log.d(ParentActivity.LOG_TAG, "The player starts Playing.");
            break;
        case Paused:
            resume();
            Log.d(ParentActivity.LOG_TAG, "The player resumes.");
            break;
        case Playing:
            pause();
            Log.d(ParentActivity.LOG_TAG, "The player is paused.");
            break;
        case Dragged:
        case FastForward:
        case Rewind:
            startAnimation((int) (normalTime * (1 - getPercentage())), false,
                    (int) (curve.getWidth() * getPercentage()), 0);
            Log.d(ParentActivity.LOG_TAG, "The player starts Playing.");
            break;
        }

        state = state == AnimationState.Playing ? AnimationState.Paused : AnimationState.Playing;
        setPlayButtonImage();
    }

    /**
     * Start animation.
     * 
     * @param duration
     *            the duration
     * @param reverse
     *            the reverse
     * @param fixedWidth
     *            the fixed width
     * @param rewindWidth
     *            the rewind width
     */
    private void startAnimation(int duration, boolean reverse, int fixedWidth, int rewindWidth) {
        setDuration(duration);
        setReverse(reverse);
        curve.setFixedAnimationWidth(fixedWidth);
        this.rewindEmptyWidth = rewindWidth;
        curve.startAnimation(this);
    }

    /**
     * Pause the animation.
     */
    private void pause() {
        elapsedAtPause = 0;
        paused = true;
    }

    /**
     * Resume the animation.
     */
    private void resume() {
        paused = false;
    }

    /**
     * Gets the normal time.
     * 
     * @return the normalTime
     */
    public int getNormalTime() {
        return normalTime;
    }

    /**
     * Sets the normal time.
     * 
     * @param normalTime
     *            the normalTime to set
     */
    public void setNormalTime(int normalTime) {
        this.normalTime = normalTime;
    }

    /**
     * Gets the fast time.
     * 
     * @return the fastTime
     */
    public int getFastTime() {
        return fastTime;
    }

    /**
     * Sets the fast time.
     * 
     * @param fastTime
     *            the fastTime to set
     */
    public void setFastTime(int fastTime) {
        this.fastTime = fastTime;
    }

    /**
     * Gets the rewind time.
     * 
     * @return the rewindTime
     */
    public int getRewindTime() {
        return rewindTime;
    }

    /**
     * Sets the rewind time.
     * 
     * @param rewindTime
     *            the rewindTime to set
     */
    public void setRewindTime(int rewindTime) {
        this.rewindTime = rewindTime;
    }

    /**
     * Gets the percentage.
     * 
     * @return the percentage
     */
    public float getPercentage() {

        final float width;
        switch (state) {
        case Rewind:
            width = curve.getWidth() - (rewindEmptyWidth + (curve.getWidth() - rewindEmptyWidth) * completedPercentage);
            break;
        case Dragged:
            width = curve.getFixedAnimationWidth();
            break;
        case Finished:
            if (beforeFinish == AnimationState.Rewind) {
                width = 0;
            } else {
                width = curve.getWidth();
            }
            break;
        case NotStarted:
            width = 0;
            break;
        case FastForward:
        case Paused:
        case Playing:
        default:
            width = curve.getFixedAnimationWidth() + (curve.getWidth() - curve.getFixedAnimationWidth())
                    * completedPercentage;
            break;
        }

        return width / curve.getWidth();
    }

    /**
     * Sets the position.
     * 
     * @param percentage
     *            the new position
     */
    public void setPosition(final float percentage) {
        cancel();

        if (curve.getWidth() == 0) {
            curve.setOnSizeChangedListener(new OnSizeChangedListener() {

                public void sizeChanged(View v) {
                    setPosition(percentage);
                    curve.setOnSizeChangedListener(null);
                }
            });
        }

        curve.setFixedAnimationWidth(percentage * curve.getWidth());
        curve.setAnimationWidth(0);
        rewindEmptyWidth = 0;
        completedPercentage = 0;
        state = AnimationState.Dragged;
        paused = false;
        setReverse(false);
        curve.invalidate();
        setTimers((int) (normalTime * percentage));
        if (seekbar != null) {
            seekbar.setProgress((int) (percentage * 100));
        }
        Log.d(ParentActivity.LOG_TAG, "The player is dragged to position:" + percentage);
    }

    /**
     * Gets the seek bar.
     * 
     * @return the seek bar
     */
    public SeekBar getSeekbar() {
        return seekbar;
    }

    /**
     * Sets the seek bar.
     * 
     * @param seekbar
     *            the seek bar to set
     */
    public void setSeekbar(SeekBar seekbar) {
        this.seekbar = seekbar;
    }

    /**
     * Sets the counters.
     *
     * @param countUp the count up
     * @param upFormatter the up formatter
     * @param countDown the count down
     * @param downFormatter the down formatter
     */
    public void setCounters(TextView countUp, CounterFormatter upFormatter, TextView countDown,
            CounterFormatter downFormatter) {
        this.countUp = countUp;
        this.countDown = countDown;
        this.upFormatter = upFormatter;
        this.downFormatter = downFormatter;
        setTimers(0);
    }

    /**
     * Gets the play button.
     * 
     * @return the play button
     */
    public ImageButton getPlayButton() {
        return playButton;
    }

    /**
     * Sets the play button.
     * 
     * @param playButton
     *            the new play button
     */
    public void setPlayButton(ImageButton playButton) {
        this.playButton = playButton;
        setPlayButtonImage();
    }

    /**
     * Sets the play button image.
     */
    private void setPlayButtonImage() {
        if (playButton == null) {
            return;
        }
        switch (state) {
        case Dragged:
        case FastForward:
        case Paused:
        case Rewind:
        case Finished:
        case NotStarted:
            playButton.setImageResource(R.drawable.play);
            break;
        case Playing:
            playButton.setImageResource(R.drawable.pause);
            break;
        }
    }

    /**
     * Reset the animation.
     */
    public void resetAnimation() {
        switch (state) {
        case FastForward:
            startAnimation(fastTime, false, 0, 0);
            Log.d(ParentActivity.LOG_TAG, "The player starts Fast forwarding from the beginning.");
            break;
        case Playing:
            startAnimation(normalTime, false, 0, 0);
            Log.d(ParentActivity.LOG_TAG, "The player starts Playing from the beginning.");
            break;
        case Rewind:
            startAnimation(rewindTime, true, 0, 0);
            Log.d(ParentActivity.LOG_TAG, "The player starts Rewinding from the beginning.");
            break;
        }
    }

    /**
     * Sets the number of iterations.
     *
     * @param numberOfIterations the new number of iterations
     */
    public void setNumberOfIterations(int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }
    
    /**
     * Gets the state.
     *
     * @return the state
     */
    public AnimationState getState() {
        return state;
    }
}