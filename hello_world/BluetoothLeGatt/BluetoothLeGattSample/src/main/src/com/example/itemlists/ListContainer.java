package com.example.itemlists;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import com.example.itemlists.ItemList.Tag;
import com.example.menus.ListContent;
import com.example.menus.MainFragment;

import android.bluetooth.BluetoothDevice;
import android.util.Log;


public class ListContainer {

	private final static String TAG =  ListContainer.class.getSimpleName();
	private HashMap<String, ItemList> allLists;
	//public ItemList allTags;
	
	public ListContainer(){
		allLists = new HashMap<String, ItemList>();
		//allTags = new ItemList("all", this);
	}
	
	public void addList(String listName, ItemList list){
		if (getList(listName) != null){
			return;
		}
		
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
		Log.d(TAG, "Better update the main screen adapter..");
		MainFragment.updateAdapter();
	}
	
	public ArrayList<Tag> getAllTags(){
		ArrayList<Tag> ts = new ArrayList<Tag>();
		for (ItemList l : allLists.values()){
			ArrayList<Tag> poop = new ArrayList<Tag>(Arrays.asList(l.getTags()));
			ts.addAll(poop);
		}
		return ts;
	}
	
	public boolean containsList(String listName){
		boolean temp = allLists.containsKey(listName);
		Log.d(TAG, "list is " + (temp ? "contained" : "not contained"));
		return temp;
	}
	
	public ItemList getList(String listName){
		ItemList ret = allLists.get(listName);
		if (ret == null){
			Log.w(TAG, "given list does not exist!!");
		}
		return ret;
	}
	
	public void updateTagInstances(Tag t){
		Tag cur;
		for (ItemList list : allLists.values()) {
		    cur = list.getTag(t.getAddr());
		    
		    if (cur != null){
		    	cur = t;
		    }
		} 
	}
	
	public Tag getTag(String a){
		Tag cur;
		for (ItemList list : allLists.values()) {
		    cur = list.getTag(a);
		    
		    if (cur != null){
		    	return cur;
		    }
		} 
		return null;
	}
	
	public void deleteList(String listName){
		allLists.remove(listName);
	}
	
	public boolean nNameTaken(String name){
		for (ItemList list : allLists.values()) {
		    for(Tag t : list.getTags()){
		    	if (t.getNname().equals(name)){
		    		return true;
		    	}
		    }
		} 
		
	    return false;
	}
	
	
	
	public String[] getListNames(){
		Log.d("ListContainer", "attempting to get an array: ");
		Set<String> temp = allLists.keySet();
		String[] temp2 = (String[])(temp.toArray(new String[temp.size()]));
		return temp2;
	}

}
