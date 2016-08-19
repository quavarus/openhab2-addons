package org.openhab.binding.smartthings.client.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Device {
    private String id;
    private String typeId;
    private String displayName;
    private String name;
    private String label;
    private List<Capability> capabilities;
    private List<Command> supportedCommands;
    private List<Attribute> supportedAttributes;
    private List<CurrentValue> currentValues;

    private transient Map<String, CurrentValue> currentValueMap = null;
    private transient Map<String, Capability> capabilityMap = null;

    private void initCurrentValueMap() {
        currentValueMap = new HashMap<>();
        for (CurrentValue value : currentValues) {
            currentValueMap.put(value.getName(), value);
        }
    }

    private void initCapabilityMap() {
        capabilityMap = new HashMap<>();
        for (Capability capability : this.capabilities) {
            capabilityMap.put(capability.getName(), capability);
        }
    }

    public Map<String, CurrentValue> getCurrentValueMap() {
        if (currentValueMap == null) {
            initCurrentValueMap();
        }
        return currentValueMap;
    }

    public Map<String, Capability> getCapabilityMap() {
        if (capabilityMap == null) {
            initCapabilityMap();
        }
        return capabilityMap;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    public String getLabel() {
        return label;
    }

    public List<Command> getSupportedCommands() {
        return supportedCommands;
    }

    public List<Attribute> getSupportedAttributes() {
        return supportedAttributes;
    }

    public List<CurrentValue> getCurrentValues() {
        return currentValues;
    }

    public String getTypeId() {
        return typeId;
    }
}
