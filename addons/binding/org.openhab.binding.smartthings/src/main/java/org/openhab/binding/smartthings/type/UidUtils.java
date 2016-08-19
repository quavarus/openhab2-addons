/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.type;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.smartthings.client.model.CurrentValue;
import org.openhab.binding.smartthings.client.model.Device;

/**
 * Utility class for generating some UIDs.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class UidUtils {

    /**
     * Generates the ThingTypeUID for the given device. If it's a Homegear device, add a prefix because a Homegear
     * device has more datapoints.
     */
    public static ThingTypeUID generateThingTypeUID(Device device) {
        // if (!device.isGatewayExtras() && device.getGatewayId().equals(HmGatewayInfo.ID_HOMEGEAR)) {
        // return new ThingTypeUID(BINDING_ID, String.format("HG-%s", device.getType()));
        // } else {
        // return new ThingTypeUID(BINDING_ID, String.format(device.getType()));
        // }
        return null;
    }

    /**
     * Generates the ChannelTypeUID for the given datapoint with deviceType, channelNumber and datapointName.
     */
    public static ChannelTypeUID generateChannelTypeUID(CurrentValue dp) {
        // return new ChannelTypeUID(BINDING_ID, String.format("%s_%s_%s", dp.getChannel().getDevice().getType(),
        // dp.getChannel().getNumber(), dp.getName()));
        return null;
    }

    /**
     * Generates the ChannelTypeUID for the given datapoint with deviceType and channelNumber.
     */
    public static ChannelGroupTypeUID generateChannelGroupTypeUID(CurrentValue channel) {
        // return new ChannelGroupTypeUID(BINDING_ID,
        // String.format("%s_%s", channel.getDevice().getType(), channel.getNumber()));
        return null;
    }

    /**
     * Generates the ThingUID for the given device in the given bridge.
     */
    public static ThingUID generateThingUID(Device device, Bridge bridge) {
        ThingTypeUID thingTypeUID = generateThingTypeUID(device);
        return new ThingUID(thingTypeUID, bridge.getUID(), device.getId());
    }

    /**
     * Generates the ChannelUID for the given datapoint with channelNumber and datapointName.
     */
    public static ChannelUID generateChannelUID(CurrentValue dp, ThingUID thingUID) {
        // return new ChannelUID(thingUID, String.valueOf(dp.getChannel().getNumber()), dp.getName());
        return null;
    }

    /**
     * Generates the CurrentValueInfo for the given thing and channelUID.
     */
    // public static HmDatapointInfo createHmDatapointInfo(ChannelUID channelUID) {
    // return new HmDatapointInfo(channelUID.getThingUID().getId(), HmParamsetType.VALUES,
    // NumberUtils.toInt(channelUID.getGroupId()), channelUID.getIdWithoutGroup());
    // }

    /**
     * Returns the address of the Homematic device from the given thing.
     */
    public static String getHomematicAddress(Thing thing) {
        return thing.getUID().getId();
    }

}
