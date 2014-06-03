package com.example.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.example.itemlists.ItemList;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ListContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<ListEntry> ITEMS = new ArrayList<ListEntry>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, ListEntry> ITEM_MAP = new HashMap<String, ListEntry>();
    
    private static int counter = 0;

    public static void addEntry(ItemList toAdd) {
    	ListEntry entry = new ListEntry(counter, toAdd.toString(), toAdd);

        ITEMS.add(entry);
        ITEM_MAP.put(entry.id_str, entry);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class ListEntry {
        public int id;
        public String id_str;
        public String name;
        public ItemList containedList;

        public ListEntry(int id, String name, ItemList newList) {
            this.id = id;
            this.id_str = Integer.toString(id);
            this.name = name;
            this.containedList = newList;
        }

        @Override
        public String toString() {
            return name;
        }
     }
}
