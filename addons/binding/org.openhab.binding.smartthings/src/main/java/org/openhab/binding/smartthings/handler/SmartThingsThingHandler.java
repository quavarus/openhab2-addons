/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.handler;

import static org.openhab.binding.smartthings.SmartThingsBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.smartthings.client.model.CurrentValue;
import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.client.model.DeviceCommand;
import org.openhab.binding.smartthings.type.UidUtils;
import org.openhab.binding.smartthings.type.transform.ChannelTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartThingsThingHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Joshua Henry - Initial contribution
 */
public class SmartThingsThingHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(SmartThingsThingHandler.class);

    public SmartThingsThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        logger.debug("Initializing {} channels of thing '{}'", getThing().getChannels().size(), getThing().getUID());
        try {
            updateStatus();
        } catch (BridgeHandlerNotAvailableException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        try {
            if (thing.getStatus() == ThingStatus.ONLINE) {
                logger.debug("Channel linked '{}' from thing id '{}'", channelUID, getThing().getUID().getId());
                updateChannelState(channelUID);
            }
        } catch (Exception ex) {
            logger.warn(ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command '{}' for channel '{}'", command, channelUID);

        Channel channel = getThing().getChannel(channelUID.getId());
        ChannelTransformer transformer = getBridgeHandler().getTransformProvider().getTransformer(channel);
        DeviceCommand deviceCommand = transformer.getDeviceCommand(command);
        getBridgeHandler().runDeviceCommand(UidUtils.getSmartThingsDeviceId(thing), deviceCommand);
        update();

    }

    private Device getDevice() {
        return getBridgeHandler().getDevice(UidUtils.getSmartThingsDeviceId(thing));
    }

    /**
     * Evaluates the channel and datapoint for this channelUID and updates the state of the channel.
     */
    private void updateChannelState(ChannelUID channelUID) {

        Channel channel = getThing().getChannel(channelUID.getId());
        boolean isChannelLinked = isLinked(channel);
        if (isChannelLinked) {

            // ChannelTransformer transformer = getBridgeHandler().getTransformProvider().getTransformer(channel);
            // if (transformer != null) {
            // State state = transformer.getChannelState(getDevice());
            // updateState(channel.getUID(), state);
            // }
            State state = transformChannelState(channel);
            updateState(channelUID, state);
        }
    }

    private State transformChannelState(Channel channel) {
        Configuration config = channel.getConfiguration();
        String itemType = channel.getAcceptedItemType();
        State state = null;
        String channelId = channel.getUID().getId();
        String attributeName = channelId.substring(channelId.lastIndexOf('_') + 1);
        CurrentValue currentValue = getDevice().getCurrentValueMap().get(attributeName);

        if (currentValue.getValue() == null) {
            return UnDefType.NULL;
        }
        String currentValueString = currentValue.getValue().toString();
        switch (itemType) {
            case ITEM_TYPE_NUMBER:
                return new DecimalType(new BigDecimal(currentValueString));
            case ITEM_TYPE_STRING:
                return new StringType(currentValueString);
            case ITEM_TYPE_SWITCH:
                if (config.get("onOpenValue").equals(currentValueString)) {
                    return OnOffType.ON;
                }
                if (config.get("offClosedValue").equals(currentValueString)) {
                    return OnOffType.OFF;
                }
                return UnDefType.NULL;
            case ITEM_TYPE_DIMMER:
                return new PercentType(new BigDecimal(currentValueString));
            case ITEM_TYPE_CONTACT:
                if (config.get("onOpenValue").equals(currentValueString)) {
                    return OpenClosedType.OPEN;
                }
                if (config.get("offClosedValue").equals(currentValueString)) {
                    return OpenClosedType.CLOSED;
                }
                return UnDefType.NULL;
            case ITEM_TYPE_ROLLERSHUTTER:
            case ITEM_TYPE_DATETIME:
            case ITEM_TYPE_COLOR:
            case ITEM_TYPE_IMAGE:
                throw new UnsupportedOperationException("Item Type not supported yet.");
        }

        return state;
    }

    /**
     * Updates the thing status based on device status.
     */
    private void updateStatus() {

        ThingStatus oldStatus = thing.getStatus();
        ThingStatus newStatus = ThingStatus.ONLINE;
        ThingStatusDetail newDetail = ThingStatusDetail.NONE;
        Device device = getDevice();
        if (device == null) {
            newStatus = ThingStatus.OFFLINE;
            newDetail = ThingStatusDetail.COMMUNICATION_ERROR;
        }

        if (thing.getStatus() != newStatus || thing.getStatusInfo().getStatusDetail() != newDetail) {
            updateStatus(newStatus, newDetail);
        }
        if (oldStatus == ThingStatus.OFFLINE && newStatus == ThingStatus.ONLINE) {
            initialize();
        }
    }

    /**
     * Returns true, if the channel is linked at least to one item.
     */
    private boolean isLinked(Channel channel) {
        return channel != null && super.isLinked(channel.getUID().getId());
    }

    private SmartThingsBridgeHandler getBridgeHandler() {
        SmartThingsBridgeHandler bridgeHandler = null;
        if (getBridge() != null) {
            bridgeHandler = ((SmartThingsBridgeHandler) getBridge().getHandler());
        }
        if (bridgeHandler == null) {
            throw new BridgeHandlerNotAvailableException("BridgeHandler not yet available!");
        }
        return bridgeHandler;
    }

    public void update() {
        for (Channel channel : thing.getChannels()) {
            updateChannelState(channel.getUID());
        }
        thingUpdated(thing);
    }
}
