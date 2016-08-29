package org.openhab.binding.smartthings.type.transform;

import static org.openhab.binding.smartthings.SmartThingsBindingConstants.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringListType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.smartthings.client.model.Attribute;
import org.openhab.binding.smartthings.client.model.Capability;
import org.openhab.binding.smartthings.client.model.Command;
import org.openhab.binding.smartthings.client.model.CurrentValue;
import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.client.model.DeviceCommand;
import org.openhab.binding.smartthings.type.UidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultChannelTransformer implements ChannelTransformer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultChannelTransformer.class);

    public static final String CONFIG_DESCRIPTION_URI_CHANNEL = "channel-type:smartthings:config";
    private URI configDescriptionUriChannel;

    private static final Map<String, String> keyItemTypeMap = new HashMap<>();
    private static final Map<String, String> keyCategoryMap = new HashMap<>();
    private static final Map<String, String> attributeCommandMap = new HashMap<>();
    private final List<String> onValues = Arrays.asList("on", "true", "open", "active", "muted");

    private final Capability capability;
    private final Attribute attribute;

    private final ChannelTypeUID channelTypeUID;

    private final String itemType;
    private final String category;
    private final String label;
    private final String description;
    private final boolean readOnly;
    private final BigDecimal minValue;
    private final BigDecimal maxValue;
    private final BigDecimal stepValue;
    private final String pattern;
    private final List<StateOption> options;

    private final ChannelType channelType;

    private String onValue;

    private String offValue;

    {
        initMaps();
    }

    public DefaultChannelTransformer(Capability capability, Attribute attribute) {
        this.capability = capability;
        this.attribute = attribute;
        try {
            configDescriptionUriChannel = new URI(CONFIG_DESCRIPTION_URI_CHANNEL);
        } catch (Exception ex) {
            logger.warn("Can't create ConfigDescription URI '{}', ConfigDescription for channels not avilable!",
                    CONFIG_DESCRIPTION_URI_CHANNEL);
        }

        channelTypeUID = UidUtils.generateChannelTypeUID(capability.getName(), attribute.getName());
        label = attribute.getName();
        description = attribute.getName();
        category = calculateCategory();
        itemType = calculateItemType();
        options = calculateOptions();
        pattern = calculatePattern();
        readOnly = calculateReadOnly();
        stepValue = null;
        minValue = null;
        maxValue = null;

        this.channelType = createChannelType();
    }

    @Override
    public ChannelType getChannelType() {
        return this.channelType;
    }

    @Override
    public String getId() {
        return channelType.getUID().getId();
    }

    @Override
    public State getChannelState(Device device) {

        State state = null;
        String channelId = getId();
        String attributeName = channelId.substring(channelId.lastIndexOf('_') + 1);
        CurrentValue currentValue = device.getCurrentValueMap().get(attributeName);

        String itemType = channelType.getItemType();
        if (currentValue.getValue() == null) {
            return UnDefType.NULL;
        }
        String currentValueString = currentValue.getValue().toString();
        switch (itemType) {
            case ITEM_TYPE_NUMBER:
                return new DecimalType(new BigDecimal(currentValueString));
            case ITEM_TYPE_STRING:
                return new StringType(currentValueString);
            case ITEM_TYPE_SWITCH:
                return valueToSwitchType(currentValueString);
            case ITEM_TYPE_DIMMER:
                return new PercentType(new BigDecimal(currentValueString));
            case ITEM_TYPE_CONTACT:
                return valueToContactType(currentValueString);
        }

        return state;
    }

    private static void initMaps() {
        keyItemTypeMap.put("Switch_switch", ITEM_TYPE_SWITCH);
        keyItemTypeMap.put("Switch_Level_level", ITEM_TYPE_DIMMER);
        keyItemTypeMap.put("Motion_Sensor_motion", ITEM_TYPE_SWITCH);

        keyCategoryMap.put("^Switch_Level", "DimmableLight");
        keyCategoryMap.put("^Switch", "Light");
        keyCategoryMap.put("^Thermostat.*Setpoint$", "Temperature");
        keyCategoryMap.put("^Thermostat.*temperature$", "Temperature");
        keyCategoryMap.put("temperature$", "Temperature");
        keyCategoryMap.put("humidity$", "Humidity");
        keyCategoryMap.put("energy$", "Energy");
        keyCategoryMap.put("power$", "Energy");
        keyCategoryMap.put("motion$", "Motion");

        // enums
        attributeCommandMap.put("Alarm_alarm_both", "both()");
        attributeCommandMap.put("Alarm_alarm_off", "off()");
        attributeCommandMap.put("Alarm_alarm_siren", "siren()");
        attributeCommandMap.put("Alarm_alarm_strobe", "strobe()");
        attributeCommandMap.put("Consumable_consumableStatus_maintenance_required", "setConsumableStatus(STRING)");
        attributeCommandMap.put("Consumable_consumableStatus_missing", "setConsumableStatus(STRING)");
        attributeCommandMap.put("Consumable_consumableStatus_order", "setConsumableStatus(STRING)");
        attributeCommandMap.put("Consumable_consumableStatus_replace", "setConsumableStatus(STRING)");
        attributeCommandMap.put("Indicator_indicatorStatus_never", "indicatorNever()");
        attributeCommandMap.put("Indicator_indicatorStatus_when off", "indicatorWhenOff()");
        attributeCommandMap.put("Indicator_indicatorStatus_when on", "indicatorWhenOn()");
        attributeCommandMap.put("Light_switch_off", "off()");
        attributeCommandMap.put("Light_switch_on", "on()");
        attributeCommandMap.put("Music_Player_mute_muted", "mute()");
        attributeCommandMap.put("Music_Player_mute_unmuted", "unmute()");
        attributeCommandMap.put("Relay_Switch_switch_off", "off()");
        attributeCommandMap.put("Relay_Switch_switch_on", "on()");
        attributeCommandMap.put("Samsung_TV_mute_muted", "mute()");
        attributeCommandMap.put("Samsung_TV_mute_unmuted", "unmute()");
        attributeCommandMap.put("Samsung_TV_pictureMode_dynamic", "setPictureMode(ENUM)");
        attributeCommandMap.put("Samsung_TV_pictureMode_movie", "setPictureMode(ENUM)");
        attributeCommandMap.put("Samsung_TV_pictureMode_standard", "setPictureMode(ENUM)");
        attributeCommandMap.put("Samsung_TV_pictureMode_unknown", "setPictureMode(ENUM)");
        attributeCommandMap.put("Samsung_TV_soundMode_clear voice", "setSoundMode(ENUM)");
        attributeCommandMap.put("Samsung_TV_soundMode_movie", "setSoundMode(ENUM)");
        attributeCommandMap.put("Samsung_TV_soundMode_music", "setSoundMode(ENUM)");
        attributeCommandMap.put("Samsung_TV_soundMode_standard", "setSoundMode(ENUM)");
        attributeCommandMap.put("Samsung_TV_soundMode_unknown", "setSoundMode(ENUM)");
        attributeCommandMap.put("Samsung_TV_switch_off", "off()");
        attributeCommandMap.put("Samsung_TV_switch_on", "on()");
        attributeCommandMap.put("Switch_switch_off", "off()");
        attributeCommandMap.put("Switch_switch_on", "on()");
        attributeCommandMap.put("Thermostat_thermostatFanMode_auto", "fanAuto()");
        attributeCommandMap.put("Thermostat_thermostatFanMode_circulate", "fanCirculate()");
        attributeCommandMap.put("Thermostat_thermostatFanMode_on", "fanOn()");
        attributeCommandMap.put("Thermostat_thermostatMode_auto", "auto()");
        attributeCommandMap.put("Thermostat_thermostatMode_cool", "cool()");
        attributeCommandMap.put("Thermostat_thermostatMode_emergency heat", "emergencyHeat()");
        attributeCommandMap.put("Thermostat_thermostatMode_heat", "heat()");
        attributeCommandMap.put("Thermostat_thermostatMode_off", "off()");
        attributeCommandMap.put("Thermostat_Fan_Mode_thermostatFanMode_auto", "fanAuto()");
        attributeCommandMap.put("Thermostat_Fan_Mode_thermostatFanMode_circulate", "fanCirculate()");
        attributeCommandMap.put("Thermostat_Fan_Mode_thermostatFanMode_on", "fanOn()");
        attributeCommandMap.put("Thermostat_Mode_thermostatMode_auto", "auto()");
        attributeCommandMap.put("Thermostat_Mode_thermostatMode_cool", "cool()");
        attributeCommandMap.put("Thermostat_Mode_thermostatMode_emergency heat", "emergencyHeat()");
        attributeCommandMap.put("Thermostat_Mode_thermostatMode_heat", "heat()");
        attributeCommandMap.put("Thermostat_Mode_thermostatMode_off", "off()");
        attributeCommandMap.put("Timed_Session_sessionStatus_canceled", "cancel()");
        attributeCommandMap.put("Timed_Session_sessionStatus_paused", "pause()");
        attributeCommandMap.put("Timed_Session_sessionStatus_running", "start()");
        attributeCommandMap.put("Timed_Session_sessionStatus_stopped", "stop()");
        attributeCommandMap.put("Valve_valve_closed", "close()");
        attributeCommandMap.put("Valve_valve_open", "open()");
        attributeCommandMap.put("Video_Camera_camera_off", "off()");
        attributeCommandMap.put("Video_Camera_camera_on", "on()");
        attributeCommandMap.put("Video_Camera_mute_muted", "mute()");
        attributeCommandMap.put("Video_Camera_mute_unmuted", "unmute()");

        // non enums
        attributeCommandMap.put("Color_Control_color", "setColor(COLOR_MAP)");
        attributeCommandMap.put("Color_Control_hue", "setHue(NUMBER)");
        attributeCommandMap.put("Color_Control_saturation", "setSaturation(NUMBER)");
        attributeCommandMap.put("Color_Temperature_colorTemperature", "setColorTemperature(NUMBER)");
        attributeCommandMap.put("Samsung_TV_volume", "setVolume(NUMBER)");
        attributeCommandMap.put("Switch_Level_level", "setLevel(NUMBER,NUMBER)");
        attributeCommandMap.put("Thermostat_coolingSetpoint", "setCoolingSetpoint(NUMBER)");
        attributeCommandMap.put("Thermostat_heatingSetpoint", "setHeatingSetpoint(NUMBER)");
        attributeCommandMap.put("Thermostat_Cooling_Setpoint_coolingSetpoint", "setCoolingSetpoint(NUMBER)");
        attributeCommandMap.put("Thermostat_Heating_Setpoint_heatingSetpoint", "setHeatingSetpoint(NUMBER)");
        attributeCommandMap.put("Thermostat_Schedule_schedule", "setSchedule(JSON_OBJECT)");
        attributeCommandMap.put("Timed_Session_timeRemaining", "setTimeRemaining(NUMBER)");

    }

    private ChannelType createChannelType() {

        StateDescription state = null;
        state = new StateDescription(minValue, maxValue, stepValue, pattern, readOnly, options);

        return new ChannelType(channelTypeUID, false, itemType, label, description, category, null, state,
                configDescriptionUriChannel);
    }

    private boolean hasMappedCommands(String channelKey) {
        for (String key : attributeCommandMap.keySet()) {
            if (key.startsWith(channelKey)) {
                return true;
            }
        }
        return false;
    }

    private boolean calculateReadOnly() {
        if (hasMappedCommands(channelTypeUID.getId())) {
            return false;
        }
        return true;
    }

    private String calculatePattern() {
        if (attribute.getUnit() != null) {
            String unit = attribute.getUnit();
            if (unit.equals("%")) {
                unit = "%%";
            }
            return "%s " + unit;
        }
        return null;
    }

    private String calculateItemType() {
        // if (keyItemTypeMap.containsKey(channelTypeUID.getId())) {
        // return keyItemTypeMap.get(channelTypeUID.getId());
        // }

        switch (attribute.getDataType()) {
            case "NUMBER":
                return ITEM_TYPE_NUMBER;
            case "VECTOR3":
                return ITEM_TYPE_STRING;
            case "ENUM":
                Map<String, String> mappedCommands = getPotentialCommands(channelTypeUID.getId());
                if (mappedCommands != null && mappedCommands.size() == 2) {
                    String[] values = mappedCommands.keySet().toArray(new String[2]);
                    if (onValues.contains(values[0])) {
                        this.onValue = values[0];
                        this.offValue = values[1];
                        return ITEM_TYPE_SWITCH;
                    } else if (onValues.contains(values[1].trim().toLowerCase())) {
                        this.onValue = values[1];
                        this.offValue = values[0];
                        return ITEM_TYPE_SWITCH;
                    }
                }
                return ITEM_TYPE_STRING;
            case "STRING":
                return ITEM_TYPE_STRING;
            case "JSON_OBJECT":
                return ITEM_TYPE_STRING;
            default:
                return ITEM_TYPE_STRING;
        }
    }

    private String calculateCategory() {
        for (String regex : keyCategoryMap.keySet()) {
            if (channelTypeUID.getId().matches(regex)) {
                return keyCategoryMap.get(regex);
            }
        }
        return null;
    }

    private List<StateOption> calculateOptions() {
        List<StateOption> options = new ArrayList<>();
        if (itemType.equals(ITEM_TYPE_STRING) && attribute.getValues() != null) {
            for (String value : attribute.getValues()) {
                Command command = findCommand(attribute, value);
                if (command != null) {
                    options.add(new StateOption(value, value));
                }
            }
        }
        return options;
    }

    private Command findCommand(Attribute attribute, String value) {
        String searchKey = capability.getName() + "_" + attribute.getName() + "_" + value;
        String commandKey = attributeCommandMap.get(searchKey);
        Command command = null;
        if (commandKey != null) {
            String commandName = commandKey.substring(0, commandKey.lastIndexOf('('));
            command = capability.getCommandMap().get(commandName);
        }
        return command;
    }

    private OnOffType valueToSwitchType(String value) {
        if (value.equals(onValue)) {
            return OnOffType.ON;
        }
        return OnOffType.OFF;
    }

    private OpenClosedType valueToContactType(String value) {
        String stringValue = value.toLowerCase();
        List<String> onValues = Arrays.asList("closed");
        if (onValues.contains(stringValue)) {
            return OpenClosedType.CLOSED;
        }
        return OpenClosedType.OPEN;
    }

    @Override
    public DeviceCommand getDeviceCommand(org.eclipse.smarthome.core.types.Command command) {
        Object value = getCommandValue(command);
        String commandKey = getCommandKey(value);
        DeviceCommand deviceCommand = createDeviceCommand(commandKey, value);
        return deviceCommand;
    }

    private DeviceCommand createDeviceCommand(String commandExpression, Object value) {
        DeviceCommand deviceCommand = null;
        if (commandExpression != null) {
            String commandName = commandExpression.substring(0, commandExpression.indexOf('('));
            Command command = capability.getCommandMap().get(commandName);
            if (command.getArguments() == null || command.getArguments().size() == 0) {
                deviceCommand = new DeviceCommand(commandName);
            } else {
                deviceCommand = new DeviceCommand(commandName, value.toString());
            }
        }
        return deviceCommand;
    }

    private String getCommandKey(Object commandValue) {
        Map<String, String> potentials = getPotentialCommands(getId());
        if (potentials.size() == 1) {
            return potentials.values().iterator().next();
        }
        if (commandValue instanceof String) {
            String searchKey = commandValue.toString();
            if (potentials.containsKey(searchKey)) {
                return potentials.get(searchKey);
            }
        }
        return null;
    }

    private Map<String, String> getPotentialCommands(String searchKey) {
        Map<String, String> potentials = new HashMap<>();
        for (String key : attributeCommandMap.keySet()) {
            if (key.startsWith(searchKey)) {
                String newKey = key.replace(searchKey, "");
                if (newKey.startsWith("_")) {
                    newKey = newKey.substring(1);
                }
                if (newKey.length() == 0) {
                    newKey = "*";
                }
                potentials.put(newKey, attributeCommandMap.get(key));
            }
        }
        return potentials;
    }

    private Object getCommandValue(org.eclipse.smarthome.core.types.Command command) {
        if (command instanceof DateTimeType) {
            DateTimeType typedCommand = (DateTimeType) command;
            return typedCommand.getCalendar().getTime();
        }
        if (command instanceof DecimalType) {
            DecimalType typedCommand = (DecimalType) command;
            return typedCommand.toBigDecimal();
        }
        if (command instanceof HSBType) {
            HSBType typedCommand = (HSBType) command;
            return typedCommand.getRGB();
        }
        if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType typedCommand = (IncreaseDecreaseType) command;
            return typedCommand.name();
        }
        if (command instanceof NextPreviousType) {
            NextPreviousType typedCommand = (NextPreviousType) command;
            return typedCommand.name();
        }
        if (command instanceof OnOffType) {
            OnOffType typedCommand = (OnOffType) command;
            if (typedCommand.equals(OnOffType.ON)) {
                return onValue;
            }
            if (typedCommand.equals(OnOffType.OFF)) {
                return offValue;
            }
            return typedCommand.name();
        }
        if (command instanceof OpenClosedType) {
            OpenClosedType typedCommand = (OpenClosedType) command;
            return typedCommand.name();
        }
        if (command instanceof PercentType) {
            PercentType typedCommand = (PercentType) command;
            return typedCommand.toBigDecimal();
        }
        if (command instanceof PlayPauseType) {
            PlayPauseType typedCommand = (PlayPauseType) command;
            return typedCommand.name();
        }
        if (command instanceof PointType) {
            PointType typedCommand = (PointType) command;
            return typedCommand;
        }
        if (command instanceof RefreshType) {
            RefreshType typedCommand = (RefreshType) command;
            return typedCommand.name();
        }
        if (command instanceof RewindFastforwardType) {
            RewindFastforwardType typedCommand = (RewindFastforwardType) command;
            return typedCommand.name();
        }
        if (command instanceof StopMoveType) {
            StopMoveType typedCommand = (StopMoveType) command;
            return typedCommand.name();
        }
        if (command instanceof StringListType) {
            StringListType typedCommand = (StringListType) command;
            return typedCommand.toString();
        }
        if (command instanceof StringType) {
            StringType typedCommand = (StringType) command;
            return typedCommand.toString();
        }
        if (command instanceof UpDownType) {
            UpDownType typedCommand = (UpDownType) command;
            return typedCommand.name();
        }
        throw new UnsupportedOperationException("Command type is unsupported. " + command.getClass().getName());
    }

}
