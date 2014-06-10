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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
//import android.widget.ExpandableListView;
//import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.example.itemlists.ItemList.Tag;
import com.example.menus.MainActivity;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    //private ExpandableListView mGattServicesList;
    private Button mGattServiceLight;
    private Button mGattServiceBuzzer;
    private SeekBar mGattServicePower;
    private ProgressBar mGattServiceDistance;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothAdapter mBluetoothAdapter;
    // RSSI buffer is used to averaging RSSI value
    //private List<Integer> mDistance_buffer = new ArrayList<Integer>();
    private int prev_distance;
    private int num_ave = 5;
    private int count = 0;
    //private BluetoothGattCharacteristic mNotifyCharacteristic;
    private EditText tagName;
    private Tag current;
    private boolean canNameTag;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            //mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
        
    };

    private final Button.OnClickListener lightOnClickListener = 
    		new Button.OnClickListener(){

				@Override
				public void onClick(View v) {
					final BluetoothGattCharacteristic characteristic =
                            mGattCharacteristics.get(2).get(1);
                	byte[] data_w = new byte[1];
                	byte[] data_r = characteristic.getValue();
                	if(data_r == null)
                	{
                		data_w[0] = 8;
                	}
                	else if((data_r[0] & 8) == 0)
                	{	
                		data_w[0] = (byte) (data_w[0] | 8);
                	}
                	else
                	{
                		data_w[0] = (byte) (data_w[0] & 7);
                	}
                	
                    // Give enough time for value to be written on characteristic
                	boolean set_success = characteristic.setValue(data_w);
                	while(!set_success)
                	{
                		set_success = characteristic.setValue(data_w);
                	}
                	mBluetoothLeService.writeCharacteristic(characteristic);
				}
    	
    	
    };
    
    private final Button.OnClickListener buzzerOnClickListener = 
    		new Button.OnClickListener(){

				@Override
				public void onClick(View v) {
					final BluetoothGattCharacteristic characteristic =
                            mGattCharacteristics.get(2).get(1);
                	byte[] data_w = new byte[1];
                	byte[] data_r = characteristic.getValue();
                	if(data_r == null)
                	{
                		data_w[0] = 1;
                	}
                	else if((data_r[0] & 1) == 0)
                	{	
                		data_w[0] = (byte) (data_w[0] | 1);
                	}
                	else
                	{
                        data_w[0] = (byte) (data_w[0] & 14);
                	}
                	
                    // Give enough time for value to be written on characteristic
                	boolean set_success = characteristic.setValue(data_w);
                	while(!set_success)
                	{
                		set_success = characteristic.setValue(data_w);
                	}
                	mBluetoothLeService.writeCharacteristic(characteristic);
					
				}
    	
    };
    
    private final SeekBar.OnSeekBarChangeListener powerOnSeekBarChangeListener =
    		new SeekBar.OnSeekBarChangeListener(){

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					final BluetoothGattCharacteristic characteristic =
                            mGattCharacteristics.get(2).get(1);
                	byte[] data_w = new byte[1];
                	byte[] data_r = characteristic.getValue();
                	byte[] value_w = new byte[1];
                	if(progress < 35) // Set value write based on progress
                	{
                		value_w[0] = 2;
                	}
                	else if(progress >= 35 && progress < 70)
                	{
                		value_w[0] = 4;
                	}
                	else
                	{
                		value_w[0] = 6;
                	}
                	
                	if(data_r == null)
                	{
                		data_w[0] = value_w[0];
                	}
                	else
                	{	
                		data_r[0] = (byte) (data_r[0] & 9);
                		value_w[0] = (byte) (value_w[0] | data_r[0]);
                		data_w[0] = value_w[0];
                	}
                	
                	
                    // Give enough time for value to be written on characteristic
                	boolean set_success = characteristic.setValue(data_w);
                	while(!set_success)
                	{
                		set_success = characteristic.setValue(data_w);
                	}
                	mBluetoothLeService.writeCharacteristic(characteristic);
					
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					
					
				}
    	
    };
    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
//    private final ExpandableListView.OnChildClickListener servicesListClickListner =
//            new ExpandableListView.OnChildClickListener() {
//                @Override
//                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
//                                            int childPosition, long id) {
//                    if (mGattCharacteristics != null) {
//                        final BluetoothGattCharacteristic characteristic =
//                                mGattCharacteristics.get(groupPosition).get(childPosition);
//                        final int charaProp = characteristic.getProperties();
//                        if((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) // If it can notify
//                        {
//                        	//mBluetoothLeService.setCharacteristicNotification()
//                        }
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                            // If there is an active notification on a characteristic, clear
//                            // it first so it doesn't update the data field on the user interface.
//                            if (mNotifyCharacteristic != null) {
//                                mBluetoothLeService.setCharacteristicNotification(
//                                        mNotifyCharacteristic, false);
//                                mNotifyCharacteristic = null;
//                            }
//                            mBluetoothLeService.readCharacteristic(characteristic);
//                        }
//                        UUID LED_write = UUID.fromString(SampleGattAttributes.LED_WRITE);
//                        if((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 && LED_write.equals(characteristic.getUuid())) {
//                        	// Give enough time to for readCharactristic call back to respond
//                        	while(characteristic.getValue() == null)
//                        	{
//                        	}
//                        	byte[] data_w = new byte[1];
//                        	byte[] data_r = characteristic.getValue();
//                        	if(data_r[0] == 1)
//                        	{	
//                        		data_w[0] = 3;
//                        	}
//                        	else
//                        	{
//                                data_w[0] = 1;
//                        	}
//                        	
//                            // Give enough time for value to be written on characteristic
//                        	boolean set_success = characteristic.setValue(data_w);
//                        	while(!set_success)
//                        	{
//                        		set_success = characteristic.setValue(data_w);
//                        	}
////                            for (int i = 0; i < 100000; i++)
////                            {
////                            	characteristic.setValue(data_w);
////                            }
//                        	mBluetoothLeService.writeCharacteristic(characteristic);
//                        }
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                            mNotifyCharacteristic = characteristic;
//                            mBluetoothLeService.setCharacteristicNotification(
//                                    characteristic, true);
//                        }
//                        return true;
//                    }
//                    return false;
//                }
//    };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        //mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        //mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        mGattServiceLight = (Button) findViewById(R.id.light_i);
        mGattServiceBuzzer = (Button) findViewById(R.id.buzzer_i);
        mGattServicePower = (SeekBar) findViewById(R.id.power_mode);
        mGattServiceDistance = (ProgressBar) findViewById(R.id.distance_i);
        mGattServiceLight.setOnClickListener(lightOnClickListener);
        mGattServiceBuzzer.setOnClickListener(buzzerOnClickListener);
        mGattServicePower.setOnSeekBarChangeListener(powerOnSeekBarChangeListener);
        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        
        current = MainActivity.allLists.getTag(mDeviceAddress); // pick a tag, any tag
        tagName = (EditText) findViewById(R.id.tagName);
        tagName.setHint(mDeviceName);
        if (current == null){
        	canNameTag = false;
        	tagName.setKeyListener(null);
        } else {
        	canNameTag = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        Log.d(TAG, "about to set tag name...");
        
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        
        
        if(canNameTag){
        	String temp = tagName.getText().toString();
            current.setNname(temp);
            Log.d(TAG, "the new tag: " + current.toString());
        } else {
        	Log.w(TAG, "Cannot name a tag that is not in a list");
        }
         
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	mConnectionState.setText(resourceId);
            }
        });
    }
    
    

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }
    

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

//        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
//                this,
//                gattServiceData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[] {LIST_NAME, LIST_UUID},
//                new int[] { android.R.id.text1, android.R.id.text2 },
//                gattCharacteristicData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[] {LIST_NAME, LIST_UUID},
//                new int[] { android.R.id.text1, android.R.id.text2 }
//        );
        //mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	int distance;
                	int base = 60; // this is the max rssi.. (which will appear as a min)
                	double scaler = base/100.0; // this is to normalize from 120 to 100
                	//int rssi;
                	
                	if(mBluetoothLeService != null)
                	{
	                	mBluetoothLeService.readRemoteRssi();

	                	if(prev_distance == 0)
	                		prev_distance = Math.abs(rssi);
	                	else
	                		prev_distance += (int)(Math.abs(rssi) * scaler);

	                	
	                	count++;
	                	if(count % num_ave == 0)
	                	{
	                		distance = (int)((float)prev_distance/(float)num_ave);
	                		Log.i(TAG, "after averaging, progress bar is " + distance);
	                		mGattServiceDistance.setProgress(distance);
	                		count = 0;
	                		prev_distance = 0;
	                	}
                	}
                }
            });
        }
    };
}