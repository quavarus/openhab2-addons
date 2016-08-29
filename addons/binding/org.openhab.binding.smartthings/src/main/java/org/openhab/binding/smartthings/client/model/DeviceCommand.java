package org.openhab.binding.smartthings.client.model;

public class DeviceCommand {

    private final String name;
    private final String[] arguments;

    public DeviceCommand(String name, String... arguments) {
        super();
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public String[] getArguments() {
        return arguments;
    }

}
