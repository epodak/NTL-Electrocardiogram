/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.nasa.ecg.R;

/**
 * 
 * This is the most important view, which draws the ECG curve.
 * 
 * <p>
 * Changes in 1.1 (NASA ECG Viewer for Android Prototype Animation Assembly
 * version 1.0): 1) Added functionality for handling compression/decompression
 * and amplification/reduction.
 * </p>
 * 
 * @author mohamede1945
 * @version 1.1
 */
public class ECGGrid extends View {

    /** The paint. */
    private Paint paint = new Paint();

    /** The small spaces. */
    private int smallSpaces;

    /** The medium spaces. */
    private int mediumSpaces;

    /** The big spaces. */
    private int largeSpaces;

    /**
     * Instantiates a new eCG curve.
     * 
     * @param context
     *            the context
     */
    public ECGGrid(Context context) {
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
    public ECGGrid(Context context, AttributeSet attrs) {
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
    public ECGGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Initialize the view object.
     */
    private void init() {
        Resources resources = getContext().getResources();
        smallSpaces = resources.getInteger(R.integer.gridSmallSpaces);
        mediumSpaces = resources.getInteger(R.integer.gridMediumSpaces);
        largeSpaces = resources.getInteger(R.integer.gridLargeSpaces);
        paint.setAntiAlias(true);
        setBackgroundResource(R.drawable.curve_background);
    }

    /**
     * Called when the view needs to be drawn.
     * 
     * @param canvas
     *            the canvas to draw on.
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // ------- draw the grid -------

        // draw the small lines.
        paint.setStrokeWidth(0);
        paint.setColor(0xff44c3f7);
        drawGrid(canvas, smallSpaces);

        // draw the medium lines.
        paint.setStrokeWidth(1);
        paint.setColor(0xff5dcaf7);
        drawGrid(canvas, mediumSpaces);

        // draw the big lines.
        paint.setStrokeWidth(2);
        paint.setColor(0xff82d4f7);
        drawGrid(canvas, largeSpaces);
    }

    /**
     * Draw grid.
     * 
     * @param canvas
     *            the canvas
     * @param spaces
     *            the spaces
     */
    private void drawGrid(Canvas canvas, int spaces) {

        float vSpaces = spaces;
        float hSpaces = spaces;

        // draw horizontal lines
        for (float i = hSpaces; i < getHeight(); i += hSpaces) {
            canvas.drawLine(0, i, getWidth(), i, paint);
        }

        // draw vertical lines
        for (float i = vSpaces; i < getWidth(); i += vSpaces) {
            canvas.drawLine(i, 0, i, getHeight(), paint);
        }
    }
}