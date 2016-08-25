package org.openhab.binding.smartthings.type;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.smartthings.client.model.Capability;
import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.type.transform.DefaultCapabilityTransformer;
import org.openhab.binding.smartthings.type.transform.CapabilityTransformer;

public class SmartThingsTransformProvider {

    private Map<String, CapabilityTransformer> capabilityTransformerMap = new HashMap<>();

    public SmartThingsTransformProvider() {

    }

    public CapabilityTransformer getTransformer(Capability capability) {
        return capabilityTransformerMap.get(capability.getName());
    }

    public CapabilityTransformer getTransformer(Channel channel) {
        String id = channel.getChannelTypeUID().getId();
        String capabilityName = id.substring(0, id.lastIndexOf("_"));
        capabilityName = capabilityName.replaceAll("_", " ");
        return capabilityTransformerMap.get(capabilityName);
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
