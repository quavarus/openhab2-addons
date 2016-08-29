package org.openhab.binding.smartthings.type;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.smartthings.client.model.Capability;
import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.type.transform.CapabilityTransformer;
import org.openhab.binding.smartthings.type.transform.ChannelTransformer;
import org.openhab.binding.smartthings.type.transform.DefaultCapabilityTransformer;

public class SmartThingsTransformProvider {

    private Map<String, CapabilityTransformer> capabilityTransformerMap = new HashMap<>();

    public SmartThingsTransformProvider() {

    }

    public CapabilityTransformer getTransformer(Capability capability) {
        return capabilityTransformerMap.get(capability.getName());
    }

    public ChannelTransformer getTransformer(Channel channel) {
        String id = channel.getChannelTypeUID().getId();
        String capabilityName = id.substring(0, id.lastIndexOf("_"));
        capabilityName = capabilityName.replaceAll("_", " ");
        CapabilityTransformer capT = capabilityTransformerMap.get(capabilityName);
        return capT.getChannelTransformer(channel);
    }

    public void registerDevice(Device device) {
        for (Capability capability : device.getCapabilities()) {
            registerCapability(capability);
        }

    }

    private void registerCapability(Capability capability) {
        capabilityTransformerMap.put(capability.getName(), new DefaultCapabilityTransformer(capability));
    }

}
