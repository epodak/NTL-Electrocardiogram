/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import android.content.res.Configuration;
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
 * This is the play back activity for a group of curves.
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
public class PlayBackGroupActivity extends ParentActivity {

    /** The curves. */
    private ECGLayout[] curves;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (curves.length > 0) {
            outState.putInt(PROGRESS_STATE, curves[0].getProgress());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int progress = savedInstanceState.getInt(PROGRESS_STATE);
        for (ECGLayout curve : curves) {
            curve.setProgress(progress);
        }
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
        setContentView(R.layout.play_back_group);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ECGLayout curve = (ECGLayout) findViewById(R.id.ecgCurve);
            curve.setAllPoints(getAllECGPoints(3));
            curve.setStatic(false);

            curves = new ECGLayout[] { curve };
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ECGLayout curve1 = (ECGLayout) findViewById(R.id.ecgCurve1);
            curve1.setAllPoints(getAllECGPoints(2));
            curve1.setStatic(false);

            ECGLayout curve2 = (ECGLayout) findViewById(R.id.ecgCurve2);
            curve2.setAllPoints(getAllECGPoints(1));
            curve2.setMidPoints(new float[] { 78 });
            curve2.setStatic(false);

            curves = new ECGLayout[] { curve1, curve2 };
        }

        // set the maximum value for the seek bar.
        SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax(100);
        ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
        for (int i = 0; i < curves.length; i++) {
            curves[i].setControls(seekbar, playButton);
            curves[i].setCounters((TextView) findViewById(R.id.countUp), upFormatter,
                    (TextView) findViewById(R.id.countDown), downFormatter);
        }

        // listen to the seek bar change events.
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {
                for (int i = 0; i < curves.length; i++) {
                    curves[i].stopTrackingTouch();
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                for (int i = 0; i < curves.length; i++) {
                    curves[i].startTrackingTouch();
                }
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    for (int i = 0; i < curves.length; i++) {
                        curves[i].onProgressChanged();
                    }
                }
            }
        });

        // update the compression/amplification values.
        updateCompressionValue();
        updateAmplificationValue();
    }

    /**
     * Updates the compression value.
     */
    private void updateCompressionValue() {
        ((TextView) findViewById(R.id.zoomLeftRightText)).setText(String.valueOf(curves[0].getCurrentCompression()));
    }

    /**
     * Updates the amplification value.
     */
    private void updateAmplificationValue() {
        ((TextView) findViewById(R.id.zoomUpDownText)).setText(String.valueOf(curves[0].getCurrentAmplification()));
    }

    /**
     * Compress clicked.
     * 
     * @param v
     *            the clicked view
     */
    public void compressClicked(View v) {
        Log.d(ParentActivity.LOG_TAG, "compress clicked.");
        for (int i = 0; i < curves.length; i++) {
            curves[i].compress();
        }
        updateCompressionValue();
    }

    /**
     * Decompress clicked.
     * 
     * @param v
     *            the clicked view
     */
    public void decompressClicked(View v) {
        Log.d(ParentActivity.LOG_TAG, "decompress clicked.");
        for (int i = 0; i < curves.length; i++) {
            curves[i].decompress();
        }
        updateCompressionValue();
    }

    /**
     * Amplify clicked.
     * 
     * @param v
     *            the clicked view
     */
    public void amplifyClicked(View v) {
        Log.d(ParentActivity.LOG_TAG, "amplify clicked.");
        for (int i = 0; i < curves.length; i++) {
            curves[i].amplify();
        }
        updateAmplificationValue();
    }

    /**
     * Reduce clicked.
     * 
     * @param v
     *            the clicked view
     */
    public void reduceClicked(View v) {
        Log.d(ParentActivity.LOG_TAG, "reduce clicked.");
        for (int i = 0; i < curves.length; i++) {
            curves[i].reduce();
        }
        updateAmplificationValue();
    }

    /**
     * Play clicked.
     * 
     * @param v
     *            the clicked view
     */
    public void playClicked(View v) {
        Log.d(ParentActivity.LOG_TAG, "play clicked.");
        for (int i = 0; i < curves.length; i++) {
            curves[i].play();
        }
    }

    /**
     * Fast forward clicked.
     * 
     * @param v
     *            the clicked view
     */
    public void fastForwardClicked(View v) {
        Log.d(ParentActivity.LOG_TAG, "fast forward clicked.");
        for (int i = 0; i < curves.length; i++) {
            curves[i].fastForward();
        }
    }

    /**
     * Rewind clicked.
     * 
     * @param v
     *            the clicked view
     */
    public void rewindClicked(View v) {
        Log.d(ParentActivity.LOG_TAG, "rewind clicked.");
        for (int i = 0; i < curves.length; i++) {
            curves[i].rewind();
        }
    }
}