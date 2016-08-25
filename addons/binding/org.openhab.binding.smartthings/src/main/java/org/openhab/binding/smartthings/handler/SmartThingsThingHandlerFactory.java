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

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.smartthings.type.SmartThingsTransformProvider;
import org.openhab.binding.smartthings.type.SmartThingsTypeGenerator;

/**
 * The {@link SmartThingsThingHandlerFactory} is responsible for creating thing and bridge handlers.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SmartThingsThingHandlerFactory extends BaseThingHandlerFactory {
    private SmartThingsTypeGenerator typeGenerator;
    private SmartThingsTransformProvider transformProvider;

    protected void setTypeGenerator(SmartThingsTypeGenerator typeGenerator) {
        this.typeGenerator = typeGenerator;
    }

    protected void unsetTypeGenerator(SmartThingsTypeGenerator typeGenerator) {
        this.typeGenerator = null;
    }

    protected void setTransformProvider(SmartThingsTransformProvider transformProvider) {
        this.transformProvider = transformProvider;
    }

    protected void unsetTransformProvider(SmartThingsTransformProvider transformProvider) {
        this.transformProvider = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_BRIDGE.equals(thing.getThingTypeUID())) {
            return new SmartThingsBridgeHandler((Bridge) thing, typeGenerator, transformProvider);
        } else {
            return new SmartThingsThingHandler(thing);
        }
    }

}
