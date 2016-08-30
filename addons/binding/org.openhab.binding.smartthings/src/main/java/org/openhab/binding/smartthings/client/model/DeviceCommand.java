package org.openhab.binding.smartthings.client.model;

public class DeviceCommand {

    private final String name;
    private final Object[] arguments;

    public DeviceCommand(String name, Object... arguments) {
        super();
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public Object[] getArguments() {
        return arguments;
    }

}
