package com.example.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static List<ListItem> ITEMS = new ArrayList<ListItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, ListItem> ITEM_MAP = new HashMap<String, ListItem>();

    static {
        // Add 3 sample items.
        addItem(new ListItem(0, "Scan for BLE devices"));
        addItem(new ListItem(1, "unused"));
    }

    private static void addItem(ListItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id_str, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class ListItem {
        public int id;
        public String id_str;
        public String content;

        public ListItem(int id, String content) {
            this.id = id;
            this.id_str = Integer.toString(id);
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
     }
}
