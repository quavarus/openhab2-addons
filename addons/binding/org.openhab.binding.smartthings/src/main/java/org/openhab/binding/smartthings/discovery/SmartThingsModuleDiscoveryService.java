/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.discovery;

import static org.openhab.binding.smartthings.SmartThingsBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.smartthings.client.SmartAppApi;
import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.handler.SmartThingsBridgeHandler;

/**
 * The {@link SmartThingsModuleDiscoveryService} searches for available Netatmo
 * devices and modules connected to the API console
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
public class SmartThingsModuleDiscoveryService extends AbstractDiscoveryService {
    private final static int SEARCH_TIME = 2;
    private SmartThingsBridgeHandler bridgeHandler;

    public SmartThingsModuleDiscoveryService(SmartThingsBridgeHandler bridgeHandler) {
        super(SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        this.bridgeHandler = bridgeHandler;
    }

    @Override
    public void startScan() {
        if (bridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            // SmartThingsBridgeConfiguration config = bridgeHandler.getThing().getConfiguration()
            // .as(SmartThingsBridgeConfiguration.class);

            SmartAppApi api = bridgeHandler.getSmartAppApi();
            List<Device> devices = bridgeHandler.executeCall(api.getDevices());
            for (Device device : devices) {
                onDeviceAddedInternal(device);
            }

        }
        bridgeHandler.updateDevices();
        stopScan();
    }

    private void onDeviceAddedInternal(Device device) {
        ThingUID thingUID = findThingUID(device);
        Map<String, Object> properties = new HashMap<>(1);

        properties.put(SMARTTHING_ID, device.getId());

        String name = device.getDisplayName();

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(bridgeHandler.getThing().getUID()).withLabel((name == null) ? device.getName() : name)
                .build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(Device device) throws IllegalArgumentException {
        String thingType = "switch";
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {

                return new ThingUID(supportedThingTypeUID, bridgeHandler.getThing().getUID(),
                        device.getId().replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered :" + thingType);
    }

}
