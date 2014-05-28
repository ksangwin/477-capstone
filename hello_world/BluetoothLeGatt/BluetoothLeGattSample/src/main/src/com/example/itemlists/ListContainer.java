package com.example.itemlists;


import java.util.HashMap;
import java.util.Set;

import com.example.itemlists.ItemList.Tag;
import com.example.menus.ListContent;
import com.example.menus.MainFragment;

import android.bluetooth.BluetoothDevice;
import android.util.Log;


public class ListContainer {

	private final static String TAG =  ListContainer.class.getSimpleName();
	HashMap<String, ItemList> allLists;
	ItemList allTags;
	
	public ListContainer(){
		allLists = new HashMap<String, ItemList>();
		allTags = new ItemList("all", this);
	}
	
	public void addList(String listName, ItemList list){
		if (list != null) {
			// if the list is already created
			allLists.put(listName, list);
			ListContent.addEntry(list);
		} else {
			// the empty list case
			list = new ItemList(listName, this);
			allLists.put(listName, list);
			Log.d(TAG, "made a list "+ list);
			ListContent.addEntry(list);
		}
		MainFragment.updateAdapter();
	}
	
	public ItemList getList(String listName){
		return allLists.get(listName);
	}
	
	public void deleteList(String listName){
		allLists.remove(listName);
	}
	
	public String getNName(BluetoothDevice dev){
		String addr = dev.getAddress();
		Tag devTag;

		for (ItemList list : allLists.values()) {
		    devTag = list.getTag(addr);
		    
		    if (devTag != null){
		    	Log.d("ListContainer", "Found " + addr + "'s nickname: " + devTag.nickname);
		    	return devTag.nickname;
		    }
		} 
		Log.d("ListContainer", "the tag has no entry!");
	    return "";
	}
	
	
	public String[] getListNames(){
		Log.d("ListContainer", "attempting to get an array: ");
		Set<String> temp = allLists.keySet();
		String[] temp2 = (String[])(temp.toArray(new String[temp.size()]));
		return temp2;
	}

}
