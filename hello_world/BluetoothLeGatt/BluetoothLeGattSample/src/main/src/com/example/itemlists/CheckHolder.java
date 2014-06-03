package com.example.itemlists;

import android.bluetooth.BluetoothDevice;

public class CheckHolder {
	private BluetoothDevice dev;
	private String address;
	private String name;
	public boolean selected;

	  
	public CheckHolder(BluetoothDevice d) {
		dev = d;
		this.address = d.getAddress();
		this.name = d.getName();
		selected = false;
	}

	public String getName() {
		return name;
	}
	
	public String getAddr(){
		return address;
	}
	
	public BluetoothDevice getDev(){
		return dev;
	}

} 
