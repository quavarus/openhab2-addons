/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.handler;

import static org.openhab.binding.smartthings.SmartThingsBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.smartthings.client.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartThingsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author quavarus - Initial contribution
 */
public class SmartThingsHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(SmartThingsHandler.class);

    private String lightId;
    private SmartThingsBridgeHandler bridgeHandler;
    private boolean propertiesInitializedSuccessfully = false;

    public SmartThingsHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue light handler.");
        initializeThing((getBridge() == null) ? null : getBridge().getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        initializeThing(bridgeStatusInfo.getStatus());
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);
        final String configLightId = (String) getConfig().get(SMARTTHING_ID);
        if (configLightId != null) {
            lightId = configLightId;
            // note: this call implicitly registers our handler as a listener on
            // the bridge
            if (getBridgeHandler() != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                    initializeProperties();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private synchronized void initializeProperties() {
        if (!propertiesInitializedSuccessfully) {
            // FullLight fullLight = getLight();
            // if (fullLight != null) {
            // String modelId = fullLight.getModelID().replaceAll(NORMALIZE_ID_REGEX, "_");
            // updateProperty(Thing.PROPERTY_MODEL_ID, modelId);
            // updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, fullLight.getSoftwareVersion());
            // String vendor = getVendor(modelId);
            // if (vendor != null) {
            // updateProperty(Thing.PROPERTY_VENDOR, vendor);
            // }
            // isOsramPar16 = OSRAM_PAR16_50_TW_MODEL_ID.equals(modelId);
            propertiesInitializedSuccessfully = true;
        }
    }

    private synchronized SmartThingsBridgeHandler getBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof SmartThingsBridgeHandler) {
                this.bridgeHandler = (SmartThingsBridgeHandler) handler;
                // this.bridgeHandler.registerLightStatusListener(this);
            } else {
                return null;
            }
        }
        return this.bridgeHandler;
    }

    private Device getDevice() {
        SmartThingsBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            return bridgeHandler.getDeviceById(lightId);
        }
        return null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        SmartThingsBridgeHandler hueBridge = getBridgeHandler();
        if (hueBridge == null) {
            logger.warn("hue bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        Device device = getDevice();
        if (device == null) {
            logger.debug("hue light not known on bridge. Cannot handle command.");
            return;
        }

        String lightState = null;
        switch (channelUID.getId()) {
            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    lightState = ((OnOffType) command).name();
                }
                break;
            // case CHANNEL_ALERT:
            // if (command instanceof StringType) {
            // lightState = LightStateConverter.toAlertState((StringType) command);
            // if (lightState == null) {
            // // Unsupported StringType is passed. Log a warning
            // // message and return.
            // logger.warn("Unsupported String command: {}. Supported commands are: {}, {}, {} ", command,
            // LightStateConverter.ALERT_MODE_NONE, LightStateConverter.ALERT_MODE_SELECT,
            // LightStateConverter.ALERT_MODE_LONG_SELECT);
            // return;
            // } else {
            // scheduleAlertStateRestore(command);
            // }
            // }
            // break;
        }
        if (lightState != null) {
            hueBridge.runDeviceCommand(device.getId(), lightState);
        } else {
            logger.warn("Command send to an unknown channel id: " + channelUID);
        }
    }

}
