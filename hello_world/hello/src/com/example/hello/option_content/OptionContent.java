package com.example.hello.option_content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class OptionContent {

    /**
     * An array of options items.
     */
    public static List<OptionItem> ITEMS = new ArrayList<OptionItem>();

    /**
     * A map of option items, by ID.
     */
    public static Map<String, OptionItem> ITEM_MAP = new HashMap<String, OptionItem>();

    static {
        // two methods of communicating
        addItem(new OptionItem("1", "ADK"));
        addItem(new OptionItem("2", "Bluetooth"));
    }

    private static void addItem(OptionItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A Option item representing a piece of content.
     */
    public static class OptionItem {
        public String id;
        public String content;

        public OptionItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
