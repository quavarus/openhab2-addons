package org.openhab.binding.smartthings.client.model;

import java.util.List;

public class CurrentValue {
    private String name;
    private String dataType;
    private List<String> possibleValues;
    private Object value;

    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    public List<String> getPossibleValues() {
        return possibleValues;
    }

    public Object getValue() {
        return value;
    }

}
