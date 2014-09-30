/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg.views;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nasa.ecg.R;
import com.nasa.ecg.views.ECGCurveAnimation.AnimationState;

/**
 * 
 * This is the most important view, which draws the ECG curve.
 * 
 * <p>
 * Changes in 1.1 (NASA ECG Viewer for Android Prototype Animation Assembly
 * version 1.0): 1) Added functionality for handling compression/decompression
 * and amplification/reduction. <br />
 * 2) Changed the way the data is gotten from reading the data file to get the
 * data from an external source. So, the view becomes independent of any data
 * retrieval or logic. 3) Added functionality for handling the play/pause, fast
 * forward, rewind animation.
 * </p>
 * 
 * @author mohamede1945
 * @version 1.1
 */
public class ECGLayout extends FrameLayout {

    /**
     * The Interface CounterFormatter.
     */
    public interface CounterFormatter {
        
        /**
         * Format.
         *
         * @param milliseconds the milliseconds
         * @return the string
         */
        public String format(int milliseconds);
    }

    /** The grid. */
    private ECGGrid grid;

    /** The curve. */
    private ECGCurve curve;

    /** The seek bar. */
    private SeekBar seekbar;

    /** The amplification factor. */
    private float amplificationFactor;

    /** The compression factor. */
    private float compressionFactor;

    /** The minimum compression. */
    private int minCompression;

    /** The minimum amplification. */
    private int minAmplification;

    /** The current compression. */
    private float currentCompression;

    /** The current amplification. */
    private float currentAmplification;

    /** The amplification factor. */
    private float amplificationFactorView;

    /** The compression factor. */
    private float compressionFactorView;

    /** The minimum compression. */
    private int minCompressionView;

    /** The minimum amplification. */
    private int minAmplificationView;

    /** The number of iterations. */
    private int numberOfIterations;

    /**
     * Instantiates a new eCG curve.
     * 
     * @param context
     *            the context
     */
    public ECGLayout(Context context) {
        super(context);
        init();
    }

    /**
     * Instantiates a new eCG curve.
     * 
     * @param context
     *            the context
     * @param attrs
     *            the attributes
     */
    public ECGLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Instantiates a new eCG curve.
     * 
     * @param context
     *            the context
     * @param attrs
     *            the attributes
     * @param defStyle
     *            the style
     */
    public ECGLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Initialize the layout object.
     */
    private void init() {
        Resources resources = getContext().getResources();
        int normalDuration = resources.getInteger(R.integer.normalPlayTime);
        int fastForwardDuration = resources.getInteger(R.integer.fastForwardPlayTime);
        int rewindDuration = resources.getInteger(R.integer.rewindPlayTime);
        compressionFactor = resources.getInteger(R.integer.compressionFactor) / 100f;
        amplificationFactor = resources.getInteger(R.integer.amplificationFactor) / 100f;
        minCompression = resources.getInteger(R.integer.minCompression);
        minAmplification = resources.getInteger(R.integer.minAmplification);
        compressionFactorView = resources.getInteger(R.integer.compressionFactorView) / 100f;
        amplificationFactorView = resources.getInteger(R.integer.amplificationFactorView) / 100f;
        minCompressionView = resources.getInteger(R.integer.minCompressionView);
        minAmplificationView = resources.getInteger(R.integer.minAmplificationView);
        numberOfIterations = resources.getInteger(R.integer.numberOfIterations);

        currentCompression = minCompression;
        currentAmplification = minAmplification;

        animation.setNormalTime(normalDuration);
        animation.setFastTime(fastForwardDuration);
        animation.setRewindTime(rewindDuration);
        animation.setNumberOfIterations(numberOfIterations);

        grid = new ECGGrid(getContext());
        curve = new ECGCurve(getContext());

        curve.setHorizontalZoomLevel(currentAmplification);
        curve.setVerticalZoomLevel(currentCompression);

        grid.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        curve.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        addView(grid);
        addView(curve);

        animation.setView(curve);
    }

    /** The animation. */
    private final ECGCurveAnimation animation = new ECGCurveAnimation();

    /**
     * Fast forward the animation.
     */
    public void fastForward() {
        animation.fastForward();
    }

    /**
     * Rewind the animation.
     */
    public void rewind() {
        animation.rewind();
    }

    /**
     * Play the animation.
     */
    public void play() {
        animation.play();
    }

    /**
     * Sets the view being a dynamic one or a static one.
     *
     * @param isStatic the new static
     */
    public void setStatic(boolean isStatic) {
        curve.setStatic(isStatic);
    }

    /**
     * Amplify the curve.
     */
    public void amplify() {
        currentAmplification *= amplificationFactor;
        curve.setHorizontalZoomLevel(currentAmplification);

        restart();
        animation.resetAnimation();
    }

    /**
     * Reduce the curve.
     */
    public void reduce() {
        if (currentAmplification <= minAmplification) {
            currentAmplification = minAmplification;
            return;
        }
        currentAmplification /= amplificationFactor;
        curve.setHorizontalZoomLevel(currentAmplification);

        restart();
        animation.resetAnimation();
    }

    /**
     * Decompress the curve.
     */
    public void decompress() {
        currentCompression *= compressionFactor;
        curve.setVerticalZoomLevel(currentCompression);

        restart();
        animation.resetAnimation();
    }

    /**
     * Restart.
     */
    private void restart() {
        if (getProgress() != 100 && animation.getState() == AnimationState.Dragged
                || animation.getState() == AnimationState.Paused) {
            animation.setPosition(0);
        }
    }

    /**
     * Compress the curve.
     */
    public void compress() {
        if (currentCompression <= minCompression) {
            currentCompression = minCompression;
            return;
        }
        currentCompression /= compressionFactor;
        curve.setVerticalZoomLevel(currentCompression);

        restart();
        animation.resetAnimation();
    }

    /**
     * Calc root.
     *
     * @param c the c
     * @param m the m
     * @param f the f
     * @return the int
     */
    public int calcRoot(float c, float m, float f) {
        int count = 0;
        while (c > m) {
            c /= f;
            count++;
        }
        return count;
    }

    /**
     * Gets the current compression.
     * 
     * @return the current compression
     */
    public int getCurrentCompression() {
        return (int) (minCompressionView * Math.pow(compressionFactorView,
                calcRoot(currentCompression, minCompression, compressionFactor)));
    }

    /**
     * Gets the current amplification.
     * 
     * @return the current amplification
     */
    public int getCurrentAmplification() {
        return (int) (minAmplificationView * Math.pow(amplificationFactorView,
                calcRoot(currentAmplification, minAmplification, amplificationFactor)));
    }

    /**
     * Sets the all points.
     * 
     * @param points
     *            the new all points
     */
    public void setAllPoints(List<Float[]> points) {
        curve.setAllPoints(points);
    }

    /**
     * Sets the mid points.
     * 
     * @param midPoints
     *            the new mid points
     */
    public void setMidPoints(float[] midPoints) {
        curve.setMidPoints(midPoints);
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
     * Stop tracking touch.
     */
    public void stopTrackingTouch() {
        animation.setPosition(seekbar.getProgress() / 100.0f);
    }

    /**
     * Start tracking touch.
     */
    public void startTrackingTouch() {
        animation.setPosition(seekbar.getProgress() / 100.0f);
    }

    /**
     * On progress changed.
     */
    public void onProgressChanged() {
        animation.setPosition(seekbar.getProgress() / 100.0f);
    }

    /**
     * Sets the number of iterations.
     *
     * @param num the new number of iterations
     */
    public void setNumberOfIterations(int num) {
        numberOfIterations = num;
        animation.setNumberOfIterations(num);
    }

    /**
     * Sets the controls the seek bar and play button.
     * 
     * @param seekbar
     *            the seek bar
     * @param playButton
     *            the play button
     */
    public void setControls(SeekBar seekbar, ImageButton playButton) {
        this.seekbar = seekbar;
        animation.setSeekbar(seekbar);
        animation.setPlayButton(playButton);
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
        animation.setCounters(countUp, upFormatter, countDown, downFormatter);
    }

    /**
     * Gets the progress.
     *
     * @return the progress
     */
    public int getProgress() {
        return (int) (animation.getPercentage() * 100);
    }

    /**
     * Sets the animation listener.
     *
     * @param listener the new animation listener
     */
    public void setAnimationListener(AnimationListener listener) {
        animation.setAnimationListener(listener);
    }

    /**
     * Sets the progress.
     *
     * @param progress the new progress
     */
    public void setProgress(int progress) {
        curve.setPercentage(progress / 100.0f);
        animation.setPosition(progress / 100.0f);
    }
}
