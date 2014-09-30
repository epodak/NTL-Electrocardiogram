/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg.views.animation;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

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
 * retrieval or logic.
 * </p>
 * 
 * @author mohamede1945
 * @version 1.1
 */
public class ECGCurve extends View {

    private final List<Float> ecgPoints = new ArrayList<Float>();

    private boolean dirty = true;

    private CurveName curveName;

    /** The paint. */
    private Paint paint = new Paint();

    /** The horizontal zoom level. */
    private float horizontalZoomLevel;

    /** The vertical zoom level. */
    //private float verticalZoomLevel;

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

    private float scaleFromRange(float x, float min, float max) {
        float height = getHeight() / 2;

        return ((2 * height) * (x - min) / (max - min)) - height;
    }

    private Path path;

    private Float transform(Float value) {

        float result;
        switch (curveName) {
        case I:
            result = scaleFromRange(value, -200, 600);
            break;
        case II:
            result = scaleFromRange(value, -200, 800);
            break;
        case III:
            result = scaleFromRange(value, -600, 600);
            break;
        case aVR:
            result = scaleFromRange(value, -500, 100);
            break;
        case aVL:
            result = scaleFromRange(value, -400, 600);
            break;
        case aVF:
            result = scaleFromRange(value, -400, 800);
            break;
        case V1:
            result = scaleFromRange(value, -600, 600);
            break;
        case V2:
            result = scaleFromRange(value, -640, 640);
            break;
        case V3:
            result = scaleFromRange(value, -360, 360);
            break;
        case V4:
            result = scaleFromRange(value, -200, 800);
            break;
        case V5:
            result = scaleFromRange(value, -450, 650);
            break;
        case V6:
            result = scaleFromRange(value, -200, 700);
            break;
        default:
            result = value;
        }

        return result * -1;
    }

    private void calculateCurve(Canvas canvas) {
        double xStep = 1;

        // clip the points
        int numberOfPointsToDisplay = (int) Math.ceil(getWidth() / xStep);

        if (ecgPoints.size() > numberOfPointsToDisplay) {
            for (int i = 0; i < ecgPoints.size() - numberOfPointsToDisplay; i++)
                ecgPoints.remove(0);
        }

        float[] points = null;

        // apply the compression/expansion value to the data.
        points = new float[ecgPoints.size()];
        for (int i = 0; i < Math.min(points.length, ecgPoints.size()); i++) {
            points[i] = transform(ecgPoints.get(i));
        }

        path = new Path();
        float mid = getHeight() / 2;

        boolean firstPoint = true;

        float x = 0;

        //        paint.setColor(Color.RED);
        //        canvas.drawLine(0, mid, getWidth(), mid, paint);
        //        paint.setColor(Color.BLACK);
        //        canvas.drawLine(0, 0, getWidth(), 0, paint);
        //        canvas.drawLine(0, getHeight(), getWidth(), getHeight(), paint);
        //        canvas.drawLine(0, 0, 0, getHeight(), paint);
        //        canvas.drawLine(getWidth(), 0, getWidth(), getHeight(), paint);
        //        paint.setColor(Color.WHITE);

        for (int i = Math.max(0, points.length - numberOfPointsToDisplay); i < points.length; i++) {

            // calculate the y axis of point x.
            float y = points[i] * horizontalZoomLevel + mid;

            if (firstPoint) {
                // start point
                path.moveTo(x, y);
                firstPoint = false;
            } else {
                path.lineTo(x, y);
            }

            x += xStep;
            if (x >= getWidth()) {
                break;
            }
        }
    }

    /**
     * Draw ECG.
     * 
     * @param canvas
     *            the canvas
     */
    private void drawECG(Canvas canvas) {

        if (dirty) {
            calculateCurve(canvas);
            dirty = false;
        }

        // The actual drawing.
        canvas.drawPath(path, paint);

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
     * Sets the horizontal zoom level.
     * 
     * @param zoom
     *            the new horizontal zoom level
     */
    public void setHorizontalZoomLevel(float zoom) {
        horizontalZoomLevel = zoom;
        invalidate();
    }

    public synchronized void appendPoints(List<Float> points) {
        ecgPoints.addAll(points);
        dirty = true;
    }

    public synchronized void appendPoint(Float point) {
        ecgPoints.add(point);
        dirty = true;
    }

    public CurveName getCurveName() {
        return curveName;
    }

    public void setCurveName(CurveName curveName) {
        this.curveName = curveName;
    }
}