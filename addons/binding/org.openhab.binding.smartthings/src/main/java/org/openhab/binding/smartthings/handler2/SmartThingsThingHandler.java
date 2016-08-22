/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.handler2;

import static org.openhab.binding.smartthings.SmartThingsBindingConstants.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

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
import org.openhab.binding.smartthings.client.SmartThingsClientException;
import org.openhab.binding.smartthings.client.SmartThingsService;
import org.openhab.binding.smartthings.client.model.CurrentValue;
import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.type.UidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartThingsThingHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Gerhard Riegler - Initial contribution
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
        try {
            SmartThingsService gateway = getSmartThingsGateway();
            Device device = gateway.getDevice(UidUtils.getSmartThingsDeviceId(getThing()));
            updateStatus(device);
            if (device != null) {
                logger.debug("Initializing {} channels of thing '{}'", getThing().getChannels().size(),
                        getThing().getUID());

                // update channel states
                for (Channel channel : getThing().getChannels()) {
                    updateChannelState(channel.getUID(), device);
                }

                // // update properties
                // Map<String, String> properties = editProperties();
                // setProperty(properties, device, PROPERTY_BATTERY_TYPE, VIRTUAL_DATAPOINT_NAME_BATTERY_TYPE);
                // setProperty(properties, device, Thing.PROPERTY_FIRMWARE_VERSION, VIRTUAL_DATAPOINT_NAME_FIRMWARE);
                // setProperty(properties, device, Thing.PROPERTY_SERIAL_NUMBER, device.getAddress());
                // setProperty(properties, device, PROPERTY_AES_KEY, DATAPOINT_NAME_AES_KEY);
                // updateProperties(properties);
                //
                // // update configurations
                // Configuration config = editConfiguration();
                // for (HmChannel channel : device.getChannels()) {
                // for (HmDatapoint dp : channel.getDatapoints().values()) {
                // if (dp.getParamsetType() == HmParamsetType.MASTER) {
                // loadSmartThingsChannelValues(dp.getChannel());
                // config.put(MetadataUtils.getParameterName(dp),
                // dp.isEnumType() ? dp.getOptionValue() : dp.getValue());
                // }
                // }
                // }
                // updateConfiguration(config);
            }
        } catch (SmartThingsClientException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
        }
        // catch (IOException ex) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        // }
        catch (BridgeHandlerNotAvailableException ex) {
            // ignore
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Sets a thing property with a datapoint value.
     */
    // private void setProperty(Map<String, String> properties, HmDevice device, String propertyName,
    // String datapointName) {
    // HmChannel channelZero = device.getChannel(0);
    // HmDatapoint dp = channelZero
    // .getDatapoint(new HmDatapointInfo(HmParamsetType.VALUES, channelZero, datapointName));
    // if (dp != null) {
    // properties.put(propertyName, ObjectUtils.toString(dp.getValue()));
    // }
    // }

    /**
     * {@inheritDoc}
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        try {
            if (thing.getStatus() == ThingStatus.ONLINE) {
                logger.debug("Channel linked '{}' from thing id '{}'", channelUID, getThing().getUID().getId());
                // updateChannelState(channelUID);
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
        // HmDatapoint dp = null;
        // try {
        // SmartThingsGateway gateway = getSmartThingsGateway();
        // HmDatapointInfo dpInfo = UidUtils.createHmDatapointInfo(channelUID);
        // if (RefreshType.REFRESH == command) {
        // logger.debug("Refreshing {}", dpInfo);
        // dpInfo = new HmDatapointInfo(dpInfo.getAddress(), HmParamsetType.VALUES, 0,
        // VIRTUAL_DATAPOINT_NAME_RELOAD_FROM_GATEWAY);
        // dp = gateway.getDatapoint(dpInfo);
        // gateway.sendDatapoint(dp, new HmDatapointConfig(true), Boolean.TRUE);
        // } else {
        // Channel channel = getThing().getChannel(channelUID.getId());
        // if (channel == null) {
        // logger.warn("Channel '{}' not found in thing '{}' on gateway '{}'", channelUID, getThing().getUID(),
        // gateway.getId());
        // } else {
        // if (StopMoveType.STOP == command && DATAPOINT_NAME_LEVEL.equals(dpInfo.getName())) {
        // // special case with stop type (rollershutter)
        // dpInfo.setName(DATAPOINT_NAME_STOP);
        // HmDatapoint stopDp = gateway.getDatapoint(dpInfo);
        // ChannelUID stopChannelUID = UidUtils.generateChannelUID(stopDp, getThing().getUID());
        // handleCommand(stopChannelUID, OnOffType.ON);
        // } else {
        // dp = gateway.getDatapoint(dpInfo);
        // TypeConverter<?> converter = ConverterFactory.createConverter(channel.getAcceptedItemType());
        // Object newValue = converter.convertToBinding(command, dp);
        // HmDatapointConfig config = getChannelConfig(channel, dp);
        // gateway.sendDatapoint(dp, config, newValue);
        // }
        // }
        // }
        // } catch (SmartThingsClientException | BridgeHandlerNotAvailableException ex) {
        // logger.warn(ex.getMessage());
        // } catch (IOException ex) {
        // if (dp != null && dp.getChannel().getDevice().isOffline()) {
        // logger.warn("Device '{}' is OFFLINE, can't send command '{}' for channel '{}'",
        // dp.getChannel().getDevice().getAddress(), command, channelUID);
        // logger.trace(ex.getMessage(), ex);
        // } else {
        // logger.error(ex.getMessage(), ex);
        // }
        // } catch (Exception ex) {
        // logger.error(ex.getMessage(), ex);
        // }
    }

    /**
     * Evaluates the channel and datapoint for this channelUID and updates the state of the channel.
     */
    private void updateChannelState(ChannelUID channelUID, Device device) {

        Channel channel = getThing().getChannel(channelUID.getId());
        boolean isChannelLinked = isLinked(channel);
        if (isChannelLinked) {
            State state = getChannelStateFromDevice(channelUID, device);
            updateState(channel.getUID(), state);
        }
    }

    private State getChannelStateFromDevice(ChannelUID channelUID, Device device) {
        State state = null;
        String channelId = channelUID.getId();
        String attributeName = channelId.substring(channelId.lastIndexOf('_') + 1);
        CurrentValue currentValue = device.getCurrentValueMap().get(attributeName);

        Channel channel = getThing().getChannel(channelUID.getId());

        String itemType = channel.getAcceptedItemType();
        if (currentValue.getValue() == null) {
            return UnDefType.NULL;
        }
        String currentValueString = currentValue.getValue().toString();
        switch (itemType) {
            case ITEM_TYPE_NUMBER:
                // if (currentValue.getUnit().equals("%")) {
                // return new PercentType(new BigDecimal(currentValueString));
                // }
                return new DecimalType(new BigDecimal(currentValueString));
            case ITEM_TYPE_STRING:
                return new StringType(currentValueString);
            case ITEM_TYPE_SWITCH:
                return valueToSwitchType(currentValueString);
            case ITEM_TYPE_DIMMER:
                return new PercentType(new BigDecimal(currentValueString));
            case ITEM_TYPE_CONTACT:
                return valueToContactType(currentValueString);
        }

        return state;
    }

    private OnOffType valueToSwitchType(String value) {
        String stringValue = value.toLowerCase();
        List<String> onValues = Arrays.asList("on", "true", "open");
        if (onValues.contains(stringValue)) {
            return OnOffType.ON;
        }
        return OnOffType.OFF;
    }

    private OpenClosedType valueToContactType(String value) {
        String stringValue = value.toLowerCase();
        List<String> onValues = Arrays.asList("closed");
        if (onValues.contains(stringValue)) {
            return OpenClosedType.CLOSED;
        }
        return OpenClosedType.OPEN;
    }

    // /**
    // * Sets the configuration or evaluates the channel for this datapoint and updates the state of the channel.
    // */
    // protected void updateDatapointState(HmDatapoint dp) {
    // try {
    // if (SmartThingsTypeGeneratorImpl.isStatusDatapoint(dp)) {
    // updateStatus(dp.getChannel().getDevice());
    // } else if (dp.getParamsetType() == HmParamsetType.MASTER) {
    // // update configuration
    // Configuration config = editConfiguration();
    // config.put(MetadataUtils.getParameterName(dp), dp.getValue());
    // updateConfiguration(config);
    // } else if (!SmartThingsTypeGeneratorImpl.isIgnoredDatapoint(dp)) {
    // // update channel
    // ChannelUID channelUID = UidUtils.generateChannelUID(dp, thing.getUID());
    // Channel channel = thing.getChannel(channelUID.getId());
    // if (channel != null) {
    // updateChannelState(dp, channel);
    // } else {
    // logger.warn("Channel not found for datapoint '{}'", new HmDatapointInfo(dp));
    // }
    // }
    // } catch (BridgeHandlerNotAvailableException ex) {
    // // ignore
    // } catch (Exception ex) {
    // logger.error(ex.getMessage(), ex);
    // }
    // }

    /**
     * Loads all values for the given SmartThings channel if it is not initialized.
     */
    // private void loadSmartThingsChannelValues(HmChannel hmChannel)
    // throws BridgeHandlerNotAvailableException, IOException {
    // if (!hmChannel.isInitialized()) {
    // synchronized (this) {
    // if (!hmChannel.isInitialized()) {
    // try {
    // getSmartThingsGateway().loadChannelValues(hmChannel);
    // } catch (IOException ex) {
    // if (hmChannel.getDevice().isOffline()) {
    // logger.warn("Device '{}' is OFFLINE, can't update channel '{}'",
    // hmChannel.getDevice().getAddress(), hmChannel.getNumber());
    // } else {
    // throw ex;
    // }
    // }
    // }
    // }
    // }
    // }

    /**
     * Updates the thing status based on device status.
     */
    private void updateStatus(Device device) throws BridgeHandlerNotAvailableException {
        // loadSmartThingsChannelValues(device.getChannel(0));

        ThingStatus oldStatus = thing.getStatus();
        ThingStatus newStatus = ThingStatus.ONLINE;
        ThingStatusDetail newDetail = ThingStatusDetail.NONE;

        // if (device.isFirmwareUpdating()) {
        // newStatus = ThingStatus.OFFLINE;
        // newDetail = ThingStatusDetail.FIRMWARE_UPDATING;
        // } else if (device.isUnreach()) {
        // newStatus = ThingStatus.OFFLINE;
        // newDetail = ThingStatusDetail.COMMUNICATION_ERROR;
        // } else if (device.isConfigPending() || device.isUpdatePending()) {
        // newStatus = thing.getStatus();
        // newDetail = ThingStatusDetail.CONFIGURATION_PENDING;
        // }

        if (thing.getStatus() != newStatus || thing.getStatusInfo().getStatusDetail() != newDetail) {
            updateStatus(newStatus, newDetail);
        }
        if (oldStatus == ThingStatus.OFFLINE && newStatus == ThingStatus.ONLINE) {
            initialize();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    /**
     * Returns true, if the channel is linked at least to one item.
     */
    private boolean isLinked(Channel channel) {
        return channel != null && super.isLinked(channel.getUID().getId());
    }

    // /**
    // * Returns the config for a channel.
    // */
    // private HmDatapointConfig getChannelConfig(Channel channel, HmDatapoint dp) {
    // HmDatapointConfig dpConfig = channel.getConfiguration().as(HmDatapointConfig.class);
    // if (DATAPOINT_NAME_STOP.equals(dp.getName()) && CHANNEL_TYPE_BLIND.equals(dp.getChannel().getType())) {
    // dpConfig.setForceUpdate(true);
    // }
    // return dpConfig;
    // }

    /**
     * Returns the SmartThings gateway if the bridge is available.
     */
    private SmartThingsService getSmartThingsGateway() throws BridgeHandlerNotAvailableException {
        if (getBridge() == null || getBridge().getHandler() == null
                || ((SmartThingsBridgeHandler) getBridge().getHandler()).getGateway() == null) {
            if (thing.getStatus() != ThingStatus.INITIALIZING) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR);
            }
            throw new BridgeHandlerNotAvailableException("BridgeHandler not yet available!");
        }

        return ((SmartThingsBridgeHandler) getBridge().getHandler()).getGateway();
    }

    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
    // throws ConfigValidationException {
    // validateConfigurationParameters(configurationParameters);
    //
    // Configuration newConfig = editConfiguration();
    // newConfig.setProperties(configurationParameters);
    //
    // try {
    // for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
    // String key = configurationParmeter.getKey();
    // Object newValue = configurationParmeter.getValue();
    //
    // if (key.startsWith("HMP_")) {
    // key = StringUtils.removeStart(key, "HMP_");
    // Integer channelNumber = NumberUtils.toInt(StringUtils.substringBefore(key, "_"));
    // String dpName = StringUtils.substringAfter(key, "_");
    //
    // SmartThingsGateway gateway = getSmartThingsGateway();
    // HmDevice device = gateway.getDevice(UidUtils.getSmartThingsAddress(getThing()));
    // HmDatapointInfo dpInfo = new HmDatapointInfo(device.getAddress(), HmParamsetType.MASTER,
    // channelNumber, dpName);
    // HmDatapoint dp = device.getChannel(channelNumber).getDatapoint(dpInfo);
    //
    // if (dp != null) {
    // try {
    // if (newValue != null) {
    // if (newValue instanceof BigDecimal) {
    // if (dp.isIntegerType()) {
    // newValue = ((BigDecimal) newValue).intValue();
    // } else if (dp.isFloatType()) {
    // newValue = ((BigDecimal) newValue).doubleValue();
    // }
    // }
    // if (ObjectUtils.notEqual(dp.isEnumType() ? dp.getOptionValue() : dp.getValue(),
    // newValue)) {
    // gateway.sendDatapoint(dp, new HmDatapointConfig(true), newValue);
    // }
    // }
    // } catch (IOException ex) {
    // logger.error("Error setting thing property {}: {}", dpInfo, ex.getMessage());
    // newConfig.put(key, getConfig().get(key));
    // }
    // } else {
    // logger.error("Can't find datapoint for thing property {}", dpInfo);
    // newConfig.put(key, getConfig().get(key));
    // }
    // }
    // }
    // updateConfiguration(newConfig);
    // } catch (SmartThingsClientException | BridgeHandlerNotAvailableException ex) {
    // logger.error("Error setting thing properties: " + ex.getMessage(), ex);
    // }
    // }
}
