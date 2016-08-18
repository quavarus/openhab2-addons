package org.openhab.binding.smartthings.client.model;

import java.util.List;

public class Device {
    private String id;
    private String displayName;
    private String name;
    private List<String> capabilities;
    private List<Command> commands;
    private List<CurrentValue> currentValues;

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
