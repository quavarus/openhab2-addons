package org.openhab.binding.smartthings.handler2.transform;

public enum CapabilityType {

    ACCELERATION_SENSOR("Acceleration Sensor"),
    ACTUATOR("Actuator"),
    ALARM("Alarm"),
    AUDIO_NOTIFICATION("Audio Notification"),
    BATTERY("Battery"),
    BEACON("Beacon"),
    BUFFERED_VIDEO_CAPTURE("Buffered Video Capture"),
    BUTTON("Button"),
    CARBON_DIOXIDE_MEASUREMENT("Carbon Dioxide Measurement"),
    CARBON_MONOXIDE_DETECTOR("Carbon Monoxide Detector"),
    COLOR_CONTROL("Color Control"),
    COLOR_TEMPERATURE("Color Temperature"),
    CONFIGURATION("Configuration"),
    CONSUMABLE("Consumable"),
    CONTACT_SENSOR("Contact Sensor"),
    DOOR_CONTROL("Door Control"),
    ENERGY_METER("Energy Meter"),
    ESTIMATED_TIME_OF_ARRIVAL("Estimated Time Of Arrival"),
    GARAGE_DOOR_CONTROL("Garage Door Control"),
    HEALTH_CHECK("Health Check"),
    ILLUMINANCE_MEASUREMENT("Illuminance Measurement"),
    IMAGE_CAPTURE("Image Capture"),
    INDICATOR("Indicator"),
    LIGHT("Light"),
    LOCATION_MODE("Location Mode"),
    LOCK("Lock"),
    LOCK_CODES("Lock Codes"),
    MEDIA_CONTROLLER("Media Controller"),
    MOMENTARY("Momentary"),
    MOTION_SENSOR("Motion Sensor"),
    MUSIC_PLAYER("Music Player"),
    NOTIFICATION("Notification"),
    PH_MEASUREMENT("pH_Measurement"),
    POLLING("Polling"),
    POWER("Power"),
    POWER_METER("Power Meter"),
    POWER_SOURCE("Power Source"),
    PRESENCE_SENSOR("Presence Sensor"),
    REFRESH("Refresh"),
    RELATIVE_HUMIDITY_MEASUREMENT("Relative Humidity Measurement"),
    RELAY_SWITCH("Relay Switch"),
    SAMSUNG_TV("Samsung TV"),
    SENSOR("Sensor"),
    SHOCK_SENSOR("Shock Sensor"),
    SIGNAL_STRENGTH("Signal Strength"),
    SLEEP_SENSOR("Sleep Sensor"),
    SMOKE_DETECTOR("Smoke Detector"),
    SOUND_PRESSURE_LEVEL("Sound Pressure Level"),
    SOUND_SENSOR("Sound Sensor"),
    SPEECH_RECOGNITION("Speech Recognition"),
    SPEECH_SYNTHESIS("Speech Synthesis"),
    STEP_SENSOR("Step Sensor"),
    SWITCH("Switch"),
    SWITCH_LEVEL("Switch Level"),
    TAMPER_ALERT("Tamper Alert"),
    TEMPERATURE_MEASUREMENT("Temperature Measurement"),
    TEST_CAPABILITY("Test Capability"),
    THERMOSTAT("Thermostat"),
    THERMOSTAT_COOLING_SETPOINT("Thermostat Cooling Setpoint"),
    THERMOSTAT_FAN_MODE("Thermostat Fan Mode"),
    THERMOSTAT_HEATING_SETPOINT("Thermostat Heating Setpoint"),
    THERMOSTAT_MODE("Thermostat Mode"),
    THERMOSTAT_OPERATING_STATE("Thermostat Operating State"),
    THERMOSTAT_SCHEDULE("Thermostat Schedule"),
    THERMOSTAT_SETPOINT("Thermostat Setpoint"),
    THREE_AXIS("Three Axis"),
    TIMED_SESSION("Timed Session"),
    TONE("Tone"),
    TOUCH_SENSOR("Touch Sensor"),
    TV("TV"),
    ULTRAVIOLET_INDEX("Ultraviolet Index"),
    VALVE("Valve"),
    VIDEO_CAMERA("Video Camera"),
    VIDEO_CAPTURE("Video Capture"),
    VOLTAGE_MEASUREMENT("Voltage Measurement"),
    WATER_SENSOR("Water Sensor"),
    WINDOW_SHADE("Window Shade"),
    ZW_MULTICHANNEL("Zw Multichannel");

    private String key;

    CapabilityType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key;
    }

}
