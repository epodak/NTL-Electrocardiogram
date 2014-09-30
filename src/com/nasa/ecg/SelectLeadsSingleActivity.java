/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import android.os.Bundle;
import android.view.View;

import com.nasa.ecg.views.ECGLayout;

/**
 * 
 * This is the select leads activity for a single curve.
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class SelectLeadsSingleActivity extends ParentActivity {

    /**
     * Called when the activity gets created.
     * @param savedInstanceState the saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_leads_single);
        
        ECGLayout curve = (ECGLayout) findViewById(R.id.ecgCurve);
        curve.setAllPoints(getAllECGPoints(1));
        curve.setStatic(true);
    }

    /**
     * Play clicked.
     *
     * @param view the view
     */
    public void playClicked(View view) {
        startActivityForResult(PlayBackSingleActivity.class);
    }

    /**
     * Single clicked.
     *
     * @param view the view
     */
    public void singleClicked(View view) {
        startActivityForResult(SelectLeadsSingleActivity.class);
    }
}