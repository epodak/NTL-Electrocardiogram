/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import android.os.Bundle;
import android.view.View;

/**
 * 
 * This is the select leads activity.
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class SelectLeadsActivity extends ParentActivity {

    /**
     * Called when the activity gets created.
     * @param savedInstanceState the saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_leads);
    }

    /**
     * Lead clicked.
     *
     * @param view the view
     */
    public void leadClicked(View view) {
        startActivityForResult(SelectLeadsGroupActivity.class);
    }
}