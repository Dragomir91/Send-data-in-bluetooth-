package com.example.tp7_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class Client {
    public static class  ConnectThread extends Thread {
        private  BluetoothSocket mmSocket;
        private  BluetoothDevice mmDevice;
        private Thread ConnectedThread;
        private  BluetoothManager manager;
        private Handler handler;

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                Log.i(TAG, "uuid ok");
                tmp = device.createRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            Log.i(TAG, "inside run: ");
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.i(TAG, "mmSocket.connect inside");
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                    Log.i(TAG, "socket close");
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.

            //manager.getAdapter();
            manageMyConnectedSocket(mmSocket);
        }

        private void manageMyConnectedSocket(BluetoothSocket mmSocket) {

           ConnectedThread = new ConnectedThread(mmSocket);
           ConnectedThread.start();

        }
        private interface MessageConstants {
            public static final int MESSAGE_READ = 0;
            public static final int MESSAGE_WRITE = 1;
            public static final int MESSAGE_TOAST = 2;

            // ... (Add other message types here as needed.)
        }
        class ConnectedThread extends Thread {
            BluetoothSocket Socket;
            private  BluetoothSocket mmSocket;
            private  InputStream mmInStream;
            private  OutputStream mmOutStream;
            private byte[] mmBuffer; // mmBuffer store for the stream

            ConnectedThread(BluetoothSocket socket) {

                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;
                Log.i(TAG, "ConnectedThread:service ");
                // Get the input and output streams; using temp objects because
                // member streams are final.
                try {
                    tmpIn = mmSocket.getInputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating input stream", e);
                }
                try {
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating output stream", e);
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }
            //MainActivity.MyBluetoothService service = new MainActivity.MyBluetoothService();
            public void run() {

                mmBuffer = new byte[1024];
                int numBytes; // bytes returned from read()
                write(mmBuffer);
                /*while (true) {
                    try {
                        // Read from the InputStream.
                        numBytes = mmInStream.read(mmBuffer);
                        // Send the obtained bytes to the UI activity.
                        Message readMsg = handler.obtainMessage(MessageConstants.MESSAGE_READ, numBytes, -1, mmBuffer);
                        readMsg.sendToTarget();
                    } catch (IOException e) {
                        Log.d(TAG, "Input stream was disconnected", e);
                        break;
                    }
                }*/

            }

            // Call this from the main activity to send data to the remote device.
            public void write(byte[] bytes) {
                try {
                    mmOutStream.write(bytes);

                    // Share the sent message with the UI activity.
                    Message writtenMsg = handler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                    writtenMsg.sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when sending data", e);

                    // Send a failure message back to the activity.
                    Message writeErrorMsg = handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString("toast", "Couldn't send data to the other device");
                    writeErrorMsg.setData(bundle);
                    handler.sendMessage(writeErrorMsg);
                }
            }
        }



        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
}
