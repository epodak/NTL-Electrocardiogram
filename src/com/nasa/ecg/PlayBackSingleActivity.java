/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.nasa.ecg.views.ECGLayout;

/**
 * 
 * This is the play back activity for a single curve.
 * 
 * <p>
 * Changes in 1.1 (NASA ECG Viewer for Android Prototype Animation Assembly
 * version 1.0): 1) Added methods for handling user clicks on
 * compression/decompression and amplification/reduction. <br />
 * 2) Added methods for handling user clicks on play/pause, fast forward and
 * rewind.
 * </p>
 * 
 * @author mohamede1945
 * @version 1.1
 */
public class PlayBackSingleActivity extends ParentActivity {

    /** The curve. */
    private ECGLayout curve;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (curve != null) {
            outState.putInt(PROGRESS_STATE, curve.getProgress());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int progress = savedInstanceState.getInt(PROGRESS_STATE);
        curve.setProgress(progress);
    }

    /**
     * Called when the activity gets created.
     * 
     * @param savedInstanceState
     *            the saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_back_single);

        curve = (ECGLayout) findViewById(R.id.ecgCurve);
        curve.setAllPoints(getAllECGPoints(1));
        curve.setStatic(false);

        // set the maximum value for the seek bar.
        SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax(100);

        curve.setControls(seekbar, (ImageButton) findViewById(R.id.playButton));
        curve.setCounters((TextView) findViewById(R.id.countUp), upFormatter, (TextView) findViewById(R.id.countDown),
                downFormatter);

        // start listening to the seek bar change events.
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {
                curve.stopTrackingTouch();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                curve.startTrackingTouch();
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    curve.onProgressChanged();
                }
            }
        });

        // update the compression/amplification value.
        updateCompressionValue();
        updateAmplificationValue();
    }

    /**
     * Update compression value.
     */
    private void updateCompressionValue() {
        ((TextView) findViewById(R.id.zoomLeftRightText)).setText(String.valueOf(curve.getCurrentCompression()));
    }

    /**
     * Update amplification value.
     */
    private void updateAmplificationValue() {
        ((TextView) findViewById(R.id.zoomUpDownText)).setText(String.valueOf(curve.getCurrentAmplification()));
    }

    /**
     * Compress clicked.
     * 
     * @param view
     *            the clicked view
     */
    public void compressClicked(View view) {
        Log.d(ParentActivity.LOG_TAG, "compress clicked.");
        curve.compress();
        updateCompressionValue();
    }

    /**
     * Decompress clicked.
     * 
     * @param view
     *            the clicked view
     */
    public void decompressClicked(View view) {
        Log.d(ParentActivity.LOG_TAG, "decompress clicked.");
        curve.decompress();
        updateCompressionValue();
    }

    /**
     * Amplify clicked.
     * 
     * @param view
     *            the clicked view
     */
    public void amplifyClicked(View view) {
        Log.d(ParentActivity.LOG_TAG, "amplify clicked.");
        curve.amplify();
        updateAmplificationValue();
    }

    /**
     * Reduce clicked.
     * 
     * @param view
     *            the clicked view
     */
    public void reduceClicked(View view) {
        Log.d(ParentActivity.LOG_TAG, "reduce clicked.");
        curve.reduce();
        updateAmplificationValue();
    }

    /**
     * Play clicked.
     * 
     * @param view
     *            the clicked view
     */
    public void playClicked(View view) {
        Log.d(ParentActivity.LOG_TAG, "play clicked.");
        curve.play();
    }

    /**
     * Fast forward clicked.
     * 
     * @param view
     *            the clicked view
     */
    public void fastForwardClicked(View view) {
        Log.d(ParentActivity.LOG_TAG, "fast forward clicked.");
        curve.fastForward();
    }

    /**
     * Rewind clicked.
     * 
     * @param view
     *            the clicked view
     */
    public void rewindClicked(View view) {
        Log.d(ParentActivity.LOG_TAG, "rewind clicked.");
        curve.rewind();
    }
}