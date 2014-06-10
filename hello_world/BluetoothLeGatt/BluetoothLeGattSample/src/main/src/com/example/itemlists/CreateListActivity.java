package com.example.itemlists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.*;
import com.example.itemlists.ItemList.Tag;
import com.example.menus.MainActivity;

public class CreateListActivity extends ListActivity implements Callback {

	private final static String TAG = CreateListActivity.class.getSimpleName();
	public final static String EXTRAS_EDIT_MODE = "EDIT MODE";

	private boolean editMode;
	private Button finishList;
	private EditText listname;
	private String listname_s = "Untitled List";
	private ListView tagList;
	public static ArrayList<CheckHolder> checkList;
	public static CheckAdapter checkAdapter;

	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;
	private static final int REQUEST_ENABLE_BT = 1;
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		intent.getBooleanExtra(EXTRAS_EDIT_MODE, editMode);

		setContentView(R.layout.create_item_list_activity);
		getActionBar().setTitle(R.string.new_list_title);

		mHandler = new Handler();

		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
		}
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// yell at the customer
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		listname = ((EditText) findViewById(R.id.ListNameInput));

		finishList = ((Button) findViewById(R.id.saveButton));

		finishList.setOnClickListener(finishNewList);

		tagList = (ListView) findViewById(android.R.id.list);

		// I will likely need to scan and update the list as I do
		checkAdapter = new CheckAdapter(this, getCheckHolder());

		tagList.setAdapter(checkAdapter);

	}

	private ArrayList<CheckHolder> getCheckHolder() {
		checkList = new ArrayList<CheckHolder>();
		// loop through every tag we have
		
		for (Tag t : MainActivity.allLists.getAllTags()) {
			CheckHolder in = new CheckHolder(t.getDev(), t.getNname());
			checkList.add(in);
		}

		// do this when we're in edit mode to show which are already in list
		// checkList.get(1).setSelected(true);
		return checkList;
	}


	View.OnClickListener finishNewList = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.d(TAG, "Finish the list now!");

			// save the list name
			String input = listname.getText().toString();
			listname_s = input.isEmpty() ? listname_s : input;
			
			// get check box status
			ArrayList<BluetoothDevice> listTags = checkAdapter.getSelected();
			ItemList newlist = new ItemList(listname_s, MainActivity.allLists);
			
			if (listTags != null){
				Log.d(TAG, "adding tags: "+ listTags);
				
				for (BluetoothDevice d : listTags){
					newlist.addTag(d, d.getName());			
				}	
			}
			
			MainActivity.allLists.addList(listname_s, newlist);
			finish();
		}
	};

	@Override
	public boolean handleMessage(Message arg0) {
		return false;
	}

	public Context getContext() {
		return (Context) this;
	}

	/*
	 * 
	 * AAAAAAAAAAAAAAAAAAAAAAAAAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
	 * !
	 */

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!mScanning) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.actionbar_indeterminate_progress);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			checkAdapter.clear();
			scanLeDevice(true);
			break;
		case R.id.menu_stop:
			scanLeDevice(false);
			break;
		}
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		checkAdapter.clear();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Ensures Bluetooth is enabled on the device. If Bluetooth is not
		// currently enabled,
		// fire an intent to display a dialog asking the user to grant
		// permission to enable it.
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		// Initializes list view adapter.
		checkAdapter = new CheckAdapter(this, getCheckHolder());
		setListAdapter(checkAdapter);
		scanLeDevice(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();
	}

	public static void updateAdapter() {
		checkAdapter.notifyDataSetChanged();
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Tag t = MainActivity.allLists.getTag(device.getAddress());
					CheckHolder stuff;
					if( t != null){
						stuff = new CheckHolder(t.getDev(), t.getNname());						
					} else {
						stuff = new CheckHolder(device, device.getName());
					}
					checkAdapter.add(stuff);
				}
			});
		}
	};

}