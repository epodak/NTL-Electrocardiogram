/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg.views.animation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.nasa.ecg.ParentActivity;

/**
 * 
 * This is the most important class for (NASA ECG Viewer for Android Prototype
 * Animation Assembly version 1.0). This class is the animation class of the ECG
 * curve. It performs animation for (normal playing, fast forward, rewind,
 * pausing, and dragging).
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class ECGCurveDataSource implements Runnable {

    public final static int BUFFER_SIZE = 16;
    public final static int SLEEP_TIME = 10;
    public final static int NUMBER_OF_SAMPLES_TO_AVERAGE = 5;

    public enum CurveName {
        I, II, III, V1, V2, V3, V4, V5, V6, aVR, aVL, aVF, 
    }

    /**
     * The AnimationState enumerator.
     */
    public static enum AnimationState {

        /** The Not started. */
        NotStarted,
        /** The Playing. */
        Playing,
        /** The Paused. */
        Paused,
        /** The Fast forward. */
        FastForward,
        /** The Rewind. */
        Rewind,
        /** The Finished. */
        Finished,
        /** The Dragged. */
        Dragged
    }

    private static enum State {
        Running, Stopping, Stopped
    }

    private State state = State.Stopped;

    private Thread thread;

    /** The curve. */
    private List<ECGCurve> curves = new ArrayList<ECGCurve>();

    private InputStream inputstream;

    /**
     * Instantiates a new ECG curve animation instance.
     */
    public ECGCurveDataSource(InputStream inputstream) {
        this.inputstream = inputstream;
    }

    private static int readLittleIndian(List<Byte> buffer, int position) {

        return (0x00ff & buffer.get(position)) + (buffer.get(position + 1) << 8);
    }

    private static void fill(List<Integer> list, int constantStart, List<Byte> buffer) {

        for (int i = constantStart; i < buffer.size(); i += 16) {
            list.add(readLittleIndian(buffer, i));
        }
    }
    
    private static List<Integer> sum(float factor1, List<Integer> list1, float factor2, List<Integer> list2) {
        List<Integer> result = new ArrayList<Integer>();
        
        for (int i = 0; i < list1.size(); i++) {
            float sum = factor1 * list1.get(i);
            sum += factor2 * list2.get(i);
            result.add((int) sum);
        }

        return result;
    }

    private static void average(List<Integer> list) {

        if (list.size() == 0) {
            return;
        }

        long sum = 0;
        for (Integer v : list) {
            sum += v;
        }

        long average = sum / list.size();
        list.clear();
        list.add((int) average);
    }

    public void run() {

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        //int start = 0;

        try {
            List<Integer> iiList = new ArrayList<Integer>();
            List<Integer> iiiList = new ArrayList<Integer>();
            List<Integer> v1List = new ArrayList<Integer>();
            List<Integer> v2List = new ArrayList<Integer>();
            List<Integer> v3List = new ArrayList<Integer>();
            List<Integer> v4List = new ArrayList<Integer>();
            List<Integer> v5List = new ArrayList<Integer>();
            List<Integer> v6List = new ArrayList<Integer>();

            long startTime = System.currentTimeMillis();
            List<Byte> allData = new ArrayList<Byte>();
            

            state = State.Running;
            
            int sampleSize = NUMBER_OF_SAMPLES_TO_AVERAGE * 16;
            while ((bytesRead = inputstream.read(buffer)) != -1 && state == State.Running) {

                for (int i = 0; i < bytesRead; i++) {
                    allData.add(buffer[i]);
                }

                if (allData.size() < sampleSize) {
                    continue;
                }

                List<Byte> sub = allData.subList(0, sampleSize);
                List<Byte> bufferList = new ArrayList<Byte>(sub);
                sub.clear();

                fill(iiList, 0, bufferList);
                fill(iiiList, 2, bufferList);
                fill(v1List, 4, bufferList);
                fill(v2List, 6, bufferList);
                fill(v3List, 8, bufferList);
                fill(v4List, 10, bufferList);
                fill(v5List, 12, bufferList);
                fill(v6List, 14, bufferList);

                // average the samples
                average(iiList);
                average(iiiList);
                average(v1List);
                average(v2List);
                average(v3List);
                average(v4List);
                average(v5List);
                average(v6List);

                List<Integer> iList = sum(1, iiList, -1, iiiList);
                List<Integer> aVRList = sum(-0.5f, iList, -0.5f, iiList);
                List<Integer> aVLList = sum(1, iList, -0.5f, iiList);
                List<Integer> avFList = sum(1, iiList, -0.5f, iList);

                for (int i = 0; i < curves.size(); i++) {
                    ECGCurve curve = curves.get(i);

                    List<Integer> listToAppend;
                    switch (curve.getCurveName()) {
                    case I:
                        listToAppend = iList;
                        break;
                    case II:
                        listToAppend = iiList;
                        break;
                    case III:
                        listToAppend = iiiList;
                        break;
                    case aVR:
                        listToAppend = aVRList;
                        break;
                    case aVL:
                        listToAppend = aVLList;
                        break;
                    case aVF:
                        listToAppend = avFList;
                        break;
                    case V1:
                        listToAppend = v1List;
                        break;
                    case V2:
                        listToAppend = v2List;
                        break;
                    case V3:
                        listToAppend = v3List;
                        break;
                    case V4:
                        listToAppend = v4List;
                        break;
                    case V5:
                        listToAppend = v5List;
                        break;
                    case V6:
                        listToAppend = v6List;
                        break;
                    default:
                        continue;
                    }

                    for (int j = 0; j < listToAppend.size(); j++) {
                        curve.appendPoint((float) listToAppend.get(j));
                    }

                    // redraw
                    curve.postInvalidate();
                }

                // clear the lists
                iiList.clear();
                iiiList.clear();
                v1List.clear();
                v2List.clear();
                v3List.clear();
                v4List.clear();
                v5List.clear();
                v6List.clear();

                long elapsed = System.currentTimeMillis() - startTime;

                if (elapsed < SLEEP_TIME) {
                    Thread.sleep(Math.max(0, SLEEP_TIME - elapsed));
                }
                startTime = System.currentTimeMillis();
            }
        } catch (Exception e) {
            Log.e(ParentActivity.LOG_TAG, e.toString(), e);
        } finally {
            state = State.Stopped;
            if (inputstream != null) {
                try {
                    inputstream.close();
                } catch (IOException e) {
                    Log.e(ParentActivity.LOG_TAG, e.toString(), e);
                }
            }
        }
    }

    public void run1() {

        try {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            int b;

            while ((b = inputstream.read()) != -1) {
                byteOutput.write(b);
            }

            String[] lines = byteOutput.toString().split("\n");

            for (int j = 0; j < lines.length; j++) {
                //                Float[] data = new Float[] { Float.parseFloat(lines[j]) };

                for (int i = 0; i < curves.size(); i++) {
                    ECGCurve curve = curves.get(i);

                    //                    List<Float> list = new ArrayList<Float>(curve.getEcgPoints());
                    //
                    //                    for (int k = 0; k < data.length; k++) {
                    //                        list.add(data[k]);
                    //                    }
                    //
                    //                    curve.setEcgPoints(list);

                    // redraw
                    curve.postInvalidate();
                }

                Thread.sleep(10);
            }
        } catch (Exception e) {
            Log.e(ParentActivity.LOG_TAG, e.toString(), e);
        } finally {
            if (inputstream != null) {
                try {
                    inputstream.close();
                } catch (IOException e) {
                    Log.e(ParentActivity.LOG_TAG, e.toString(), e);
                }
            }
        }
    }

    public List<ECGCurve> getCurves() {
        return curves;
    }

    public void registerCurve(ECGCurve curve) {
        this.curves.add(curve);
    }

    public void stop() {
        state = State.Stopping;
    }

    public void start() {
        thread = new Thread(this);
        thread.setName("ECGCurveDataSource Thread");
        thread.start();
    }
}