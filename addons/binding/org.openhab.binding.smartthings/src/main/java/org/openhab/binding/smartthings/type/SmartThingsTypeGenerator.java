/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.type;

import org.openhab.binding.smartthings.client.model.Device;

public interface SmartThingsTypeGenerator {

    /**
     * Generates the ThingType and ChannelTypes for the given device.
     */
    public void generate(Device device);

    /**
     * Validates all devices for multiple firmware versions. Different firmware versions for the same device may have
     * different datapoints which may cause warnings in the logfile.
     */
    public void validateFirmwares();

}
