/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import java.io.FileInputStream;
import java.io.IOException;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nasa.ecg.views.animation.ECGCurveDataSource;
import com.nasa.ecg.views.animation.ECGCurveDataSource.CurveName;
import com.nasa.ecg.views.animation.ECGLayout;

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
public class Play12LeadsActivity extends ParentActivity {

    /** The curves. */
    private ECGLayout[] curves;

    private ECGCurveDataSource dataSource;

    /**
     * Called when the activity gets created.
     * 
     * @param savedInstanceState
     *            the saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_12_leads);

        try {
            dataSource = new ECGCurveDataSource(new FileInputStream(Environment.getExternalStorageDirectory().getPath()
                    + "/after.bt12"));
        } catch (IOException e) {
            Log.e(ParentActivity.LOG_TAG, e.toString(), e);
            return;
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ECGLayout curve1 = (ECGLayout) findViewById(R.id.ecgCurve1);
            curve1.setDataSource(dataSource, new CurveName[] { CurveName.V1, CurveName.V2, CurveName.V3, CurveName.V1, CurveName.V2, CurveName.V3 });

            ECGLayout curve2 = (ECGLayout) findViewById(R.id.ecgCurve2);
            curve2.setDataSource(dataSource, new CurveName[] { CurveName.V1, CurveName.V2, CurveName.V3, CurveName.V1, CurveName.V2, CurveName.V3 });

        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ECGLayout curve1 = (ECGLayout) findViewById(R.id.ecgCurve1);
            curve1.setDataSource(dataSource, new CurveName[] { CurveName.V1, CurveName.V2, CurveName.V3 });

            ECGLayout curve2 = (ECGLayout) findViewById(R.id.ecgCurve2);
            curve2.setDataSource(dataSource, new CurveName[] { CurveName.V1, CurveName.V2, CurveName.V3 });

            ECGLayout curve3 = (ECGLayout) findViewById(R.id.ecgCurve3);
            curve3.setDataSource(dataSource, new CurveName[] { CurveName.V1, CurveName.V2, CurveName.V3 });

            ECGLayout curve4 = (ECGLayout) findViewById(R.id.ecgCurve4);
            curve4.setDataSource(dataSource, new CurveName[] { CurveName.V1, CurveName.V2, CurveName.V3 });
        }

        dataSource.start();

        // set the maximum value for the seek bar.
        SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax(100);
        //        ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
        //        for (int i = 0; i < curves.length; i++) {
        //            curves[i].setControls(seekbar, playButton);
        //            curves[i].setCounters((TextView) findViewById(R.id.countUp), upFormatter,
        //                    (TextView) findViewById(R.id.countDown), downFormatter);
        //        }
        //
        //        // listen to the seek bar change events.
        //        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        //
        //            public void onStopTrackingTouch(SeekBar seekBar) {
        //                for (int i = 0; i < curves.length; i++) {
        //                    curves[i].stopTrackingTouch();
        //                }
        //            }
        //
        //            public void onStartTrackingTouch(SeekBar seekBar) {
        //                for (int i = 0; i < curves.length; i++) {
        //                    curves[i].startTrackingTouch();
        //                }
        //            }
        //
        //            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //                if (fromUser) {
        //                    for (int i = 0; i < curves.length; i++) {
        //                        curves[i].onProgressChanged();
        //                    }
        //                }
        //            }
        //        });

        // update the compression/amplification values.
//        updateCompressionValue();
//        updateAmplificationValue();
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
            //            curves[i].play();
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
            //            curves[i].fastForward();
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
            //            curves[i].rewind();
        }
    }
}