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
		
		Tag t = new Tag(dev, nickname);
		knownTags.put(dev.getAddress(), t);
		if(!listName.equals("all")){
			// yeeeaaaahhh. Add to the universal list for bookeeping
			// but don't have the universal list add it again... and again... and again...
			listContainer.allTags.addTag(dev, nickname);
		}
	}
	
	public void addTag (Tag t) {
		knownTags.put(t.dev.getAddress(), t);
		// we want this info in case we remove tag from this lists
		if(!listName.equals("all")){
			// yeeeaaaahhh. Add to the universal list for bookeeping
			// but don't have the universal list add it again... and again... and again...
			listContainer.allTags.addTag(t);
		} 
	}
	
	public void removeTag (BluetoothDevice dev){	
		knownTags.remove(dev.getAddress());
	}
	
	public boolean isEmpty(){
		return knownTags.isEmpty();
	}
	
	public Tag getTag (String addr){
		 return knownTags.get(addr);
	}
	
	public Tag[] getTags (){
		return knownTags.values().toArray((new Tag[knownTags.size()]));
	}
	
	public String dbgString(){
		String everything;
		if(knownTags.isEmpty()){
			everything = listName + ": (empty)";
		} else {
			everything = listName + ": " + knownTags.values().toString();			
		}
		return everything;
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
		public String nickname;
		public String range;
		private BluetoothDevice dev;
		
		public Tag(BluetoothDevice dev, String nickname){
			this.nickname = nickname;
			this.range = "unscanned";
			this.dev = dev;
		}
		/*
		public Tag(){
			this.address = "";
			this.name = "";
			this.nickname = "";
			this.range = "unscanned";
		} */
		
		public String toString(){
			return nickname + ": " + dev.getAddress();
		}
		
		public BluetoothDevice getDev(){
			return dev;
		}
	}

}
