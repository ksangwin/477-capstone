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

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String UUID_BATTERY_STATUS = "00002a19-0000-1000-8000-00805f9b34fb";
    public static String BUTTON_RECEIVED = "00001524-1212-efde-1523-785feabcd123";
    public static String LED_WRITE = "00001525-1212-efde-1523-785feabcd123";
    public static String SERVICE_CHANGED = "00002a05-0000-1000-8000-00805f9b34fb";
    

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
        attributes.put("00001523-1212-efde-1523-785feabcd123", "IO Service");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put(UUID_BATTERY_STATUS, "Battery Status");
        attributes.put(BUTTON_RECEIVED, "Button Received");
        attributes.put(LED_WRITE, "LED Write");
        attributes.put(SERVICE_CHANGED, "Service Changed");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}