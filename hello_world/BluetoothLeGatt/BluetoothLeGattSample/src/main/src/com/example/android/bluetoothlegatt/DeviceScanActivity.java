/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import com.example.itemlists.ItemList;
import com.example.itemlists.NameNewTag;
import com.example.menus.MainActivity;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {
	
	private final static String TAG = DeviceScanActivity.class.getSimpleName();
	//public final static String EXTRAS_ADD_MODE = "ADD_MODE";
	public final static String EXTRAS_LIST = "LIST";
	
	private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    
    private TextView selectListDisplay;
    //Spinner search_list;
    
    
    //public boolean addMode;
    public static ItemList selectedList;
    public static String selectedList_name;
    public static BluetoothDevice selectedDev;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	Log.d(TAG, "onCreate()");
    	
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        
        Log.d(TAG, "About to get the extra from my starting intent");
        
        selectedList_name = intent.getStringExtra(EXTRAS_LIST);
        		
        Log.d(TAG, "Scanning " + selectedList_name);
                
        setContentView(R.layout.activity_scan);
        
        getActionBar().setTitle(R.string.title_devices);
        
        mHandler = new Handler();
        

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        
        selectListDisplay = (TextView) findViewById(R.id.selection);
        selectListDisplay.setText(selectedList_name + ":");
        
        Log.d(TAG, "onCreate finished");

        /*
        Log.d(TAG, "initializing the spinner");
        // making a spinner
        Spinner search_list = (Spinner) findViewById(R.id.list_dropdown);
       
        Log.d(TAG, "creating the spinner adapter");
        // Create an ArrayAdapter using the string array and a default spinner layout
        String[] listlist = MainActivity.allLists.getListNames();
        Log.d(TAG, Arrays.toString(listlist));
        
        Log.d(TAG, "creating the adapter");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listlist);
        
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        Log.d(TAG, "attatching the adapter");
        // Apply the adapter to the spinner
        search_list.setAdapter(adapter);
        
        Log.d(TAG, "attaching the callback");
        
        search_list.setOnItemSelectedListener(this);
        Log.d(TAG, "finished creating...");
        */
        
    }
    
    /*
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
    	Log.d(TAG, "thing " + pos + " was selected!");
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    	if (pos > 0){
    		String crap = parent.getItemAtPosition(pos).toString();
    		selectListDisplay.setText(crap);
    		//scanList 
    	}
    	
    }
    
    

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    	//selectListDisplay.setText("Scanning all tags");
    }
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
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } /*else if (requestCode == 1){
        	String result = data.getStringExtra("result");
        	selectedList.addTag(selectedDev, result);
        	finish();
        } */
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
    	selectedDev = mLeDeviceListAdapter.getDevice(position);
    	
        if (selectedDev == null) return;
        
        
        // if we're not inspecting items, then we just want to return to the new list page
        if (true){
        	final Intent intent = new Intent(this, DeviceControlActivity.class);
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, selectedDev.getName());
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, selectedDev.getAddress());
            if (mScanning) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mScanning = false;
            }
            startActivity(intent);
            
        } /*else {
        	// check if it's already named
        	String nname = MainActivity.allLists.getNName(selectedDev);
        	
        	if(nname.isEmpty()){
        		Log.d(TAG, "no existing entry!");
        		startActivityForResult(new Intent(this, NameNewTag.class), 1);
        		finish();
        	} else {
        		Log.d(TAG, "Adding " + nname);
        	}
        	
        	selectedList.addTag(selectedDev, nname);
        	finish();
        }*/
        
    }
    
    public void updateAdapter(){
    	mLeDeviceListAdapter.notifyDataSetChanged();
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

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            // TODO: add code to display not-found items in red
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }
    
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	Log.d(TAG, "running our scan");
                	/* Don't want to display all devices --only the ones specified in the list 
                	 * For now, I'll set that to "Testing Tag", but later I'll use something like this
                	 * if (searchList.containsAddress(device.getAddress())) { add device }
                	 */
                	//if (device.getName().equals("Testing Tag")){
                	//	Log.d(TAG, "we have one of our tags!");
                		mLeDeviceListAdapter.addDevice(device);
                        mLeDeviceListAdapter.notifyDataSetChanged();
                	//} else {
                	//	Log.d(TAG, "I don't know what we're reading...");
                	//}
                }
            });
        }
    };


}