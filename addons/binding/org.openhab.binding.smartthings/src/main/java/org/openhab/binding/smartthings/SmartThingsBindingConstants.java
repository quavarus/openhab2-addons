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

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "smartthingsapi");
    public final static ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    // List of all Channel ids
    public final static String CHANNEL_1 = "channel1";

    // List of all adressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(new HashSet<ThingTypeUID>() {
                {
                    addAll(Arrays.asList(THING_TYPE_BRIDGE, THING_TYPE_SAMPLE));
                }
            });

    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(new HashSet<ThingTypeUID>() {
                {
                    // addAll(Arrays.asList(THING_TYPE_BRIDGE, THING_TYPE_SAMPLE));
                }
            });

}
