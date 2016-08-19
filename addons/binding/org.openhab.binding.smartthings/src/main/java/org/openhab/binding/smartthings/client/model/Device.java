package org.openhab.binding.smartthings.client.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Device {
    private String id;
    private String displayName;
    private String name;
    private List<String> capabilities;
    private List<Command> commands;
    private List<CurrentValue> currentValues;
    private transient Map<String, CurrentValue> currentValueMap = null;

    private void initCurrentValueMap() {
        currentValueMap = new HashMap<>();
        for (CurrentValue value : currentValues) {
            currentValueMap.put(value.getName(), value);
        }
    }

    public Map<String, CurrentValue> getCurrentValueMap() {
        if (currentValueMap == null) {
            initCurrentValueMap();
        }
        return currentValueMap;
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

    public List<String> getCapabilities() {
        return capabilities;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public List<CurrentValue> getCurrentValues() {
        return currentValues;
    }

}
