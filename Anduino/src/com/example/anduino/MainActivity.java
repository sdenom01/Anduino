package com.example.anduino;

import java.util.ArrayList;
import java.util.Set;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {

	@SuppressWarnings("unused")
	private Button On, Off, Visible, list;
	private BluetoothAdapter BA;
	private Set<BluetoothDevice> pairedDevices;
	private ListView lv;
	private LinearLayout llBluetooth, llColorWheel;
	private Switch toggleBT;
	private ColorPicker picker;
	private SVBar svBar;
	private OpacityBar opacityBar;
	private SaturationBar saturationBar;
	private ValueBar valueBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		On = (Button) findViewById(R.id.button1);
		Off = (Button) findViewById(R.id.button2);
		Visible = (Button) findViewById(R.id.button3);
		list = (Button) findViewById(R.id.button4);

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

		BA = BluetoothAdapter.getDefaultAdapter();

		picker = (ColorPicker) findViewById(R.id.picker);
		svBar = (SVBar) findViewById(R.id.svbar);
		opacityBar = (OpacityBar) findViewById(R.id.opacitybar);
		saturationBar = (SaturationBar) findViewById(R.id.saturationbar);
		valueBar = (ValueBar) findViewById(R.id.valuebar);

		picker.addSVBar(svBar);
		picker.addOpacityBar(opacityBar);
		picker.addSaturationBar(saturationBar);
		picker.addValueBar(valueBar);

		// To get the color
		picker.getColor();

		// To set the old selected color u can do it like this
		picker.setOldCenterColor(picker.getColor());
		// adds listener to the colorpicker which is implemented
		// in the activity
		picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener()
	    {
	        @Override
	        public void onColorChanged(int color) {

	        }
	    });

		// to turn of showing the old color
		// picker.setShowOldCenterColor(false);

		// adding onChangeListeners to bars
		// opacitybar.setOnOpacityChangeListener(new OnOpacityChangeListener …)
		// valuebar.setOnValueChangeListener(new OnValueChangeListener …)
		// saturationBar.setOnSaturationChangeListener(new
		// OnSaturationChangeListener …)
	}

	public void on(View view) {
		if (!BA.isEnabled()) {
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
		pairedDevices = BA.getBondedDevices();

		ArrayList<String> list = new ArrayList<String>();
		for (BluetoothDevice bt : pairedDevices)
			list.add(bt.getName());

		Toast.makeText(getApplicationContext(), "Showing Paired Devices",
				Toast.LENGTH_SHORT).show();
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		lv.setAdapter(adapter);

	}

	public void off(View view) {
		BA.disable();
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
}
