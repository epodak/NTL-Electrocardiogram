package com.nasa.ecg.bluetooth;

import com.nasa.ecg.NewInputDataActivity;
import com.nasa.ecg.ParentActivity;
import com.nasa.ecg.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/*
 *  5DEC11wbk
 *  This file is basically Android application boilerplate.
 *  
 *  Mostly derived from the Bluetooth Chat SDK example and
 *  this example:
 *  http://luugiathuy.com/2011/02/android-java-bluetooth/
 *  
 *  We shouldn't need to change much of anything with it initially.
 *  A better user interface will require more Android experience
 *  than I care to develop.
 */

public abstract class BluetoothActivity extends ParentActivity {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 11;
    private static final int REQUEST_ENABLE_BT = 12;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_ENDING = 6;

    // Key names received from the BluetoothCommandService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for Bluetooth Command Service
    public BluetoothCommandService commandService = null;
    private String fileName;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) { // Android equivalent of main()
        super.onCreate(savedInstanceState);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        fileName = getIntent().getStringExtra(NewInputDataActivity.BUNDLE_FILE_NAME);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If BT is not on, request that it be enabled.
        // setupCommand() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        // otherwise set up the command service
        else {
            if (commandService == null)
                setupCommand();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (commandService != null) {
            if (commandService.getState() == BluetoothCommandService.STATE_NONE) {
                commandService.start();
            }
        }
    }

    private void setupCommand() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        commandService = new BluetoothCommandService(this, mHandler);
        commandService.setFileName(fileName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (commandService != null) {
            commandService.stop();
            commandService = null;
        }
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothCommandService.STATE_RECEIVING:
                    //                    mTitle.setText("Receiving BT12 data: ");
                    //                    mTitle.append(mConnectedDeviceName);
                    break;
                case BluetoothCommandService.STATE_CONNECTED:
                    //                    mTitle.setText(R.string.title_connected_to);
                    //                    mTitle.append(mConnectedDeviceName);
                    break;
                case BluetoothCommandService.STATE_CONNECTING:
                    //                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothCommandService.STATE_NONE:
                    //                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(
                        getApplicationContext(),
                        "Connected " + mConnectedDeviceName + "BT12 protocol version: "
                                + BluetoothCommandService.BT12version, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_ENDING:
                // report bytes written & CRC errors

                Toast.makeText(
                        getApplicationContext(),
                        "Data collection finished: " + "\nBytes written: " + BluetoothCommandService.FileBytes
                                + "\nCRC errors: " + BluetoothCommandService.CRCerrors, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                commandService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupCommand();
            } else {
                // User did not enable Bluetooth or an error occured
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.d("AndroidB12", "Key pressed:" + keyCode);
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            Log.d("AndroidB12", "Starting");
//            mCommandService.write(BluetoothCommandService.BT12_START);
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            Log.d("AndroidB12", "Stopping");
//            mCommandService.write(BluetoothCommandService.BT12_STOP);
//            return true;
//        }

        return super.onKeyDown(keyCode, event);
    }
}