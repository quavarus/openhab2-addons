package org.openhab.binding.smartthings.type;

import java.util.List;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.smartthings.client.model.Device;

public interface SmartThingsTransformer {

    List<ChannelType> getChannelTypes();

    State getChannelState(Channel channel, Device device);

    String getCommand(Channel channel, Command command);

    String getArguments(Channel channel, Command command);

}
