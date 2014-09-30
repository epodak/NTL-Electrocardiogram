/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * 
 * This is the new input data activity.
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class NewInputDataActivity extends ParentActivity {

    public static final String BUNDLE_FILE_NAME = "file name";
    public static final String BUNDLE_CODE = "code";
    public static final String BUNDLE_BORN = "born date";
    public static final String BUNDLE_LAST_ECG = "last ecg date";

    /**
     * Called when the activity gets created.
     * @param savedInstanceState the saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_data);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.input_data);
    }

    private String getEditTextValue(int viewId) {
        return ((EditText) findViewById(viewId)).getText().toString();
    }

    private static boolean validateDate(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        Date testDate;
        try {
            testDate = formatter.parse(date);
        } catch (ParseException e) {
            return false;
        }

        return formatter.format(testDate).equals(date);
    }

    /**
     * Continue clicked.
     *
     * @param view the view
     */
    public void continueClicked(View view) {

        String fileName = getEditTextValue(R.id.input_fileName);
        String code = getEditTextValue(R.id.input_code);
        String born = getEditTextValue(R.id.input_born);
        String lastEcg = getEditTextValue(R.id.input_lastEcg);

        fileName = "test_1";
        code = "code";
        born = "12/12/1990";
        lastEcg = "12/12/2012";

        if (fileName.trim().length() == 0) {
            displayDialog("File name is required");
            return;
        }
        if (code.trim().length() == 0) {
            displayDialog("Code is required");
            return;
        }

        if (!validateDate(born)) {
            displayDialog("Born is not a valid date");
            return;
        }
        if (!validateDate(lastEcg)) {
            displayDialog("Last Ecg is not a valid date");
            return;
        }

        if (fileName.toLowerCase().endsWith(".bt12")) {
            fileName += ".bt12";
        }

        Bundle b = new Bundle();
        b.putString(BUNDLE_FILE_NAME, fileName);
        b.putString(BUNDLE_CODE, code);
        b.putString(BUNDLE_BORN, born);
        b.putString(BUNDLE_LAST_ECG, lastEcg);

        startActivityForResult(TracingActivity.class, b);
    }
}