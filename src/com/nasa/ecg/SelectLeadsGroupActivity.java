/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.nasa.ecg.views.ECGLayout;

/**
 * 
 * This is the select leads activity for a group of curves.
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class SelectLeadsGroupActivity extends ParentActivity {

    /**
     * Called when the activity gets created.
     * @param savedInstanceState the saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_leads_group);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ECGLayout curve = (ECGLayout) findViewById(R.id.ecgCurve);
            curve.setAllPoints(getAllECGPoints(4));
            curve.setStatic(true);
        } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ECGLayout curve1 = (ECGLayout) findViewById(R.id.ecgCurve1);
            curve1.setAllPoints(getAllECGPoints(2));
            curve1.setStatic(true);
            
            ECGLayout curve2 = (ECGLayout) findViewById(R.id.ecgCurve2);
            curve2.setAllPoints(getAllECGPoints(2));
            curve2.setStatic(true);
        }
    }
    
    /**
     * Play clicked.
     *
     * @param view the view
     */
    public void playClicked(View view) {
        startActivityForResult(Play12LeadsActivity.class);
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