package com.nasa.ecg.bluetooth;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import com.nasa.ecg.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothCommandService {

    // Debugging
    private static final String TAG = "BluetoothCommandService";
    private static final boolean D = true;

    // Unique UUID for this application
    // Magic UUID which corresponds to the BT serial profile.
    /*
     * from emulateBT12 java code // Look like the BT12 device String url =
     * "btspp://localhost:" + new UUID( 0x1101 ).toString() +
     * ";name=ECG service"; // create the Magic UUID to match device notifier =
     * (StreamConnectionNotifier)Connector.open(url); System.out.println(url);
     * // print is so I can cut and paste into Android code.
     */
    public static final UUID BT12_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //
    // The BT12 devices supports a lot of other stuff not important or very
    // useful to us.
    // Here is an open source Android/Symbian project that uses the BT12 and
    // seems to try and support everything.
    // It was more helpful to me than the docs provided with the C-language sdk
    // we started with, wish I had it then.
    // http://code.google.com/p/moca/source/browse/clients/android/branches/hardware-1.0.x/src/org/moca/hardware/drivers/corscience/?r=826
    // But the purpose of this example is to let you just use the BT12 code and
    // test with the Java BT12 emulator (which only emulates what we need).

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    // private ConnectedThread mConnectedThread;
    private int mState;
    private Context mContext;

    private String fileName = "bluetooth_data.bt12";

    private OutputStream outputstream;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing
                                                  // connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote
                                                 // device
    public static final int STATE_RECEIVING = 4; // start command received,
                                                 // collecting data

    // Constants that indicate command to computer
    public static final int EXIT_CMD = -1;
    public static final int BT12_START = 1;
    public static final int BT12_STOP = 2;

    // protocol constants retrieved from protocol version request reply
    public static int BT12version = 0;
    public static int BT12headerSkip = 0;
    public static int BT12trailerSkip = 0;

    // a couple of things to report to the user at the end of a data collection
    // epoch
    public static int FileBytes = 0;
    public static int CRCerrors = 0;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * 
     * @param context
     *            The UI Activity Context
     * @param handler
     *            A Handler to send messages back to the UI Activity
     */
    public BluetoothCommandService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        // mConnectionLostCount = 0;
        mHandler = handler;
        mContext = context;
    }

    /**
     * Set the current state of the chat connection
     * 
     * @param state
     *            An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D)
            Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */

    public synchronized void start() {
        if (D)
            Log.d(TAG, "start");
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        setState(STATE_NONE);
    }

    public synchronized void setFileName(String file) {
        fileName = file;
    }

    public synchronized void setOutputStream(OutputStream outputstream) {
        this.outputstream = outputstream;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * 
     * @param device
     *            The BluetoothDevice to connect
     */

    public synchronized void connect(BluetoothDevice device) {
        if (D)
            Log.d(TAG, "connect to: " + device);
        // Cancel any thread attempting to make a connection
        if (mState != STATE_NONE/* == STATE_CONNECTING */) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start(); // inherited method that ultimate calls our run
                                // override method
        setState(STATE_CONNECTING);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D)
            Log.d(TAG, "stop");
        if (mConnectThread != null && mState != STATE_NONE) {
            mConnectThread.cancel();
            mConnectThread = null;
            setState(STATE_NONE);
        }
    }

    public void write(int out) {
        // Create temporary object
        ConnectThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (!(mState == STATE_CONNECTED || mState == STATE_RECEIVING))
                return;
            r = mConnectThread;
            // r.write(out);
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_NONE);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private boolean gotFC = false;
        private boolean gotFD = false;

        // mode set string for 500Hz 8-Channel ECG data transmission.
        private final byte mode[] = { (byte) 0xFC, (byte) 0x04, (byte) 0x01, (byte) 0x09, (byte) 0x02, (byte) 0x05,
                (byte) 0xE8, (byte) 0x46, (byte) 0xFD };
        private final byte modeReply[] = { (byte) 0xFC, (byte) 0x00, (byte) 0x01, (byte) 0x07, (byte) 0x02,
                (byte) 0x05, (byte) 0xEF, (byte) 0xD4, (byte) 0xFD };
        // protocol version request string, our hardware is version 3, so that
        // will be our reply, 1,2,&4 are other possibilities I know of
        private final byte protocolRequest[] = { (byte) 0xFC, (byte) 0x02, (byte) 0x00, (byte) 0x08, (byte) 0x00,
                (byte) 0x01, (byte) 0x0F, (byte) 0xEC, (byte) 0xFD };
        // private final byte
        // protocolReply[]={(byte)0xFC,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x03,(byte)0xDC,(byte)0x00,(byte)0x1E,(byte)0x34,(byte)0xFD};
        // client sends these packets to start/stop the data flow
        private final byte BT12_start[] = { (byte) 0xFC, (byte) 0x05, (byte) 0x05, (byte) 0x09, (byte) 0x01,
                (byte) 0xCC, (byte) 0x79, (byte) 0xfD };
        private final byte BT12_stop[] = { (byte) 0xFC, (byte) 0x06, (byte) 0x05, (byte) 0x09, (byte) 0x00,
                (byte) 0x31, (byte) 0xF2, (byte) 0xFD };

        // BT12 protocol magic bytes
        private final byte BTfirst = (byte) 0xFC;
        private final byte BTlast = (byte) 0xFD;
        private final byte BTescape = (byte) 0xFE;
        
        private BluetoothSocket createSocket(BluetoothDevice device)
                throws SecurityException, NoSuchMethodException,
                IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            BluetoothDevice hxm = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.getAddress());
            Method m;
            m = hxm.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            return (BluetoothSocket)m.invoke(hxm, Integer.valueOf(1)); 
        }

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            // Get a BluetoothSocket for a connection with the given
            // BluetoothDevice
            try {
                mmSocket = createSocket(device);
            } catch (Exception e) {
                Log.e(TAG, "create() failed", e);
                mmSocket = null;
            }
            // Get the BluetoothSocket input and output streams
            try {
                mmInStream = mmSocket.getInputStream();
                mmOutStream = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "sockets not created", e);
                mmInStream = null;
                mmOutStream = null;
            }
        }

        // found via Google, minor changes for our purposes:
        // http://introcs.cs.princeton.edu/java/51data/CRC16CCITT.java.html
        //
        // some background info on various CRC schemes:
        // http://en.wikipedia.org/wiki/Cyclic_redundancy_check
        //
        // C-language sample code:
        // http://www.lammertbies.nl/comm/info/crc-calculation.html
        /*************************************************************************
         * 
         * Reads in a sequence of bytes and calculates its 16 bit Cylcic
         * Redundancy Check (CRC-CCIIT 0xFFFF).
         * 
         * 1 + x + x^5 + x^12 + x^16 is irreducible polynomial.
         * 
         *************************************************************************/
        // The Eclipse/Android debugger is really lame, which made getting this
        // to work correctly waste a lot of my time.
        // I've seen some evidence of performance issues on the phone,
        // converting this to the table
        // driven algorithm as shown in the C-language example code link above
        // should help.
        public boolean CRC16CCITT(byte packet[], int first, int last) {
            int crc = 0xFFFF; // initial value
            int polynomial = 0x1021; // 0001 0000 0010 0001 (0, 5, 12)
            byte b;
            int lb = 0, hb = 0;

            for (int j = first; j <= last; j++) {
                b = packet[j];
                for (int i = 0; i < 8; i++) {
                    boolean bit = ((b >> (7 - i) & 1) == 1);
                    boolean c15 = ((crc >> 15 & 1) == 1);
                    crc <<= 1;
                    if (c15 ^ bit)
                        crc ^= polynomial;
                }
            }
            crc &= 0xffff;
            lb = packet[last + 1] & 0x0ff; // CRC word is not included in CRC
                                           // check
            hb = packet[last + 2] & 0x0ff;
            int bs = hb << 8 | lb; // byte swapped check, Java 16-bit "unsigned"
                                   // integer
            bs &= 0xffff;
            if (bs - crc != 0) {
                return false; // calculated CRC on the data payload didn't match
                              // CRC sent in packet
            } else {
                return true;
            }
            // In "normal" Java, this works:
            /*
             * if( ((byte)((crc & 0x0ff00) >> 8) == packet[last+2]) &&
             * ((byte)(crc & 0x00ff) == packet[last+1]) ){ return true; }else{
             * return false; }
             */
            // but always evaluates false even when the debugger shows the crc
            // and lb hb data are correct.
            // I've forced a CRC error on the first packet to verify that this
            // one doesn't always return true.
            // Now you know why I've never thought much of Java & Android.
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "Error While connecting", e);
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }
            // send BT12 set mode request command and wait for ack
            try {
                mmOutStream.write(mode, 0, mode.length);
                byte mbuf[] = new byte[modeReply.length];
                int Nread = mmInStream.read(mbuf, 0, modeReply.length);
                if (Arrays.equals(modeReply, mbuf)) {
                    // send BT12 protocol version request and wait for response
                    mmOutStream.write(protocolRequest, 0, protocolRequest.length);
                    byte pbuf[] = new byte[32];
                    Nread = mmInStream.read(pbuf);
                    if (Nread > 4) {
                        BT12version = pbuf[4];
                        switch (BT12version) {
                        case 1: // from CorScience "C" SDK header files, I've
                                // never tested!
                            BT12headerSkip = 8;
                            BT12trailerSkip = 3;
                            break;
                        case 2: // I believe we had one of these briefly for
                                // another project demo,
                        case 3: // this is what our only BT12 device is.
                            BT12headerSkip = 9;
                            BT12trailerSkip = 3;
                            break;
                        case 4: // from CorScience "C" SDK header files, I've
                                // never tested!
                            BT12headerSkip = 9;
                            BT12trailerSkip = 7;
                            break;
                        default:
                            Log.e(TAG, "Unsupported BT12 protocol version!");
                            throw new IOException("Bad Protocol!");
                        }
                    }
                }
            } catch (IOException e) {
                // should never happen unless BT12 device goes away during
                // initialization
                Log.e(TAG, "unable to initialize BT12 device", e);
                return;
            }
            // Send the name of the connected device back to the UI Activity
            Message msg = mHandler.obtainMessage(BluetoothActivity.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(BluetoothActivity.DEVICE_NAME, mmDevice.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            setState(STATE_CONNECTED);
            byte BTbuf[] = new byte[512];
            byte Pbuf[] = new byte[512];
            int Pidx = 0;
            CRCerrors = 0; // reset CRC error counter
            int Nread = 0;
            FileBytes = 0; // reset file length counter

            File ekgFolder = new File(mContext.getString(R.string.ekg_folder));
            ekgFolder.mkdirs();

            File BT12file = new File(ekgFolder, fileName);
            // We would want user set filenames here, but for now
            // recycle hard wired file for testing/development simplicity.
            OutputStream outF = null;
            PipedInputStream pipeIn = null;
            PipedOutputStream pipeOut = null;
            try {
                outF = new BufferedOutputStream(new FileOutputStream(BT12file));
                pipeOut = new PipedOutputStream();
                // int pipeSize = 3060; //requires API level 9, is that and acceptable minimum target version?
                // pipeIn = new PipedInputStream(pipeOut, pipeSize); //requires API level 9
                pipeIn = new PipedInputStream(pipeOut);
            } catch (IOException e1) {
                Log.e(TAG, "Can't open output file!", e1);
                cancel(); // Close the socket and exit thread, otherwise the app
                          // crashes instead of catching IOException
                return; // in while loop below, at least on Android 2.2 phone I
                        // have to test with.
            }
            while (mState == STATE_CONNECTED) {
                Thread.yield(); // idle here until we get a start button push
            }
            // int firstTime=1; // to force error on first packet to verify CRC
            // calculation routine
            while (mState == STATE_RECEIVING) {
                // Loop to read Bluetooth stream, find BT12 protocol packet boundaries, do ppp-like escape character decoding,
                // verify CRC16 CCITT checksum, and finally decompress the BT12
                // data to a binary file of little endian 16-bit data.
                try {
                    if (mmInStream.available() > 1 && mState == STATE_RECEIVING) {
                        Nread = mmInStream.read(BTbuf, 0, BTbuf.length);
                        // need FIFO since no guarantee BT12 packet boundaries will match Bluetooth packet boundaries
                        pipeOut.write(BTbuf, 0, Nread);
                        while (pipeIn.available() > 1 && mState == STATE_RECEIVING) {
                            // must always have at least two bytes available in case escape char is one of them!

                            while (!gotFC && mState == STATE_RECEIVING) {
                                pipeIn.read(Pbuf, 0, 1);
                                if (Pbuf[0] == BTfirst) {
                                    gotFC = true;
                                    Pidx = 1;
                                }
                            }
                            while (pipeIn.available() > 1 && !gotFD && mState == STATE_RECEIVING) {
                                pipeIn.read(Pbuf, Pidx, 1);
                                if (Pbuf[Pidx] == BTlast) {
                                    gotFD = true; // i not incremented so
                                                  // Pbuf[i]=0xFD
                                } else {
                                    if (Pbuf[Pidx] == BTescape) {
                                        pipeIn.read(Pbuf, Pidx, 1);
                                        Pbuf[Pidx] |= (byte) 0x20;
                                        // create escaped value from escape sequence, 0xFE 0xDC --> 0xFC, 0xFE 0xDD --> FD, 0xFE 0xDE --> 0xFE
                                        Pidx++; // packet buffer length is reduced by one from received data length, two input bytes became one output byte.
                                    } else {
                                        // valid payload byte at Pbuf[i]
                                        Pidx++; // advance packet buffer pointer to next location
                                    }
                                }
                            }
                            if (gotFC && gotFD) { // Pbuf now holds complete BT12 packet [0xFC ... payload bytes ... CRClow CRChigh 0xFD]
                                gotFC = false;
                                gotFD = false;
                                // if(firstTime != 0){firstTime=0; Pbuf[3]++;}
                                // //Force CRC error on first BT12 packet to
                                // verify CRC calculation.
                                if (CRC16CCITT(Pbuf, 1, Pidx - 3)) { // 0xFC start byte, 0xFD stop byte, and CRC word are not included in CRC calculation
                                    // packet checksum is correct, decompress data At this point you could byte swap the data and also turn it into short or int
                                    // for use in the plotting routines. But for now I want the "native" little endian data in the file on the phone for
                                    // testing & troubleshooting. When interpreted as a stream of little endian shorts its the same as the CardioSoft sample binary files,
                                    // except the BT12 sends [ II III V1 V2 V3 V4 V5 V6 ] instead of [ I II CR1 CR2 CR3 CR4 CR5 CR6 ] The actual format of the file to be
                                    // transmitted back to the central server is TBD, but this seems good as any.
                                    int j = 0;
                                    int k = 0;
                                    byte Fbuf[] = new byte[1024]; // because of compression in BT12 protocol data length could double
                                    // the header contains a multi-byte packet number, heart rate, and a pair of status monitoring bytes we can ignore for now
                                    // Pbuf[1] holds low order 8-bits of packet number Pbuf[2] holds low order bits of command word, 0x24 for our mode of operation.
                                    // int pulseRate = Pbuf[6]; //it might be useful to display this in the UI periodically.
                                    // int monitor1 = Pbuf[7];
                                    // int monitor2 = Pbuf[8];
                                    for (j = BT12headerSkip; j <= Pidx - BT12trailerSkip; j++) {
                                        int tmp = Pbuf[j];
                                        if ((tmp & 0x01) > 0) { // is lsb of byte set? data value was not compressed, uses two bytes in the protocol stream
                                            Fbuf[k + 1] = (byte) ((tmp >> 1) & 0x0ff);
                                            Fbuf[k] = Pbuf[++j]; // next byte is low byte
                                        } else { // not set means data is less than +/-64, takes only a single byte in the protocol stream
                                            Fbuf[k] = (byte) ((tmp >> 1) & 0x0ff);
                                            if ((tmp & 0x080) > 0)
                                                Fbuf[k + 1] = (byte) 0x0ff; // number is negative so sign extend
                                            else
                                                Fbuf[k + 1] = (byte) 0x00; // number is positive, high byte is zero
                                        }
                                        k += 2;
                                    }
                                    if (k > 0) {
                                        outF.write(Fbuf, 0, k); // byte stream of little endian 16-bit integer data
                                        FileBytes += k;

                                        if (outputstream != null) {
                                            outputstream.write(Fbuf, 0, k);
                                        }
                                    }
                                } else {
                                    // Since we didn't decode and write to file, corrupt packet data is dropped.
                                    // Because the data is "compressed" there is no way to know how many samples were dropped
                                    // so there appears to be no way to correct the timebase.
                                    // In the Windows C-code that reads the BT12 data, we've never seen CRC errors.
                                    // I believe the version 4 protocol offers an "advanced" mode with timebase fields to address the issue.
                                    CRCerrors++;
                                }
                                Pidx = 0; // start search for next packet
                            }
                            // outF.write(BTbuf, 0, Nread); // write decoded
                            // data
                        }// while(Pavailable()>1)
                    }// if(BTavailable()>1)
                } catch (IOException e) {
                    Log.e(TAG, "Exception in BT12 read loop, probably connection closed by server.", e);
                }
            }
            try {
                outF.flush();
                outF.close();
                if (outputstream != null) {
                    outputstream.flush();
                    outputstream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception flushing and closing BT12 data file on sdcard!", e);
            }
            // Send the name of the connected device back to the UI Activity
            msg = mHandler.obtainMessage(BluetoothActivity.MESSAGE_ENDING);
            bundle = new Bundle();
            bundle.putString(BluetoothActivity.DEVICE_NAME, mmDevice.getName());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
            cancel(); // close the socket
            return;
        }

        public void write(int out) {

            try {
                switch (out) {
                case BT12_START:
                    if (mState == STATE_CONNECTED) {
                        mmOutStream.write(BT12_start, 0, BT12_start.length);
                        // Send the name of the connected device back to the UI
                        // Activity
                        setState(STATE_RECEIVING);
                    }
                    break;
                case BT12_STOP:
                    if (mState == STATE_RECEIVING) {
                        mmOutStream.write(BT12_stop, 0, BT12_stop.length);
                        setState(STATE_NONE);
                    }
                    break;
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception sending start/stop command to BT12 device!", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
                setState(STATE_NONE);
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    } // class ConnectThread

} // class BluetoothCommandService