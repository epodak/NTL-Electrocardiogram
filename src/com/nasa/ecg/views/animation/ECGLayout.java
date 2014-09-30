/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg.views.animation;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.nasa.ecg.R;
import com.nasa.ecg.views.animation.ECGCurveDataSource.CurveName;

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

    /** The curves. */
    private final List<ECGCurve> curves = new ArrayList<ECGCurve>();

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
        compressionFactor = resources.getInteger(R.integer.compressionFactor) / 100f;
        amplificationFactor = resources.getInteger(R.integer.amplificationFactor) / 100f;
        minCompression = resources.getInteger(R.integer.minCompression);
        minAmplification = resources.getInteger(R.integer.minAmplification);
        compressionFactorView = resources.getInteger(R.integer.compressionFactorView) / 100f;
        amplificationFactorView = resources.getInteger(R.integer.amplificationFactorView) / 100f;
        minCompressionView = resources.getInteger(R.integer.minCompressionView);
        minAmplificationView = resources.getInteger(R.integer.minAmplificationView);

        currentCompression = minCompression;
        currentAmplification = minAmplification;

        grid = new ECGGrid(getContext());
        grid.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(grid);
    }

    public void setDataSource(ECGCurveDataSource datasource, CurveName[] curveNames) {
        LinearLayout curvesLayout = new LinearLayout(getContext());
        curvesLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        curvesLayout.setOrientation(LinearLayout.VERTICAL);
        addView(curvesLayout);

        for (int i = 0; i < curveNames.length; i++) {

            ECGCurve curve = new ECGCurve(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            params.weight = 1;

            curve.setLayoutParams(params);
            curve.setHorizontalZoomLevel(currentAmplification);
            curve.setCurveName(curveNames[i]);
            // curve.setVerticalZoomLevel(currentCompression);

            curves.add(curve);
            curvesLayout.addView(curve);
            datasource.registerCurve(curve);
        }
    }

    /**
     * Amplify the curve.
     */
    public void amplify() {
        currentAmplification *= amplificationFactor;

        for (int i = 0; i < curves.size(); i++) {
            curves.get(i).setHorizontalZoomLevel(currentAmplification);
        }
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
        for (int i = 0; i < curves.size(); i++) {
            curves.get(i).setHorizontalZoomLevel(currentAmplification);
        }
    }

    /**
     * Decompress the curve.
     */
    public void decompress() {
        currentCompression *= compressionFactor;

        for (int i = 0; i < curves.size(); i++) {
            // curves.get(i).setVerticalZoomLevel(currentCompression);
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

        for (int i = 0; i < curves.size(); i++) {
            // curves.get(i).setVerticalZoomLevel(currentCompression);
        }
    }

    /**
     * Calc root.
     *
     * @param c the c
     * @param m the m
     * @param f the f
     * @return the int
     */
    public static int calcRoot(float c, float m, float f) {
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
}
