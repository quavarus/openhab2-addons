package org.openhab.binding.smartthings.client.model;

import java.util.List;

public class Attribute {
    private String name;
    private String dataType;
    private List<String> values;
    private String unit;

    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    public List<String> getValues() {
        return values;
    }

    public String getUnit() {
        return unit;
    }

}
