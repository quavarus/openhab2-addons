package org.openhab.binding.smartthings.client.model;

import java.util.List;

public class Command {

    private String name;
    private List<String> arguments;

    public String getName() {
        return name;
    }

    public List<String> getArguments() {
        return arguments;
    }

}
