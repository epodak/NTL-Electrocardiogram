/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nasa.ecg.views.HomeElement;

/**
 * 
 * This is the home activity.
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class HomeActivity extends ParentActivity {

    /** The elements. */
    private final ArrayList<HomeElement> elements = new ArrayList<HomeElement>();

    /** The selected index. */
    private Integer selectedIndex;

    /** The l. */
    private final OnClickListener l = new OnClickListener() {

        public void onClick(View v) {

            for (int i = 0; i < elements.size(); i++) {
                elements.get(i).setSelected(elements.get(i) == v);
                if (elements.get(i) == v) {
                    selectedIndex = i;
                }
            }
        }
    };

    /**
     * Called when the activity gets created.
     * 
     * @param savedInstanceState
     *            the saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        selectedIndex = null;

        LinearLayout homeElementsLayout = (LinearLayout) findViewById(R.id.homeElementsLayout);
        elements.clear();
        for (int i = 0; i < getResources().getInteger(R.integer.home_elements); i++) {
            HomeElement element = new HomeElement(this);
            element.setOnClickListener(l);
            homeElementsLayout.addView(element);
            elements.add(element);
        }
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
        setContentView(R.layout.home);

        LinearLayout homeElementsLayout = (LinearLayout) findViewById(R.id.homeElementsLayout);

        elements.clear();
        for (int i = 0; i < getResources().getInteger(R.integer.home_elements); i++) {
            HomeElement element = new HomeElement(this);
            element.setOnClickListener(l);
            homeElementsLayout.addView(element);
            elements.add(element);
        }

        if (selectedIndex != null) {
            elements.get(selectedIndex).setSelected(true);
        }
    }

    /**
     * Delete clicked.
     * 
     * @param v
     *            the v
     */
    public void deleteClicked(View v) {
        
        if (selectedIndex == null) {
            Toast.makeText(this, "Please, select an item", Toast.LENGTH_LONG).show();
            return;
        }
        
        final Dialog deleteDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        deleteDialog.setContentView(R.layout.delete_dialog);
        setDimBackground(deleteDialog);
        deleteDialog.show();

        deleteDialog.findViewById(R.id.homeOK).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });

        deleteDialog.findViewById(R.id.homeCancel).setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                deleteDialog.cancel();
            }
        });
    }

    /**
     * Open clicked.
     * 
     * @param v the the view object.
     */
    public void openClicked(View v) {
        if(selectedIndex == null) {
            Toast.makeText(this, "Please, select an item", Toast.LENGTH_LONG).show();
            return;
        }
        startActivityForResult(LoadingActivity.class);
    }
    
    /**
     * Open.
     */
    public void open() {
    	startActivityForResult(LoadingActivity.class);
    }

    /**
     * New clicked.
     * 
     * @param v the the view object.
     */
    public void newClicked(View v) {
        startActivityForResult(NewSettingActivity.class);
    }

    /**
     * Sets the dim background.
     * 
     * @param dialog
     *            the new dim background
     */
    public static void setDimBackground(Dialog dialog) {

        if (dialog != null) {
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.dimAmount = 0.7f;
            dialog.getWindow().setAttributes(lp);
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

}
