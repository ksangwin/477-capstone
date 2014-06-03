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
import android.graphics.Color;
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
import com.example.itemlists.ItemList.Tag;
import com.example.menus.MainActivity;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class DeviceScanActivity extends ListActivity {
	
	private final static String TAG = DeviceScanActivity.class.getSimpleName();
	//public final static String EXTRAS_ADD_MODE = "ADD_MODE";
	public final static String EXTRAS_LIST = "LIST";
	
	private LeDeviceListAdapter LeAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    
    private TextView selectListDisplay;
    //Spinner search_list;
    
    public int closeRange = -80;
    public int minRange = -120; // the min
    
    public boolean showAll;
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
        selectedList = MainActivity.allLists.getList(selectedList_name);
        showAll = selectedList_name.equals("Show All");
        		
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

    }


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
                LeAdapter.clear();
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
        LeAdapter = new LeDeviceListAdapter();
        setListAdapter(LeAdapter);
        
        // load up the adapter
        if (!selectedList.isEmpty()){
        	int i = 0;
        	for (Tag t : selectedList.getTags()){
            	LeAdapter.addDevice(t.getDev());
            	// make sure that RSSI is added at the same index as the device
            	LeAdapter.setRSSI(minRange, LeAdapter.mLeDevices.indexOf(t.getDev()));
            	i++;
            }
        	LeAdapter.notifyDataSetChanged();
        }
        Log.d(TAG, "LeAdapter: " + LeAdapter.toString());
        scanLeDevice(true);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        LeAdapter.clear();
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //final BluetoothDevice device = LeAdapter.getDevice(position);
    	selectedDev = LeAdapter.getDevice(position);
    	
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
            
        }         
    }
    
    public void updateAdapter(){
    	LeAdapter.notifyDataSetChanged();
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
        private ArrayList<Integer> mRSSI;
        private int count; // Use as an iterator of RSSI values

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
            mRSSI = new ArrayList<Integer>();
            count = 0;
        }

        public void addDevice(BluetoothDevice device) {
        	Log.d(TAG, "adding " + device.getName() + " to our scan list adapter");
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                count++;
            }
        }
        public void setRSSI(int rssi, int index) {
        	if(mRSSI.size() != 0 && mRSSI.size() > index) {
        		mRSSI.remove(index);
        	}
        	mRSSI.add(index,rssi);
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
        
        public String toString(){
        	String allDevs = "My BLE devices: ";
        	for(BluetoothDevice d : mLeDevices){
        		allDevs = allDevs + d.getName();
        	}
        	return allDevs;
        }
        
        public int setRSSIcolor(int rssi){
        	if(rssi == 0){
        		return Color.WHITE;
        	} else if (rssi > closeRange){
        		return Color.GREEN;
        	} else {
        		return Color.YELLOW;
        	}
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            
            Log.d(TAG, "trying to get a list view..");
            // General ListView optimization code.
            // TODO: add code to display not-found items in red
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.deviceRSSI = (TextView) view.findViewById(R.id.device_rssi);
                
                //view.setBackgroundColor(setRSSIcolor());
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            
            
            Log.d(TAG, "LeAdapter: " + this.toString());
            Log.d(TAG, "grabbing a device " + i + " out of the adapter so we can print it");
            
            BluetoothDevice device = mLeDevices.get(i);
            int rssi = mRSSI.get(i);
            final String deviceName = device.getName();
            
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            
            viewHolder.deviceAddress.setText(device.getAddress());
            viewHolder.deviceRSSI.setText(String.valueOf(rssi));

            Log.d(TAG, "displaying tag " + deviceName + " (now in COLOR!)");
            
            view.setBackgroundColor(setRSSIcolor(rssi));
            
            return view;
        }
    }
    
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRSSI;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	Log.d(TAG, "running our scan");
                	
                	// we won't add devices to the adapter here when scanning other lists
                	// their tags will display constantly, rather than appear when visible
                	if (showAll){
                		Log.d(TAG, "adding a device because ShowAll");
                		LeAdapter.addDevice(device);
                        LeAdapter.notifyDataSetChanged();
                	}
                	
                	// it's ok to do this regardless of the list we're scanning though
                	
                	int toUpdate = LeAdapter.mLeDevices.indexOf(device);                	
                	LeAdapter.setRSSI(rssi, toUpdate);
                	LeAdapter.notifyDataSetChanged();


                    
                }
            });
        }
    };


}