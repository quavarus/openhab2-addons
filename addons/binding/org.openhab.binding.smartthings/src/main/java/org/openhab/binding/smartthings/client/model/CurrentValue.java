package org.openhab.binding.smartthings.client.model;

import java.util.Date;

public class CurrentValue {
    private String name;
    private String dataType;
    private Object value;
    private String unit;
    private long date;

    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    public Object getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public Date getDate() {
        return new Date(date);
    }

}
