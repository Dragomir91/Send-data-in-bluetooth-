
package com.example.tp7_bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    //static final UUID MY_UUID = UUID.fromString("98D351FDE8B9");
    private BroadcastReceiver MyReceiver = null;
    private BluetoothSocket msock = null;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private Handler handler;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter mBluetoothAdapter ;
    private BluetoothDevice ddevice;
    String[] MobileMac = {"Android","IPhone","WindowsMobile","Blackberry","WebOS","Ubuntu","Windows7","Max OS X"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter mac;
        Button bouton = (Button) findViewById(R.id.button);
        Button device_connu = (Button) findViewById(R.id.button2);
        Button Data = (Button) findViewById(R.id.button3);
        final BluetoothSocket[] sock = {null};
        String Nom_appareil = " ";
        String Mac_appareil = " "; // MAC address
        TextView etat_bluetooth = (TextView) findViewById(R.id.textView);
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.ma_liste,R.id.textView, MobileMac);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
            public void onReceive (Context context, Intent intent) {

                InputStream tmpIn = null;
                OutputStream tmpOut = null;
                mmBuffer = new byte[1024];
                int numBytes; // bytes returned from read()
                String msg = "hello m";
                Boolean state;
                int etat = 0;
                Log.i(TAG, "dans receiver");
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                if (pairedDevices.size() > 0) {
                    // There are paired devices. Get the name and address of each paired device.
                    for (BluetoothDevice device : pairedDevices) {
                        ddevice = device;
                        String deviceName = ddevice.getName();
                        String deviceHardwareAddress = ddevice.getAddress(); // MAC address
                        MobileMac[0] =  ddevice.getName() + " : " + ddevice.getAddress();
                        MobileMac[1] =  ddevice.getName() + " : " + ddevice.getAddress();
                        Log.i(TAG, "Paired");
                        ddevice = mBluetoothAdapter.getRemoteDevice(ddevice.getAddress());


                        listView.setAdapter(adapter);

                    }
                }

                //BluetoothAdapter.LeScanCallback leScanCallback = null;
                //mBluetoothAdapter.startLeScan(leScanCallback);
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.i(TAG, "Action Found");

                }
            }
        };

        bouton.setOnClickListener(new View.OnClickListener()
        {
            String appareil = " ";
            BluetoothDevice mack = null;
            @Override
            public void onClick(View v) {

                if (mBluetoothAdapter == null) {
                    Log.d("MainActivity", "Votre appareil ne possède pas de liaison Bluetooth.");
                } else if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBTIntent);
                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    registerReceiver(mBroadcastReceiver1, BTIntent);

                }
                else if (mBluetoothAdapter.isEnabled()) {
                    Log.d("MainActivity", "Désactivation BT."); mBluetoothAdapter.disable();
                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiver1, BTIntent);
                }
            }
        });

        device_connu.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String appareil = "90:7A:58:67:88:D2";
                Boolean state;
                MyBluetoothService.ConnectedThread write = new MyBluetoothService.ConnectedThread(msock);
                try {
                    msock.getOutputStream().write((int)'a');
                    Log.i(TAG, "send the caractere a");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                etat_bluetooth.setText(ddevice.getAddress());
                if (mBluetoothAdapter == null) {
                    Log.d("MainActivity", "Votre appareil ne possède pas de liaison Bluetooth.");
                }
                else if (!mBluetoothAdapter.isEnabled()) {

                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBTIntent);
                    IntentFilter BTIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver1, BTIntent);

                }
                else if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                    registerReceiver(mBroadcastReceiver1, BTIntent);

                }

            }
        });

        //public boolean setName(String name);
        Data.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v) {
                Thread1 tache = new Thread1(ddevice);

                try {
                    tache.start();
                    Log.i(TAG, "tache run");

                }
                catch (Exception e)
                {
                    Log.i(TAG, " fail thread");
                }
            }

        });

    }
    class Thread1 extends Thread{

        BluetoothSocket socket = null  ;
        Thread1(BluetoothDevice device)
        {
            try {
                msock = device.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                msock.connect();
                Log.i(TAG, "sock connect");
            } catch (IOException e) {
                try {
                    msock.close();
                }
                catch (IOException ioException)
                {
                    ioException.printStackTrace();
                }
                e.printStackTrace();
            }
            //getHandler();

        }
    }

    public Handler getHandler() {

        MyBluetoothService.ConnectedThread write = new MyBluetoothService.ConnectedThread(msock);
        write.write(100);
        return handler;
    }

    private void manageMyConnectedSocket(BluetoothSocket mmSocket) {

        MyBluetoothService.ConnectedThread service = new MyBluetoothService.ConnectedThread(mmSocket);
        service.write(100);

    }

    public static class MyBluetoothService {
        private static final String TAG = "MY_APP_DEBUG_TAG";
        private Handler handler; // handler that gets info from Bluetooth service

        // Defines several constants used when transmitting messages between the
        // service and the UI.
        private interface MessageConstants {
            public static final int MESSAGE_READ = 0;
            public static final int MESSAGE_WRITE = 1;
            public static final int MESSAGE_TOAST = 2;

            // ... (Add other message types here as needed.)
        }

        private static class ConnectedThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;
            private byte[] mmBuffer; // mmBuffer store for the stream
            private Handler handler;

            public ConnectedThread(BluetoothSocket socket) {
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the input and output streams; using temp objects because
                // member streams are final.
                try {
                    tmpIn = socket.getInputStream();
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

            public void run() {
                mmBuffer = new byte[1024];
                int numBytes; // bytes returned from read()
                // Keep listening to the InputStream until an exception occurs.
                Log.i(TAG, "run: ");


            }

            // Call this from the main activity to send data to the remote device.
            public void write(int bytes) {

                try {
                    mmOutStream.write(bytes);
                    Log.i(TAG, "write:bon ");
                    // Share the sent message with the UI activity.
                    Message writtenMsg = handler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                    writtenMsg.sendToTarget();
                }

                catch (IOException e) {
                    Log.e(TAG, "Error occurred when sending data", e);

                    // Send a failure message back to the activity.
                    Message writeErrorMsg = handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                    Bundle bundle = new Bundle();
                    bundle.putString("toast", "Couldn't send data to the other device");
                    writeErrorMsg.setData(bundle);
                    handler.sendMessage(writeErrorMsg);
                }
            }

            // Call this method from the main activity to shut down the connection.
            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the connect socket", e);
                }
            }
        }
    }



        public class  ConnectThread extends Thread {
            private  BluetoothSocket mmSocket;
            private  BluetoothDevice mmDevice;
            long msb = 123;
            long lsb = 12;
            BluetoothAdapter bluetoothAdapter;

            UUID uuid = new UUID(msb,lsb);//UUID.fromString("abcdezerty")
            public ConnectThread(BluetoothDevice device) {
                // Use a temporary object that is later assigned to mmSocket
                // because mmSocket is final.
                BluetoothSocket tmp = null;
                mmDevice = device;

                try {
                    // Get a BluetoothSocket to connect with the given BluetoothDevice.
                    // MY_UUID is the app's UUID string, also used in the server code.
                    tmp = device.createRfcommSocketToServiceRecord(uuid);
                } catch (IOException e) {
                    Log.e(TAG, "Socket's create() method failed", e);
                }
                mmSocket = tmp;
            }

            public void run() {
                // Cancel discovery because it otherwise slows down the connection.

                bluetoothAdapter.cancelDiscovery();

                try {
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    mmSocket.connect();
                } catch (IOException connectException) {
                    // Unable to connect; close the socket and return.
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) {
                        Log.e(TAG, "Could not close the client socket", closeException);
                    }
                    return;
                }

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(mmSocket);
            }

            private void manageMyConnectedSocket(BluetoothSocket mmSocket) {

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

