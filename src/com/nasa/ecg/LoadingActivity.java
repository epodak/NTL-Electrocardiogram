/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import android.os.Bundle;
import android.os.Handler;

/**
 * 
 * This is the loading activity.
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class LoadingActivity extends ParentActivity {

    /**
     * Called when the activity gets created.
     * @param savedInstanceState the saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        
        Handler handler = new Handler() ;
        handler.postDelayed(new Runnable() {
            
            public void run() {
                finish();
                
                startActivityForResult(SelectLeadsActivity.class);
            }
        }, getResources().getInteger(R.integer.loadingTime));
    }
}