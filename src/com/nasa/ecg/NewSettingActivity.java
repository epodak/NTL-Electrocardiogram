/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

/**
 * 
 * This is the new setting activity.
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class NewSettingActivity extends ParentActivity {

    /**
     * Called when the activity gets created.
     * 
     * @param savedInstanceState
     *            the saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
    }

    /**
     * Called when the configuration of the activity is changed.
     * 
     * @param newConfig
     *            the new configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.setting);
    }

    /**
     * Continue clicked.
     * 
     * @param view
     *            the view
     */
    public void continueClicked(View view) {
        startActivityForResult(NewInputDataActivity.class);
    }
}