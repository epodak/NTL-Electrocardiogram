/*
 * Copyright (C) 2011 TopCoder Inc., All Rights Reserved.
 */
package com.nasa.ecg;

import android.os.Bundle;
import android.os.Handler;

/**
 * 
 * This is the splash activity.
 * 
 * @author mohamede1945
 * @version 1.0
 */
public class SplashActivity extends ParentActivity {

    public static int readLittleIndian(byte[] buffer, int position) {

        return (0x00ff & buffer[position]) + (buffer[position + 1] << 8);
    }

    /**
     * Called when the activity gets created.
     * @param savedInstanceState the saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        //        File file = new File("/sdcard/1DecodedData.BT12");
        //        try {
        //            InputStream stream = new FileInputStream(file);
        //            byte[] buffer = new byte[16];
        //
        //            int read;
        //
        //            while ((read = stream.read(buffer)) != -1) {
        //                Log.d("testing", "Read size:" + read);
        //
        //                //                int ii = readLittleIndian(buffer, 0);
        //                //                int iii = readLittleIndian(buffer, 2);
        //                //                int v1 = readLittleIndian(buffer, 4);
        //                //                int v2 = readLittleIndian(buffer, 6);
        //                //                int v3 = readLittleIndian(buffer, 8);
        //                //                int v4 = readLittleIndian(buffer, 10);
        //                //                int v5 = readLittleIndian(buffer, 12);
        //                //                int v6 = readLittleIndian(buffer, 14);
        //            }
        //
        //            stream.close();
        //        } catch (FileNotFoundException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        } catch (IOException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            public void run() {
                finish();

                startActivityForResult(HomeActivity.class);
            }
        }, getResources().getInteger(R.integer.splashTime));
    }
}