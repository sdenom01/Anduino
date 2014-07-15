package com.example.anduino;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorSelectedListener;
import com.larswerkman.holocolorpicker.ValueBar;
import com.larswerkman.holocolorpicker.ValueBar.OnValueChangedListener;

public class MainActivity extends Activity {

	@SuppressWarnings("unused")
	private Button On, Off, Visible, list, redOn, redOff, greenOn, greenOff,
			blueOn, blueOff;
	private ListView lv;
	private TextView tvColorDisplay;

	@SuppressWarnings("unused")
	private LinearLayout llBluetooth, llColorWheel;
	private Switch toggleBT;
	private ColorPicker picker;
	private ValueBar valueBar;

	private BluetoothAdapter btAdapter;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private Set<BluetoothDevice> pairedDevices;

	private Boolean listToggle = false;

	// MAC-address of Bluetooth module
	private static String address;
	private static String TAG = "Debug";

	// SPP UUID service
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		On = (Button) findViewById(R.id.button1);
		Off = (Button) findViewById(R.id.button2);
		Visible = (Button) findViewById(R.id.button3);
		list = (Button) findViewById(R.id.button4);

		tvColorDisplay = (TextView) findViewById(R.id.tvColorDisplay);

		redOn = (Button) findViewById(R.id.btRedOn);
		redOn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendData("1");
			}

		});

		redOff = (Button) findViewById(R.id.btRedOff);
		redOff.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendData("0");
			}

		});

		greenOn = (Button) findViewById(R.id.btGreenOn);
		greenOn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendData("2");
			}

		});

		greenOff = (Button) findViewById(R.id.btGreenOff);
		greenOff.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendData("3");
			}

		});

		blueOn = (Button) findViewById(R.id.btBlueOn);
		blueOn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendData("4");
			}

		});

		blueOff = (Button) findViewById(R.id.btBlueOff);
		blueOff.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendData("5");
			}

		});

		llBluetooth = (LinearLayout) findViewById(R.id.bluetoothInformation);
		llColorWheel = (LinearLayout) findViewById(R.id.colorPicker);

		toggleBT = (Switch) findViewById(R.id.bluetoothToggle);
		toggleBT.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked)
					llBluetooth.setVisibility(View.VISIBLE);
				else
					llBluetooth.setVisibility(View.GONE);
			}
		});

		lv = (ListView) findViewById(R.id.listView1);

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		checkBTState();

		picker = (ColorPicker) findViewById(R.id.picker);

		valueBar = (ValueBar) findViewById(R.id.valuebar);

		picker.addValueBar(valueBar);

		// To get the color
		picker.getColor();

		tvColorDisplay.setText(String.format("#%06X",
				(0xFFFFFF & picker.getColor())));

		// To set the old selected color u can do it like this
		picker.setOldCenterColor(picker.getColor());

		// Happens every 'tick' the picker is being dragged,
		// eventually I want to send each of these values
		picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
			@Override
			public void onColorChanged(int color) {
				String hexColor = String.format("#%06X", (0xFFFFFF & color));

				tvColorDisplay.setText(hexColor);

				sendToArduino(hexColor);
			}
		});

		// Only time to send the hexcolor right now
		// Eventually send OnColorChanged
		picker.setOnColorSelectedListener(new OnColorSelectedListener() {
			@Override
			public void onColorSelected(int color) {
				picker.setOldCenterColor(picker.getColor());
			}

		});

		valueBar.setOnValueChangedListener(new OnValueChangedListener() {

			@Override
			public void onValueChanged(int value) {
				picker.setColor(value);
				picker.setOldCenterColor(picker.getColor());
			}

		});
	}

	public void on(View view) {
		if (!btAdapter.isEnabled()) {
			Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(turnOn, 0);
			Toast.makeText(getApplicationContext(), "Turned on",
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(), "Already on",
					Toast.LENGTH_LONG).show();
		}
	}

	public void list(View view) {
		pairedDevices = btAdapter.getBondedDevices();

		ArrayList<String> list = new ArrayList<String>();
		for (BluetoothDevice bt : pairedDevices)
			list.add(bt.getName());

		listToggle = !listToggle;

		if (listToggle) {
			view.setBackgroundColor(Color.RED);
			lv.setVisibility(View.VISIBLE);
		} else {
			view.setBackgroundColor(Color.parseColor("#C2C2C2"));
			lv.setVisibility(View.INVISIBLE);
		}

		Toast.makeText(getApplicationContext(), "Showing Paired Devices",
				Toast.LENGTH_SHORT).show();
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView tvValue = (TextView) view
						.findViewById(android.R.id.text1);
				Log.d("", tvValue.getText().toString());

				Toast.makeText(
						getBaseContext(),
						"Attempting Connection to: "
								+ tvValue.getText().toString(),
						Toast.LENGTH_SHORT).show();

				for (BluetoothDevice bt : pairedDevices)
					if (tvValue.getText().toString().equals(bt.getName())) {
						address = bt.getAddress();

						Log.d("", address);

						AttemptConnection();
					}

			}
		});

	}

	public void off(View view) {
		btAdapter.disable();
		Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG)
				.show();
	}

	public void visible(View view) {
		Intent getVisible = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		startActivityForResult(getVisible, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return true;
	}

	private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
			throws IOException {
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod(
						"createInsecureRfcommSocketToServiceRecord",
						new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, MY_UUID);
			} catch (Exception e) {
				Log.e("", "Could not create Insecure RFComm Connection", e);
			}
		}
		return device.createRfcommSocketToServiceRecord(MY_UUID);
	}

	public void AttemptConnection() {
		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.

		try {
			btSocket = createBluetoothSocket(device);
		} catch (IOException e1) {
			errorExit(
					"Fatal Error",
					"In AttemptConnection() and socket create failed: "
							+ e1.getMessage() + ".");
		}

		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection. This will block until it connects.
		Log.d(TAG, "...Connecting...");
		try {
			btSocket.connect();
			Log.d(TAG, "...Connection ok...");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				errorExit("Fatal Error",
						"In AttemptConnection() and unable to close socket during connection failure"
								+ e2.getMessage() + ".");
			}
		}

		// Create a data stream so we can talk to server.
		Log.d(TAG, "...Create Socket...");

		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			errorExit("Fatal Error",
					"In AttemptConnection() and output stream creation failed:"
							+ e.getMessage() + ".");
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		Log.d(TAG, "...In onPause()...");

		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
				errorExit(
						"Fatal Error",
						"In onPause() and failed to flush output stream: "
								+ e.getMessage() + ".");
			}
		}

		if (btSocket != null)
			try {
				btSocket.close();
			} catch (IOException e2) {
				errorExit(
						"Fatal Error",
						"In onPause() and failed to close socket."
								+ e2.getMessage() + ".");
			}
	}

	private void checkBTState() {
		// Check for Bluetooth support and then check to make sure it is turned
		// on
		// Emulator doesn't support Bluetooth and will return null
		if (btAdapter == null) {
			errorExit("Fatal Error", "Bluetooth not support");
		} else {
			if (btAdapter.isEnabled()) {
				Log.d(TAG, "...Bluetooth ON...");
			} else {
				// Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 1);
			}
		}
	}

	private void errorExit(String title, String message) {
		Toast.makeText(getBaseContext(), title + " - " + message,
				Toast.LENGTH_LONG).show();
		finish();
	}

	private void sendData(String message) {
		byte[] msgBuffer = message.getBytes();

		Log.d(TAG, "...Send data: " + message + "...");

		try {
			if (outStream != null)
				outStream.write(msgBuffer);
			else
				Log.d("", "Failed to get outStream.");
		} catch (IOException e) {
			String msg = "In onResume() and an exception occurred during write: "
					+ e.getMessage();
			if (address.equals("00:00:00:00:00:00"))
				msg = msg
						+ ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
			msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString()
					+ " exists on server.\n\n";

			errorExit("Fatal Error", msg);
		}
	}

	// sends color data to a Serial device as #000000
	private void sendToArduino(String message) {
		byte[] msgBuffer = message.getBytes();

		// send the color to the serial device
		if (outStream != null) {
			try {
				outStream.write(msgBuffer);
				Thread.sleep(25); // Sleep is required for Arduino baud rate
			} catch (IOException e) {
				Log.e(TAG, "couldn't write color bytes to serial device");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			Log.d(TAG, "No device connected.");
		}
	}
}
