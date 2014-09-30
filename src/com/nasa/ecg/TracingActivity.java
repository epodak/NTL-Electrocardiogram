/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;


import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.nasa.ecg.bluetooth.BluetoothActivity;
import com.nasa.ecg.bluetooth.BluetoothCommandService;
import com.nasa.ecg.views.animation.ECGCurveDataSource.CurveName;
import com.nasa.ecg.views.animation.ECGCurveDataSource;
import com.nasa.ecg.views.animation.ECGLayout;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * 
 * This is the stop tracing activity.
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class TracingActivity extends BluetoothActivity {

    private ECGCurveDataSource dataSource;

    private String fileName;
    private String code;
    private String bornDate;
    private String lastEcgDate;

    private void setText(int resId, String text) {
        ((TextView) findViewById(resId)).setText(text);
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
        setContentView(R.layout.tracing_12);

        // get the data
        fileName = getIntent().getStringExtra(NewInputDataActivity.BUNDLE_FILE_NAME);
        code = getIntent().getStringExtra(NewInputDataActivity.BUNDLE_CODE);
        bornDate = getIntent().getStringExtra(NewInputDataActivity.BUNDLE_BORN);
        lastEcgDate = getIntent().getStringExtra(NewInputDataActivity.BUNDLE_LAST_ECG);

        setText(R.id.playName, fileName);
        setText(R.id.playCodeValue, code);
        setText(R.id.playBornDate, bornDate);
        setText(R.id.playLastECGValue, lastEcgDate);
    }

    /**
     * Start clicked.
     *
     * @param view the view
     */
    public void startClicked(View view) {
        // switch the view.
        findViewById(R.id.tracingStart).setVisibility(View.GONE);
        findViewById(R.id.tracingStop).setVisibility(View.VISIBLE);

        // start measurements
        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream;

        try {
            outputStream = new PipedOutputStream(inputStream);
//            dataSource = new ECGCurveDataSource(new FileInputStream("/sdcard/ekg_files/after.bt12"));
            dataSource = new ECGCurveDataSource(inputStream);
        } catch (IOException e) {
            Log.e(ParentActivity.LOG_TAG, e.toString(), e);
            return;
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ECGLayout curve1 = (ECGLayout) findViewById(R.id.ecgCurve1);
            curve1.setDataSource(dataSource, new CurveName[] { CurveName.I, CurveName.II, CurveName.III, CurveName.aVR, CurveName.aVL, CurveName.aVF});

            ECGLayout curve2 = (ECGLayout) findViewById(R.id.ecgCurve2);
            curve2.setDataSource(dataSource, new CurveName[] { CurveName.V1, CurveName.V2, CurveName.V3, CurveName.V4, CurveName.V5, CurveName.V6 });

        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ECGLayout curve1 = (ECGLayout) findViewById(R.id.ecgCurve1);
            curve1.setDataSource(dataSource, new CurveName[] { CurveName.I, CurveName.II, CurveName.III });

            ECGLayout curve2 = (ECGLayout) findViewById(R.id.ecgCurve2);
            curve2.setDataSource(dataSource, new CurveName[] { CurveName.aVR, CurveName.aVL, CurveName.aVF});

            ECGLayout curve3 = (ECGLayout) findViewById(R.id.ecgCurve3);
            curve3.setDataSource(dataSource, new CurveName[] { CurveName.V1, CurveName.V2, CurveName.V3 });

            ECGLayout curve4 = (ECGLayout) findViewById(R.id.ecgCurve4);
            curve4.setDataSource(dataSource, new CurveName[] { CurveName.V4, CurveName.V5, CurveName.V6 });
        }

//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            ECGLayout curve = (ECGLayout) findViewById(R.id.ecgCurve);
//            curve.setDataSource(dataSource, new CurveName[] { CurveName.V1, CurveName.V2, CurveName.V3 });
//
//        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            ECGLayout curve1 = (ECGLayout) findViewById(R.id.ecgCurve1);
//            curve1.setDataSource(dataSource, new CurveName[] { CurveName.V1, CurveName.V2 });
//
//            ECGLayout curve2 = (ECGLayout) findViewById(R.id.ecgCurve2);
//            curve2.setDataSource(dataSource, new CurveName[] { CurveName.V3 });
//        }

        dataSource.start();

        commandService.setOutputStream(outputStream);
        commandService.write(BluetoothCommandService.BT12_START);

        // start the counter handler
        running = true;
        time = 0;
        handler.post(counterRunnable);

    }

    private int time = 0;
    private boolean running = false;
    private final Handler handler = new Handler();

    private final Runnable counterRunnable = new Runnable() {

        public void run() {

            int mintes = time / 60;
            int seconds = time % 60;
            setText(R.id.countUp, getTimeString(mintes) + " : " + getTimeString(seconds) + "   ");

            if (running) {
                handler.postDelayed(this, 1000);
            }
            time++;
        }
    };

    private static String getTimeString(int v) {
        if (v < 10) {
            return "0" + v;
        }
        return String.valueOf(v);
    }

    /**
     * Save clicked.
     * 
     * @param view
     *            the view
     */
    public void saveClicked(View view) {
        final Dialog saveDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        saveDialog.setContentView(R.layout.save_dialog);
        HomeActivity.setDimBackground(saveDialog);
        saveDialog.show();

        saveDialog.findViewById(R.id.saveClose).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                homeClicked(v);
            }
        });
    }

    /**
     * Restart clicked.
     * 
     * @param view
     *            the view
     */
    public void restartClicked(View view) {
        // switch the view.
        findViewById(R.id.tracingResult).setVisibility(View.GONE);
        findViewById(R.id.tracingStart).setVisibility(View.VISIBLE);
    }

    /**
     * Stop clicked.
     * 
     * @param view
     *            the view
     */
    public void stopClicked(View view) {
        running = false;
        // switch the view.
        findViewById(R.id.tracingStop).setVisibility(View.GONE);
        findViewById(R.id.tracingResult).setVisibility(View.VISIBLE);

        stopBluetoothConnection();
    }

    private void stopBluetoothConnection() {

        if (commandService != null) {
            commandService.write(BluetoothCommandService.BT12_STOP);
        }

        if (dataSource != null) {
            dataSource.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBluetoothConnection();

        if (commandService != null) {
            commandService.stop();
            commandService = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopBluetoothConnection();
    }
}