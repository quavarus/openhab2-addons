package org.openhab.binding.smartthings.type.transform;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.smartthings.client.model.Attribute;
import org.openhab.binding.smartthings.client.model.Capability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCapabilityTransformer implements CapabilityTransformer {

    private Capability capability;
    private static final Logger logger = LoggerFactory.getLogger(DefaultCapabilityTransformer.class);

    private Map<String, ChannelTransformer> channelTransformers = new HashMap<>();

    public DefaultCapabilityTransformer(Capability capability) {
        this.capability = capability;
        initChannels();
    }

    private void initChannels() {
        if (capability.getAttributes() != null) {
            for (Attribute attribute : capability.getAttributes()) {
                ChannelTransformer transformer = new DefaultChannelTransformer(capability, attribute);
                channelTransformers.put(transformer.getId(), transformer);
            }
        }
    }

    @Override
    public Collection<ChannelTransformer> getChannelTransformers() {
        return Collections.unmodifiableCollection(channelTransformers.values());
    }

    @Override
    public ChannelTransformer getChannelTransformer(Channel channel) {
        return channelTransformers.get(channel.getChannelTypeUID().getId());
    }

}
