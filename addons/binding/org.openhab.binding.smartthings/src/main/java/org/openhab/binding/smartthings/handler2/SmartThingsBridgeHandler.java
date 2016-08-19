/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.handler2;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.config.SmartThingsBridgeConfiguration;
import org.openhab.binding.smartthings.discovery2.SmartThingsDeviceDiscoveryService;
//import org.openhab.binding.smartthings.internal.common.SmartThingsConfig;
//import org.openhab.binding.smartthings.internal.communicator.SmartThingsGateway;
//import org.openhab.binding.smartthings.internal.communicator.SmartThingsGatewayFactory;
//import org.openhab.binding.smartthings.internal.communicator.SmartThingsGatewayListener;
//import org.openhab.binding.smartthings.internal.misc.SmartThingsClientException;
//import org.openhab.binding.smartthings.internal.misc.LocalNetworkInterface;
//import org.openhab.binding.smartthings.internal.model.HmDatapoint;
//import org.openhab.binding.smartthings.internal.model.HmDevice;
import org.openhab.binding.smartthings.type.SmartThingsTypeGenerator;
import org.openhab.binding.smartthings.type.UidUtils;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SmartThingsBridgeHandler} is the handler for a SmartThings gateway and connects it to the framework.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SmartThingsBridgeHandler extends BaseBridgeHandler {
    private static final Logger logger = LoggerFactory.getLogger(SmartThingsBridgeHandler.class);
    private static final long REINITIALIZE_DELAY_SECONDS = 10;
    private static SimplePortPool portPool = new SimplePortPool();

    private SmartThingsBridgeConfiguration config;
    // private SmartThingsGateway gateway;
    private SmartThingsTypeGenerator typeGenerator;

    private SmartThingsDeviceDiscoveryService discoveryService;
    private ServiceRegistration<?> discoveryServiceRegistration;

    public SmartThingsBridgeHandler(Bridge bridge, SmartThingsTypeGenerator typeGenerator) {
        super(bridge);
        this.typeGenerator = typeGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        // try {
        String id = getThing().getUID().getId();
        config = createSmartThingsConfig();

        // gateway = SmartThingsGatewayFactory.createGateway(id, config, this);
        // gateway.initialize();

        registerDeviceDiscoveryService();
        scheduler.submit(new Runnable() {

            @Override
            public void run() {
                discoveryService.startScan(null);
                discoveryService.waitForScanFinishing();
                updateStatus(ThingStatus.ONLINE);
                for (Thing hmThing : getThing().getThings()) {
                    hmThing.getHandler().thingUpdated(hmThing);
                }
            }
        });

        // } catch (IOException ex) {
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
        // dispose();
        // scheduleReinitialize();
        // }
    }

    /**
     * Schedules a reinitialization, if the SmartThings gateway is not reachable at bridge startup.
     */
    private void scheduleReinitialize() {
        scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                initialize();
            }
        }, REINITIALIZE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        logger.debug("Disposing bridge '{}'", getThing().getUID().getId());
        super.dispose();
        if (discoveryService != null) {
            discoveryService.stopScan();
            unregisterDeviceDiscoveryService();
        }
        // if (gateway != null) {
        // gateway.dispose();
        // }
        if (config != null) {
            // portPool.release(config.getCallbackPort());
        }
    }

    /**
     * Registers the DeviceDiscoveryService.
     */
    private void registerDeviceDiscoveryService() {
        logger.trace("Registering SmartThingsDeviceDiscoveryService for bridge '{}'", getThing().getUID().getId());
        discoveryService = new SmartThingsDeviceDiscoveryService(this);
        discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
        discoveryService.activate();
    }

    /**
     * Unregisters the DeviceDisoveryService.
     */
    private void unregisterDeviceDiscoveryService() {
        if (discoveryServiceRegistration != null) {
            SmartThingsDeviceDiscoveryService service = (SmartThingsDeviceDiscoveryService) bundleContext
                    .getService(discoveryServiceRegistration.getReference());
            service.deactivate();

            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
            discoveryService = null;
        }
    }

    /**
     * Sets the OFFLINE status for all things of this bridge that has been removed from the gateway.
     */
    public void setOfflineStatus() {
        for (Thing hmThing : getThing().getThings()) {
            // try {
            // gateway.getDevice(UidUtils.getSmartThingsAddress(hmThing));
            // } catch (SmartThingsClientException e) {
            // ((SmartThingsThingHandler) hmThing.getHandler()).updateStatus(ThingStatus.OFFLINE);
            // }
        }
    }

    /**
     * Creates the configuration for the SmartThingsGateway.
     */
    private SmartThingsBridgeConfiguration createSmartThingsConfig() {
        SmartThingsBridgeConfiguration smartthingsConfig = getThing().getConfiguration()
                .as(SmartThingsBridgeConfiguration.class);
        // if (smartthingsConfig.getCallbackHost() == null) {
        // smartthingsConfig.setCallbackHost(LocalNetworkInterface.getLocalNetworkInterface());
        // }
        // if (smartthingsConfig.getCallbackPort() == 0) {
        // smartthingsConfig.setCallbackPort(portPool.getNextPort());
        // } else {
        // portPool.setInUse(smartthingsConfig.getCallbackPort());
        // }
        logger.debug(smartthingsConfig.toString());
        return smartthingsConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing bridge '{}'", getThing().getUID().getId());
            // reloadAllDeviceValues();
        }
    }

    /**
     * Returns the TypeGenerator.
     */
    public SmartThingsTypeGenerator getTypeGenerator() {
        return typeGenerator;
    }

    // /**
    // * Returns the SmartThingsGateway.
    // */
    // public SmartThingsGateway getGateway() {
    // return gateway;
    // }

    /**
     * Updates the thing for the given SmartThings device.
     */
    public void updateThing(Device device) {
        Thing hmThing = getThingByUID(UidUtils.generateThingUID(device, getThing()));
        if (hmThing != null) {
            hmThing.getHandler().thingUpdated(hmThing);
        }
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    // public void onStateUpdated(HmDatapoint dp) {
    // Thing hmThing = getThingByUID(UidUtils.generateThingUID(dp.getChannel().getDevice(), getThing()));
    // if (hmThing != null) {
    // SmartThingsThingHandler thingHandler = (SmartThingsThingHandler) hmThing.getHandler();
    // thingHandler.updateDatapointState(dp);
    // }
    // }

    /**
     * {@inheritDoc}
     */
    // @Override
    // public void onNewDevice(HmDevice device) {
    // onDeviceLoaded(device);
    // updateThing(device);
    // }

    /**
     * {@inheritDoc}
     */
    // @Override
    // public void onDeviceDeleted(HmDevice device) {
    // discoveryService.deviceRemoved(device);
    // updateThing(device);
    // }

    /**
     * {@inheritDoc}
     */
    // @Override
    // public void onServerRestart() {
    // reloadAllDeviceValues();
    // }

    /**
     * {@inheritDoc}
     */
    // @Override
    // public void onConnectionLost() {
    // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
    // }

    /**
     * {@inheritDoc}
     */
    // @Override
    // public void onConnectionResumed() {
    // updateStatus(ThingStatus.ONLINE);
    // reloadAllDeviceValues();
    // }

    /**
     * {@inheritDoc}
     */
    // @Override
    // public void onDeviceLoaded(HmDevice device) {
    // typeGenerator.generate(device);
    // if (discoveryService != null) {
    // discoveryService.deviceDiscovered(device);
    // }
    // }

    /**
     * {@inheritDoc}
     */
    // @Override
    // public void reloadDeviceValues(HmDevice device) {
    // if (device.isGatewayExtras()) {
    // typeGenerator.generate(device);
    // }
    // updateThing(device);
    // }

    /**
     * {@inheritDoc}
     */
    // @Override
    // public void reloadAllDeviceValues() {
    // for (Thing hmThing : getThing().getThings()) {
    // try {
    // HmDevice device = gateway.getDevice(UidUtils.getSmartThingsAddress(hmThing));
    // gateway.triggerDeviceValuesReload(device);
    // } catch (SmartThingsClientException ex) {
    // logger.warn(ex.getMessage());
    // }
    // }
    // }

}
