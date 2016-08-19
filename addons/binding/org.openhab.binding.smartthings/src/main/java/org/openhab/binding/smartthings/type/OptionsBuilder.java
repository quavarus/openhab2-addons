package org.openhab.binding.smartthings.type;

public interface OptionsBuilder<T> {

    public T createOption(String value, String description);

}
