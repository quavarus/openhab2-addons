/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.discovery;

import static org.openhab.binding.smartthings.SmartThingsBindingConstants.BINDING_ID;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.type.UidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SmartThingsDeviceDiscoveryService} is used to discover devices that are connected to a SmartThings
 * hub.
 *
 * @author Joshua Henry - Initial contribution
 */
public class SmartThingsDeviceDiscoveryService extends AbstractDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(SmartThingsDeviceDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 300;

    private SmartThingsBridgeHandler bridgeHandler;
    private Future<?> scanFuture;

    public SmartThingsDeviceDiscoveryService(SmartThingsBridgeHandler bridgeHandler) {
        super(ImmutableSet.of(new ThingTypeUID(BINDING_ID, "-")), DISCOVER_TIMEOUT_SECONDS, false);
        this.bridgeHandler = bridgeHandler;
    }

    /**
     * Called on component activation.
     */
    public void activate() {
        super.activate(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivate() {
        super.deactivate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startScan() {
        logger.debug("Starting SmartThings discovery scan");
        loadDevices();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stopScan() {
        logger.debug("Stopping SmartThings discovery scan");
        waitForScanFinishing();
        super.stopScan();
    }

    /**
     * Waits for the discovery scan to finish and then returns.
     */
    public void waitForScanFinishing() {
        if (scanFuture != null) {
            logger.debug("Waiting for SmartThings device discovery scan to finish");
            try {
                scanFuture.get();
                logger.debug("SmartThings device discovery scan finished");
            } catch (CancellationException ex) {
                // ignore
            } catch (Exception ex) {
                logger.error("Error waiting for device discovery scan: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * Starts a thread which loads all SmartThings devices connected to the gateway.
     */
    public void loadDevices() {
        if (scanFuture == null) {
            scanFuture = scheduler.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        for (Device device : bridgeHandler.getDevices()) {
                            bridgeHandler.registerDevice(device);
                            deviceDiscovered(device);
                        }
                        logger.debug("Finished SmartThings device discovery scan on gateway");
                    } catch (Throwable ex) {
                        logger.error(ex.getMessage(), ex);
                    } finally {
                        scanFuture = null;
                        removeOlderResults(getTimestampOfLastScan());
                    }
                }
            });
        } else {
            logger.debug("SmartThings devices discovery scan in progress");
        }
    }

    /**
     * Removes the SmartThings device.
     */
    public void deviceRemoved(Device device) {
        ThingUID thingUID = UidUtils.generateThingUID(device, bridgeHandler.getThing());
        thingRemoved(thingUID);
    }

    /**
     * Generates the DiscoveryResult from a SmartThings device.
     */
    public void deviceDiscovered(Device device) {
        ThingUID bridgeUID = bridgeHandler.getThing().getUID();
        ThingTypeUID typeUid = UidUtils.generateThingTypeUID(device);
        ThingUID thingUID = new ThingUID(typeUid, bridgeUID, device.getId());
        String label = device.getDisplayName() != null ? device.getDisplayName() : device.getId();

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(label)
                .build();
        thingDiscovered(discoveryResult);
    }

}
