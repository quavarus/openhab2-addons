package org.openhab.binding.smartthings.client.model;

import java.util.List;

public class Capability {

    private String name;
    private List<Attribute> attributes;
    private List<Command> commands;

    public String getName() {
        return name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Command> getCommands() {
        return commands;
    }

}
