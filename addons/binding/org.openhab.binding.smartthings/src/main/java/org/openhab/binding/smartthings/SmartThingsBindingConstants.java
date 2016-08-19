/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SmartThingsBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author quavarus - Initial contribution
 */
public class SmartThingsBindingConstants {

    public static final String BINDING_ID = "smartthings";

    public static final String SMARTTHING_ID = "smartThingId";
    public static final String POLLING_INTERVAL = "pollingInterval";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "smartthingsapi");
    public final static ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");

    // List of all Channel ids
    public final static String CHANNEL_1 = "channel1";
    public final static String CHANNEL_SWITCH = "switch";
    public final static String CHANNEL_ALERT = "alert";

    // List of all addressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(new HashSet<ThingTypeUID>() {
                {
                    addAll(Arrays.asList(THING_TYPE_BRIDGE, THING_TYPE_SWITCH));
                }
            });

    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(new HashSet<ThingTypeUID>() {
                {
                    addAll(Arrays.asList(THING_TYPE_SWITCH));
                }
            });

    // things from homematic
    public static final String CONFIG_DESCRIPTION_URI_CHANNEL = "channel-type:homematic:config";
    public static final String CONFIG_DESCRIPTION_URI_THING = "thing-type:homematic:config";

    public static final String PROPERTY_VENDOR_NAME = "eQ-3 AG";

    public static final String ITEM_TYPE_SWITCH = "Switch";
    public static final String ITEM_TYPE_ROLLERSHUTTER = "Rollershutter";
    public static final String ITEM_TYPE_CONTACT = "Contact";
    public static final String ITEM_TYPE_STRING = "String";
    public static final String ITEM_TYPE_NUMBER = "Number";
    public static final String ITEM_TYPE_DIMMER = "Dimmer";

    public static final String CATEGORY_BATTERY = "Battery";
    public static final String CATEGORY_ALARM = "Alarm";
    public static final String CATEGORY_HUMIDITY = "Humidity";
    public static final String CATEGORY_TEMPERATURE = "Temperature";
    public static final String CATEGORY_MOTION = "Motion";
    public static final String CATEGORY_PRESSURE = "Pressure";
    public static final String CATEGORY_SMOKE = "Smoke";
    public static final String CATEGORY_WATER = "Water";
    public static final String CATEGORY_WIND = "Wind";
    public static final String CATEGORY_RAIN = "Rain";
    public static final String CATEGORY_ENERGY = "Energy";
    public static final String CATEGORY_BLINDS = "Blinds";
    public static final String CATEGORY_CONTACT = "Contact";
    public static final String CATEGORY_DIMMABLE_LIGHT = "DimmableLight";
    public static final String CATEGORY_SWITCH = "Switch";

    public static final String PROPERTY_BATTERY_TYPE = "batteryType";
    public static final String PROPERTY_AES_KEY = "aesKey";

}
