/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg.views;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

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
 * retrieval or logic.
 * </p>
 * 
 * @author mohamede1945
 * @version 1.1
 */
public class ECGCurve extends View {

    /** The is static. */
    private boolean isStatic = false;

    /** The percentage. */
    private float percentage = 1;

    /** The paint. */
    private Paint paint = new Paint();

    /** The horizontal zoom level. */
    private float horizontalZoomLevel;

    /** The vertical zoom level. */
    private float verticalZoomLevel;

    /** The mid points. */
    private float[] midPoints;

    /** The all points that represents all ECG data. */
    private List<Float[]> allPoints = null;

    /** The animation width. */
    private float animationWidth = 0;

    /** The fixed animation width. */
    private float fixedAnimationWidth = 0;

    /**
     * Instantiates a new eCG curve.
     * 
     * @param context
     *            the context
     */
    public ECGCurve(Context context) {
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
    public ECGCurve(Context context, AttributeSet attrs) {
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
    public ECGCurve(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Initialize the view object.
     */
    private void init() {
        paint.setAntiAlias(true);
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

        // ------- draw the ECG -------
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        paint.setStyle(Style.STROKE);

        drawECG(canvas);
    }

    /**
     * Draw ECG.
     * 
     * @param canvas
     *            the canvas
     */
    private void drawECG(Canvas canvas) {

        // draw each curve inside the for loop.
        for (int c = 0; c < getAllPoints().size(); c++) {
            float[] points = null;

            // apply the compression/expansion value to the data.
            points = new float[(int) (getAllPoints().get(c).length / verticalZoomLevel)];
            for (int i = 0; i < Math.min(points.length, getAllPoints().get(c).length); i++) {
                points[i] = getAllPoints().get(c)[i];
            }

            Path path = new Path();
            float mid;

            // calculate the x axis the data will be around.
            if (midPoints == null || midPoints.length == 0) {
                mid = (c + 1) * getHeight() / (getAllPoints().size() + 1.0f);
            } else {
                mid = midPoints[c];
            }

            // Draw the data.
            path.moveTo(0, mid);
            final float increment = getWidth() / (points.length * 1.0f);
            int i = 0;
            float width = isStatic ? percentage * getWidth() : Math.min(getAnimationWidth() + getFixedAnimationWidth(),
                    getWidth());
            for (float x = 1; x < width && i < points.length; x += increment, i++) {
                // calculate the y axis of point x.
                float y = points[i] * horizontalZoomLevel + mid;
                canvas.drawPoint(x, y, paint);
                path.lineTo(x, y);
            }

            // The actual drawing.
            canvas.drawPath(path, paint);
        }
    }

    /**
     * The listener interface for receiving onSizeChanged events.
     * The class that is interested in processing a onSizeChanged
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addOnSizeChangedListener<code> method. When
     * the onSizeChanged event occurs, that object's appropriate
     * method is invoked.
     *
     * @see OnSizeChangedEvent
     */
    public interface OnSizeChangedListener {
        
        /**
         * Size changed.
         *
         * @param v the v
         */
        public void sizeChanged(View v);
    }

    /** The listener. */
    private OnSizeChangedListener listener;

    /**
     * Sets the on size changed listener.
     *
     * @param listener the new on size changed listener
     */
    public void setOnSizeChangedListener(OnSizeChangedListener listener) {
        this.listener = listener;
    }

    /**
     * Called when the size changes.
     * @see android.view.View#onSizeChanged(int, int, int, int)
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (getWidth() == 0) {
            return;
        }

        if (listener != null) {
            listener.sizeChanged(this);
        }
    }

    /**
     * Sets the vertical zoom level.
     * 
     * @param zoom
     *            the new vertical zoom level
     */
    public void setVerticalZoomLevel(float zoom) {
        verticalZoomLevel = zoom;
        invalidate();
    }

    /**
     * Sets the horizontal zoom level.
     * 
     * @param zoom
     *            the new horizontal zoom level
     */
    public void setHorizontalZoomLevel(float zoom) {
        horizontalZoomLevel = zoom;
        invalidate();
    }

    /**
     * Gets the mid points.
     * 
     * @return the mid points
     */
    public float[] getMidPoints() {
        return midPoints;
    }

    /**
     * Sets the mid points.
     * 
     * @param midPoints
     *            the new mid points
     */
    public void setMidPoints(float[] midPoints) {
        if (midPoints != null && midPoints.length != getAllPoints().size()) {
            throw new IllegalArgumentException("number of elements in the array should equal to number of curves.");
        }
        this.midPoints = midPoints;
    }

    /**
     * Gets the all points.
     * 
     * @return the all points
     */
    public List<Float[]> getAllPoints() {
        return allPoints;
    }

    /**
     * Sets the all points.
     * 
     * @param allPoints
     *            the new all points
     */
    public void setAllPoints(List<Float[]> allPoints) {
        this.allPoints = allPoints;
    }

    /**
     * Gets the animation width.
     * 
     * @return the animation width
     */
    public float getAnimationWidth() {
        return animationWidth;
    }

    /**
     * Sets the animation width.
     * 
     * @param animationWidth
     *            the new animation width
     */
    public void setAnimationWidth(float animationWidth) {
        this.animationWidth = animationWidth;
    }

    /**
     * Gets the fixed animation width.
     * 
     * @return the fixedAnimationWidth
     */
    public float getFixedAnimationWidth() {
        return fixedAnimationWidth;
    }

    /**
     * Sets the fixed animation width.
     * 
     * @param fixedAnimationWidth
     *            the fixedAnimationWidth to set
     */
    public void setFixedAnimationWidth(float fixedAnimationWidth) {
        this.fixedAnimationWidth = fixedAnimationWidth;
    }

    /**
     * Checks if is static.
     *
     * @return true, if is static
     */
    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Sets the static.
     *
     * @param isStatic the new static
     */
    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    /**
     * Sets the percentage.
     *
     * @param percentage the new percentage
     */
    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}