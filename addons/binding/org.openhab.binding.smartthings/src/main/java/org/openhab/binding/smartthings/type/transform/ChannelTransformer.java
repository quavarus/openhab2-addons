package org.openhab.binding.smartthings.type.transform;

import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.client.model.DeviceCommand;

public interface ChannelTransformer {

    ChannelType getChannelType();

    String getId();

    State getChannelState(Device device);

    DeviceCommand getDeviceCommand(Command command);

}
