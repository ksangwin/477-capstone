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
	//private HashMap<String, ItemList> allLists;
	private ArrayList<ListE> allLists;
	//public ItemList allTags;
	
	public ListContainer(){
		allLists = new ArrayList<ListE>();
		//allTags = new ItemList("all", this);
	}
	
	public void addList(String listName, ItemList list){
		if (getList(listName) != null){
			return;
		}
		
		if (list != null) {
			// if the list is already created
			allLists.add(new ListE(listName, list));
			ListContent.addEntry(list);
		} else {
			// the empty list case
			list = new ItemList(listName, this);
			allLists.add(new ListE(listName, list));
			ListContent.addEntry(list);
		}
		Log.d(TAG, "Current lists: " + allLists);
		MainFragment.updateAdapter();
	}
	
	public ArrayList<Tag> getAllTags(){
		ArrayList<Tag> ts = new ArrayList<Tag>();
		for (ListE le : allLists){
			ArrayList<Tag> poop = new ArrayList<Tag>(Arrays.asList(le.list.getTags()));
			ts.addAll(poop);
		}
		return ts;
	}
	
	public boolean containsList(String listName){
		for(ListE le : allLists){
			if(le.name.equals(listName)){
				return true;
			}
		}
		return false;
	}
	
	public ItemList getList(String listName){
		for(ListE le : allLists){
			if(le.name.equals(listName)){
				return le.list;
			}
		}
		return null;
	}
	
	public void updateTagInstances(Tag t){
		Tag cur;
		for (ListE le: allLists) {
		    cur = le.list.getTag(t.getAddr());
		    
		    if (cur != null){
		    	cur = t;
		    }
		} 
	}
	
	public Tag getTag(String a){
		Tag cur;
		for (ListE le : allLists) {
		    cur = le.list.getTag(a);
		    
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
		for (ListE le : allLists) {
		    for(Tag t : le.list.getTags()){
		    	if (t.getNname().equals(name)){
		    		return true;
		    	}
		    }
		} 
		
	    return false;
	}
	
	
	public String[] getListNames(){
		ArrayList<String> temp = new ArrayList<String>();
		
		for(ListE le : allLists){
			temp.add(le.name);
		}
		
		return (String[])temp.toArray();
	}
	
	public String toString(){
		String temp = "All Lists: ";
		for(String s : getListNames()){
			temp += s + " ";
		}
		
		return temp;
	}
	
	private class ListE {
		String name;
		ItemList list;
		
		public ListE(String n, ItemList l){
			name = n;
			list = l;
		}
	}

}
