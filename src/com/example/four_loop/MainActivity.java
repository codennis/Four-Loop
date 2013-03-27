package com.example.four_loop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static String DEVICENAME = "device_name";
    public static final String TOAST = "toast";
    public static final String tag = "Four-Loop";
	
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mArrayAdapter;

	private BluetoothDevice bd;
	BluetoothAdapter btInterface;
	
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    
    private static final boolean D = true;
    private BluetoothSocket socket;
    
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    
    private void connectToRobot(BluetoothDevice bd)
    {
	    try
	    {
		    socket = bd.createRfcommSocketToServiceRecord
		    (UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
		    socket.connect();
		    }
		catch (Exception e)
		{
		    Log.e(tag,"Error interacting with remote device [" + e.getMessage() +
		    "]");
	    }
    }
    
	public void findRobot()
	{
		try
		{
			// get adapter
			btInterface = BluetoothAdapter.getDefaultAdapter();
			// enable bluetooth
			// if bt is not enabled, the system will ask the user to enable.
			if (!btInterface.isEnabled()) {
			    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			// querying paired devices
			Set<BluetoothDevice> pairedDevices = btInterface.getBondedDevices();
			/*if (pairedDevices.size() > 0) {
			    // Loop through paired devices
			    for (BluetoothDevice device : pairedDevices) {
			        // Add the name and address to an array adapter to show in a ListView
			        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			    }
			}*/
			Iterator<BluetoothDevice> it = pairedDevices.iterator();
			while (it.hasNext())
			{
				bd = it.next();
				if (bd.getName().equalsIgnoreCase(DEVICENAME)) {
					connectToRobot(bd);
					return;
				}
			}
		}
		catch (Exception e)
		{
			Log.e("tag","Failed in findRobot() " + e.getMessage());
		}
	}
	
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	        }
	    }
	};

	private class AcceptThread extends Thread {
	    private final BluetoothServerSocket mmServerSocket;
	 
	    public AcceptThread() {
	        // Use a temporary object that is later assigned to mmServerSocket,
	        // because mmServerSocket is final
	        BluetoothServerSocket tmp = null;
	        try {
	            // MY_UUID is the app's UUID string, also used by the client code
	            tmp = btInterface.listenUsingRfcommWithServiceRecord(DEVICENAME, UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
	        } catch (IOException e) { }
	        mmServerSocket = tmp;
	    }
	 
	    public void run() {
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        while (true) {
	            try {
	                socket = mmServerSocket.accept();
	            } catch (IOException e) {
	                break;
	            }
	            // If a connection was accepted
	            if (socket != null) {
	                // Do work to manage the connection (in a separate thread)
	               // manageConnectedSocket(socket);
	                try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                break;
	            }
	        }
	    }
	 
	    /** Will cancel the listening socket, and cause the thread to finish */
	    public void cancel() {
	        try {
	            mmServerSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i("tag", "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case MainActivity.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(DEVICENAME);
                    mArrayAdapter.clear();
                    break;
                case MainActivity.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case MainActivity.STATE_LISTEN:
                case MainActivity.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mArrayAdapter.add(writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mArrayAdapter.add(readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                DEVICENAME = msg.getData().getString(DEVICENAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + DEVICENAME, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
	                        .sendToTarget();
	            } catch (IOException e) {
	                break;
	            }
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Keyboard button
        Button launchKeyboard = (Button) findViewById(R.id.keyboard);
        launchKeyboard.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Intent intent = new Intent(v.getContext(), KeyboardPage.class);
        		startActivityForResult(intent, 0);
        	}
        });
        
        //Media button
        Button launchMedia = (Button) findViewById(R.id.media);
        launchMedia.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Intent intent = new Intent(v.getContext(), MediaPage.class);
        		startActivityForResult(intent, 0);
        	}
        });
        
		findRobot();
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
		// enabling discoverability
		Intent discoverableIntent = new
				Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
				startActivity(discoverableIntent);
	}

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
