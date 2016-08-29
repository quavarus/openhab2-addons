package org.openhab.binding.smartthings.type.transform;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.Channel;

public interface CapabilityTransformer {

    Collection<ChannelTransformer> getChannelTransformers();

    ChannelTransformer getChannelTransformer(Channel channel);

    // List<ChannelType> getChannelTypes();

    // State getChannelState(Channel channel, Device device);

    // String getCommand(Channel channel, Command command);

    // String getArguments(Channel channel, Command command);

}
