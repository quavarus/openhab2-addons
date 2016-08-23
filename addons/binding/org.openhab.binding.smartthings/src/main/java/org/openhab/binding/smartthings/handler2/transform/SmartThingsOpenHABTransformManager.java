package org.openhab.binding.smartthings.handler2.transform;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.smartthings.client.model.Capability;

public class SmartThingsOpenHABTransformManager {

    Map<CapabilityType, SmartThingsOpenHabTransformer> capabilityTransformerMap = new HashMap<>();
    Map<String, SmartThingsOpenHabTransformer> channelTransformerMap = new HashMap<>();

    public SmartThingsOpenHABTransformManager() {
        initialize();
    }

    private void initialize() {
        capabilityTransformerMap.put(CapabilityType.SWITCH, new SmartThingsOpenHabTransformer());
    }

    public SmartThingsOpenHabTransformer getTransformer(Capability capability) {
        return capabilityTransformerMap.get(capability.getName());
    }

    public SmartThingsOpenHabTransformer getTransformer(Channel channel) {
        return channelTransformerMap.get(channel.getUID());
    }

}
