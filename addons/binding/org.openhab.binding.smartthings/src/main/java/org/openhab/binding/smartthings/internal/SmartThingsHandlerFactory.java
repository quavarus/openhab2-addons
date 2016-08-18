/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.internal;

import static org.openhab.binding.smartthings.SmartThingsBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.smartthings.discovery.SmartThingsModuleDiscoveryService;
import org.openhab.binding.smartthings.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.handler.SmartThingsHandler;
import org.osgi.framework.ServiceRegistration;

/**
 * The {@link SmartThingsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author quavarus - Initial contribution
 */
public class SmartThingsHandlerFactory extends BaseThingHandlerFactory {

    private ServiceRegistration<?> discoveryServiceReg;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            SmartThingsBridgeHandler bridgeHandler = new SmartThingsBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_SWITCH)) {
            return new SmartThingsHandler(thing);
        }

        return null;
    }

    private void registerDeviceDiscoveryService(SmartThingsBridgeHandler bridge) {
        SmartThingsModuleDiscoveryService discoveryService = new SmartThingsModuleDiscoveryService(bridge);
        discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
        discoveryServiceReg.toString();
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (discoveryServiceReg != null && thingHandler.getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE)) {
            discoveryServiceReg.unregister();
            discoveryServiceReg = null;
        }
        super.removeHandler(thingHandler);
    }
}
