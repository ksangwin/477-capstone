package com.example.itemlists;

import java.util.HashMap;

import com.example.menus.ListContent;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class ItemList {
	
	private final static String TAG =  ItemList.class.getSimpleName();
	private ListContainer listContainer;
	// use address as key because we don't always know the nickname when looking up
	private HashMap<String, Tag> knownTags;
	private String listName;
	
	public ItemList(String name, ListContainer parent){
		listName = name;
		knownTags = new HashMap<String, Tag>();
		listContainer = parent;
	}
	
	public void addTag (BluetoothDevice dev, String nickname) {
		if(listContainer.allTags.hasNName(nickname)){
			Log.w(TAG, "This name is already taken!");
		}
		String addr = dev.getAddress();
		Tag t = new Tag(addr, dev.getName(), nickname);
		knownTags.put(addr, t);
		listContainer.allTags.addTag(dev, nickname);
	}
	
	public void removeTag (BluetoothDevice dev){	
		knownTags.remove(dev.getAddress());
	}
	
	public Tag getTag (String addr){
		 return knownTags.get(addr);
	}
	
	public String toString(){
		return listName;
	}
	
	public boolean hasNName(String name){
		for (Tag t : knownTags.values()){
			if(t.nickname.equals(name))
				return true;
		}
		return false;
	}
	
	public class Tag {
		String address;
		String name;
		String nickname;
		
		public Tag(String address, String name, String nickname){
			this.address = address;
			this.name = name;
			this.nickname = nickname;
		}
		
		public Tag(){
			this.address = "";
			this.name = "";
			this.nickname = "";
		}
	}

}
