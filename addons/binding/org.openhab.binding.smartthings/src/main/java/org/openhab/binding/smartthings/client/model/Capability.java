package org.openhab.binding.smartthings.client.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Capability {

    private String name;
    private List<Attribute> attributes;
    private List<Command> commands;
    public Map<String, Command> commandMap = null;

    public String getName() {
        return name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public Map<String, Command> getCommandMap() {
        if (commandMap == null) {
            commandMap = new HashMap<>();
            if (commands != null) {
                for (Command command : getCommands()) {
                    commandMap.put(command.getName(), command);
                }
            }
        }
        return Collections.unmodifiableMap(commandMap);
    }

}
