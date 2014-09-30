/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.nasa.ecg.views.ECGLayout.CounterFormatter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * 
 * This is the parent activity for the entire application.
 * 
 * <p>
 * Changes in 1.1 (NASA ECG Viewer for Android Prototype Animation Assembly
 * version 1.0): 1) Added logging tag <br />
 * 2) Added reading the ECG points from the file and construct array of curves.
 * No two curves have the same shape.
 * </p>
 * 
 * @author mohamede1945
 * @version 1.1
 */
public class ParentActivity extends Activity {

    /** The Constant HomeCode. */
    public static final int HomeCode = 45;

    /** The Constant logging tag. */
    public static final String LOG_TAG = "ECG";

    /** The Constant PROGRESS_STATE. */
    protected static final String PROGRESS_STATE = "progress_state";

    /** The Constant TRACING_POSITION. */
    protected static final String TRACING_POSITION = "tracing_position";

    /**
     * Gets the 2 digits.
     *
     * @param v the v
     * @return the 2 digits
     */
    protected static String get2Digits(int v) {
        if (v < 10) {
            return "0" + v;
        }
        return String.valueOf(v);
    }

    /**
     * Gets the time.
     *
     * @param time the time
     * @param ceil the ceil
     * @return the time
     */
    protected static String getTime(int time, boolean ceil) {
        // convert to seconds.
        if (ceil) {
            time = (int) Math.ceil(time / 1000.0);
        } else {
            time = time / 1000;
        }
        int minutes = time / 60; // get the minutes.
        int seconds = time % 60; // get the seconds.

        return get2Digits(minutes) + ":" + get2Digits(seconds);
    }

    /** The Constant upFormatter. */
    protected static final CounterFormatter upFormatter = new CounterFormatter() {
        public String format(int milliseconds) {
            return "  " + getTime(milliseconds, false) + "  ";
        }
    };

    /** The Constant downFormatter. */
    protected static final CounterFormatter downFormatter = new CounterFormatter() {
        public String format(int milliseconds) {
            return "  -" + getTime(milliseconds, true) + "  ";
        }
    };

    /**
     * Called when the activity start another one and this one is finished with
     * result code.
     * 
     * @param requestCode
     *            the request code.
     * @param resultCode
     *            the result code.
     * @param data
     *            the intent data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == HomeCode && !HomeActivity.class.isInstance(this)) {
            setResult(HomeCode);
            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Back clicked.
     * 
     * @param view
     *            the view
     */
    public void backClicked(View view) {
        finish();
    }

    /**
     * Home clicked.
     * 
     * @param view
     *            the view
     */
    public void homeClicked(View view) {
        setResult(HomeCode);
        finish();
    }

    /**
     * starts a new activity.
     * 
     * @param activity
     *            the activity class to start.
     */
    public void startActivityForResult(Class<?> activity, Bundle b) {
        Intent myIntent = new Intent(this, activity);
        myIntent.putExtras(b);
        startActivityForResult(myIntent, 0);
    }
    
    public void startActivityForResult(Class<?> activity) {
        Intent myIntent = new Intent(this, activity);
        startActivityForResult(myIntent, 0);
    }

    /**
     * starts a new activity.
     * 
     * @param activity
     *            the activity class to start.
     * @param position
     *            the position
     */
    public void startActivityForResult(Class<?> activity, int position) {
        Intent myIntent = new Intent(this, activity);
        myIntent.putExtra(TRACING_POSITION, position);
        startActivityForResult(myIntent, 0);
    }

    /**
     * Gets a list that contains numberOfCurves ECG curves. No two curves have
     * the same shape.
     * 
     * @param numberOfCurves
     *            the number of curves.
     * @return a list that contains numberOfCurves ECG curves.
     */
    protected List<Float[]> getAllECGPoints(int numberOfCurves) {
        List<Float[]> list = new ArrayList<Float[]>();

        for (int i = 0; i < numberOfCurves; i++) {
            Float[] points = getEcgPoints();
            Collections.rotate(Arrays.asList(points), (int) ((1.0f * i) / numberOfCurves * points.length));
            list.add(points);
        }

        return list;
    }

    /**
     * Display dialog with successful message.
     * 
     * @param message
     *            the message
     */
    protected void displayDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNeutralButton("OK", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Read the ECG curve from the file.
     * 
     * @return the ECG curve data points.
     */
    protected Float[] getEcgPoints() {
        Float[] points = null;
        InputStream stream = null;
        try {
            stream = getAssets().open("ecg_data.txt");

            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            int b;

            while ((b = stream.read()) != -1) {
                byteOutput.write(b);
            }

            String[] lines = byteOutput.toString().split("\n");

            points = new Float[lines.length];
            for (int i = 0; i < points.length; i++) {
                points[i] = Float.parseFloat(lines[i]);
            }

            byteOutput.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.toString());
                }
            }
        }

        return points;
    }
}
