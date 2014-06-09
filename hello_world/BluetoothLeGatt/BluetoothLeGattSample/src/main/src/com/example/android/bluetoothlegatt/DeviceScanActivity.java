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
import android.os.CountDownTimer;
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
import java.util.Timer;
import java.util.TimerTask;

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
	
	private int num_ave = 5; // Use indicate number of averages to do for RSSI values
	private static int maxInterval = 2000; // countdown in milliseconds for each timer
	
	private LeDeviceListAdapter LeAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    
    private TextView selectListDisplay;
    
    public int closeRange = -95;
    public int minRange = -120; // the min RSSI val
    public int outOfRange = -200;
    
    public boolean showAll;
    public static ItemList selectedList;
    public static String selectedList_name;
    public static DevEntry selectedDev;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 100000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        
        selectedList_name = intent.getStringExtra(EXTRAS_LIST);
        selectedList = MainActivity.allLists.getList(selectedList_name);
        showAll = selectedList_name.equals("Show All");
        
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
        
        //Log.i(TAG, "here are all the tags: " + Arrays.toString(MainActivity.allLists.getTags()));
        //Log.i(TAG, "here are my tags: " + Arrays.toString(selectedList.getTags()));

        // Initializes list view adapter.
        LeAdapter = new LeDeviceListAdapter();
        setListAdapter(LeAdapter);
        
        // load up the adapter
        if (!selectedList.isEmpty()){
        	
        	for (Tag t : selectedList.getTags()){
            	LeAdapter.addDevice(t.getDev());            	
            }
        	LeAdapter.notifyDataSetChanged();
        }
        Log.d(TAG, "LeAdapter on resume: " + LeAdapter.toString());
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
        } 
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //final BluetoothDevice device = LeAdapter.getDevice(position);
    	selectedDev = (DevEntry)LeAdapter.getItem(position);
    	
        if (selectedDev == null) return;
        
        
        // if we're not inspecting items, then we just want to return to the new list page
        if (true){
        	final Intent intent = new Intent(this, DeviceControlActivity.class);
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, selectedDev.getName());
            intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, selectedDev.device.getAddress());
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
        private ArrayList<DevEntry> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<DevEntry>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!hasDevice(device)) {
                mLeDevices.add(new DevEntry(device));
            }
        }
        
        public void setRSSI(int rssi, int index) {
        	DevEntry cur = mLeDevices.get(index);
        	cur.setRSSI(rssi);
        }
        
        public int indexOf(BluetoothDevice dev){
        	for(int i = 0; i < mLeDevices.size(); i++) {
        		DevEntry de = mLeDevices.get(i); 
        		if(de.device.equals(dev)){
        			return i;
        		}
        	}
        	return -1;
        }
        
        public boolean hasDevice(BluetoothDevice dev){
        	return indexOf(dev) >= 0;
        }
        
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position).device;
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
        	String allDevs = "{ ";
        	for(DevEntry d : mLeDevices){
        		allDevs = allDevs + d.device.getName();
        	}
        	return allDevs + " }";
        }
        
        public int getRSSIcolor(int rssi){
        	if(rssi == outOfRange){
        		return R.color.myRed;
        	} else if (rssi > closeRange){
        		return R.color.myGreen;
        	} else {
        		return R.color.myYellow;
        	}
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
             
            
            Log.d(TAG, "trying to get a list view..");
            // General ListView optimization code.
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
            
            DevEntry de = (DevEntry)getItem(i);
            int rssi = de.curRSSI;
            final String deviceName = de.getName();
            
            
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            
            viewHolder.deviceAddress.setText(de.device.getAddress());
            viewHolder.deviceRSSI.setText(String.valueOf(rssi));

            view.setBackgroundResource(getRSSIcolor(rssi));
            
            return view;
        }
    }
    
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRSSI;
    }
    
    private class DevEntry {
        BluetoothDevice device;
        String nname;
        int curRSSI;
        int runningSum;
        int tally;
        int interval;
        int numRSSIupdates;
        @SuppressWarnings("unused")
		CountDownTimer timer;
        boolean dead;
        
        
        public DevEntry(BluetoothDevice d){
        	device = d;
        	nname = null;
        	curRSSI = outOfRange;
        	runningSum = 0;
        	tally = 0;
        	interval = maxInterval;
        	dead = false;
        	startTimer();
        }
        
        public void setRSSI(int rssi){
        	// if we've grabbed enough counts to average them
        	if(tally >= num_ave) {
        		curRSSI = (int)((float) runningSum / (float)num_ave);
        		runningSum = curRSSI;
        		tally = 0;
        	} else {
        		runningSum += rssi;
        		tally += 1;
        		//Log.d(TAG, "adding to the running total (" + tally + "th time)");
        	}
        	
        	//Log.d(TAG, "Entry RSSI updated to: " + toString());

        	if(dead){
        		startTimer();
        		dead = false;
        	}
        	
        	numRSSIupdates++;
        	
        	//Log.i(TAG, "set RSSI " + numRSSIupdates + " since timer");        	
            
        }
        
        private void startTimer(){
        	numRSSIupdates = 0;
        	timer = new CountDownTimer(1000, interval) {

        	     public void onTick(long millisUntilFinished) { }

        	     public void onFinish() {
        	         if((numRSSIupdates == 0) && mScanning){     	        	 
        	        	 // we're dead
        	        	 Log.i(TAG, "Tag went out of range!");
        	        	 curRSSI = outOfRange;
        	        	 dead = true;
        	        	 updateAdapter();
        	         } else {
        	        	 // start counting again
        	        	 numRSSIupdates = 0;
        	        	 Log.i(TAG, "Tag was active over last 5 seconds. Good... good...");
        	        	 this.start(); // start up this timer again
        	         }
        	     }
        	  }.start();

        }
        
        /**
         * This can be used to update the nname field inside of this device entry
         * The device entry should never have a reason to change the nname and it contains
         * no tag itself, so it is necessary to retrieve the nname from a tag. 
         */
        public String getNName(){
        	// attempt to get tag (it may not be registered with us yet)
        	Tag instance = MainActivity.allLists.getTag(device.getAddress());
        	
        	if (instance == null){
        		return null;
        	}
        	
        	String n = instance.getNname();
        	nname = n;
        	return n;
        }
        
        public String getName(){
        	getNName();
        	return (nname != null) ? nname : device.getName();
        }
        
        public String toString(){
        	return device.getName() + ": RSSI=" + curRSSI + " (" + runningSum + "/" + tally + ")"; 
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                	// we won't add devices to the adapter here when scanning other lists
                	// their tags will display constantly, rather than appear when visible
                	if (showAll){
                		LeAdapter.addDevice(device);
                        LeAdapter.notifyDataSetChanged();
                	}
                	
                	// it's ok to do this regardless of the list we're scanning though
                	int toUpdate = LeAdapter.indexOf(device);
                	if(toUpdate >= 0){
                		Log.i(TAG, "updating RSSI of " + device.getName());
                		LeAdapter.setRSSI(rssi, toUpdate);
                		LeAdapter.notifyDataSetChanged();
                	} else {
                		Log.w(TAG, "Did not update RSSI!");
                	}

                }
            });
        }
    };

}