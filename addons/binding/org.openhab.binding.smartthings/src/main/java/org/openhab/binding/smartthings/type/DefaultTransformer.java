package org.openhab.binding.smartthings.type;

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
import org.eclipse.smarthome.core.thing.Channel;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultTransformer implements SmartThingsTransformer {

    public static final String CONFIG_DESCRIPTION_URI_CHANNEL = "channel-type:smartthings:config";
    private URI configDescriptionUriChannel;
    private Capability capability;
    private static final Logger logger = LoggerFactory.getLogger(DefaultTransformer.class);

    private Map<String, String> keyItemTypeMap = new HashMap<>();
    private Map<String, String> keyCategoryMap = new HashMap<>();
    private Map<String, String> attributeCommandMap = new HashMap<>();

    public DefaultTransformer(Capability capability) {
        this.capability = capability;
        try {
            configDescriptionUriChannel = new URI(CONFIG_DESCRIPTION_URI_CHANNEL);
        } catch (Exception ex) {
            logger.warn("Can't create ConfigDescription URI '{}', ConfigDescription for channels not avilable!",
                    CONFIG_DESCRIPTION_URI_CHANNEL);
        }

        initMaps();
    }

    private void initMaps() {
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

        attributeCommandMap.put("Alarm_alarm_both", "both()");
        attributeCommandMap.put("Alarm_alarm_off", "off()");
        attributeCommandMap.put("Alarm_alarm_siren", "siren()");
        attributeCommandMap.put("Alarm_alarm_strobe", "strobe()");
        attributeCommandMap.put("Consumable_consumableStatus_maintenance_required", "setConsumableStatus(STRING)");
        attributeCommandMap.put("Consumable_consumableStatus_missing", "setConsumableStatus(STRING)");
        attributeCommandMap.put("Consumable_consumableStatus_order", "setConsumableStatus(STRING)");
        attributeCommandMap.put("Consumable_consumableStatus_replace", "setConsumableStatus(STRING)");
        attributeCommandMap.put("Contact Sensor_contact_open", "open()");
        attributeCommandMap.put("Door Control_door_closed", "close()");
        attributeCommandMap.put("Garage Door Control_door_closed", "close()");
        attributeCommandMap.put("Garage Door Control_door_open", "open()");
        attributeCommandMap.put("Indicator_indicatorStatus_never", "indicatorNever()");
        attributeCommandMap.put("Indicator_indicatorStatus_when off", "indicatorWhenOff()");
        attributeCommandMap.put("Indicator_indicatorStatus_when on", "indicatorWhenOn()");
        attributeCommandMap.put("Light_switch_off", "off()");
        attributeCommandMap.put("Light_switch_on", "on()");
        attributeCommandMap.put("Lock_lock_locked", "lock()");
        attributeCommandMap.put("Lock_lock_unlocked", "unlock()");
        attributeCommandMap.put("Lock Codes_lock_locked", "lock()");
        attributeCommandMap.put("Lock Codes_lock_unlocked", "unlock()");
        attributeCommandMap.put("Music Player_mute_muted", "mute()");
        attributeCommandMap.put("Music Player_mute_unmuted", "unmute()");
        attributeCommandMap.put("Relay Switch_switch_off", "off()");
        attributeCommandMap.put("Relay Switch_switch_on", "on()");
        attributeCommandMap.put("Samsung TV_mute_muted", "mute()");
        attributeCommandMap.put("Samsung TV_mute_unmuted", "unmute()");
        attributeCommandMap.put("Samsung TV_pictureMode_dynamic", "setPictureMode(ENUM)");
        attributeCommandMap.put("Samsung TV_pictureMode_movie", "setPictureMode(ENUM)");
        attributeCommandMap.put("Samsung TV_pictureMode_standard", "setPictureMode(ENUM)");
        attributeCommandMap.put("Samsung TV_pictureMode_unknown", "setPictureMode(ENUM)");
        attributeCommandMap.put("Samsung TV_soundMode_clear voice", "setSoundMode(ENUM)");
        attributeCommandMap.put("Samsung TV_soundMode_movie", "setSoundMode(ENUM)");
        attributeCommandMap.put("Samsung TV_soundMode_music", "setSoundMode(ENUM)");
        attributeCommandMap.put("Samsung TV_soundMode_standard", "setSoundMode(ENUM)");
        attributeCommandMap.put("Samsung TV_soundMode_unknown", "setSoundMode(ENUM)");
        attributeCommandMap.put("Samsung TV_switch_off", "off()");
        attributeCommandMap.put("Samsung TV_switch_on", "on()");
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
        attributeCommandMap.put("Thermostat Fan Mode_thermostatFanMode_auto", "fanAuto()");
        attributeCommandMap.put("Thermostat Fan Mode_thermostatFanMode_circulate", "fanCirculate()");
        attributeCommandMap.put("Thermostat Fan Mode_thermostatFanMode_on", "fanOn()");
        attributeCommandMap.put("Thermostat Mode_thermostatMode_auto", "auto()");
        attributeCommandMap.put("Thermostat Mode_thermostatMode_cool", "cool()");
        attributeCommandMap.put("Thermostat Mode_thermostatMode_emergency heat", "emergencyHeat()");
        attributeCommandMap.put("Thermostat Mode_thermostatMode_heat", "heat()");
        attributeCommandMap.put("Thermostat Mode_thermostatMode_off", "off()");
        attributeCommandMap.put("Timed Session_sessionStatus_canceled", "cancel()");
        attributeCommandMap.put("Timed Session_sessionStatus_paused", "pause()");
        attributeCommandMap.put("Timed Session_sessionStatus_running", "start()");
        attributeCommandMap.put("Timed Session_sessionStatus_stopped", "stop()");
        attributeCommandMap.put("Valve_contact_closed", "close()");
        attributeCommandMap.put("Valve_contact_open", "open()");
        attributeCommandMap.put("Valve_valve_closed", "close()");
        attributeCommandMap.put("Valve_valve_open", "open()");
        attributeCommandMap.put("Video Camera_camera_off", "off()");
        attributeCommandMap.put("Video Camera_camera_on", "on()");
        attributeCommandMap.put("Video Camera_mute_muted", "mute()");
        attributeCommandMap.put("Video Camera_mute_unmuted", "unmute()");
        attributeCommandMap.put("Window Shade_windowShade_closed", "close()");
        attributeCommandMap.put("Window Shade_windowShade_open", "open()");

    }

    private ChannelType createChannelType(Capability capability, Attribute attribute) {
        ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(capability.getName(), attribute.getName());
        ChannelType channelType = null;

        String itemType = null;
        String category = "Light";
        String label = attribute.getName();
        String description = attribute.getName();
        boolean readOnly = true;
        BigDecimal minValue = null;
        BigDecimal maxValue = null;
        BigDecimal stepValue = null;
        String pattern = null;

        String channelKey = UidUtils.sanitizeStringId(capability.getName() + "_" + attribute.getName());
        if (keyItemTypeMap.containsKey(channelKey)) {
            itemType = keyItemTypeMap.get(channelKey);
        }

        for (String regex : keyCategoryMap.keySet()) {
            if (channelKey.matches(regex)) {
                category = keyCategoryMap.get(regex);
                break;
            }
        }

        List<StateOption> options = null;
        if (itemType == null) {
            switch (attribute.getDataType()) {
                case "NUMBER":
                    itemType = ITEM_TYPE_NUMBER;
                    break;
                case "VECTOR3":
                    itemType = ITEM_TYPE_STRING;
                    break;
                case "ENUM":
                    itemType = ITEM_TYPE_STRING;
                    options = getOptions(attribute);
                    break;
                case "STRING":
                    itemType = ITEM_TYPE_STRING;
                    break;
                case "JSON_OBJECT":
                    itemType = ITEM_TYPE_STRING;
                    break;
                default:
                    itemType = ITEM_TYPE_STRING;
                    break;
            }
        }

        if (attribute.getUnit() != null) {
            String unit = attribute.getUnit();
            if (unit.equals("%")) {
                unit = "%%";
            }
            pattern = "%s " + unit;
        }

        if (hasCommands(channelKey)) {
            readOnly = false;
        }

        StateDescription state = null;
        state = new StateDescription(minValue, maxValue, stepValue, pattern, readOnly, options);

        channelType = new ChannelType(channelTypeUID, false, itemType, label, description, category, null, state,
                configDescriptionUriChannel);

        return channelType;
    }

    private boolean hasCommands(String channelKey) {
        for (String key : attributeCommandMap.keySet()) {
            if (key.startsWith(channelKey)) {
                return true;
            }
        }
        return false;
    }

    private List<StateOption> getOptions(Attribute attribute) {
        List<StateOption> options = new ArrayList<>();
        // readOnly = false;
        for (String value : attribute.getValues()) {
            Command command = findCommand(attribute, value);
            if (command != null) {
                options.add(new StateOption(value, value));
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

    @Override
    public List<ChannelType> getChannelTypes() {
        List<ChannelType> channelTypes = new ArrayList<>();
        if (capability.getAttributes() != null) {
            for (Attribute attribute : capability.getAttributes()) {
                channelTypes.add(createChannelType(capability, attribute));
            }
        }
        return channelTypes;
    }

    @Override
    public State getChannelState(Channel channel, Device device) {

        State state = null;
        String channelId = channel.getUID().getId();
        String attributeName = channelId.substring(channelId.lastIndexOf('_') + 1);
        CurrentValue currentValue = device.getCurrentValueMap().get(attributeName);

        String itemType = channel.getAcceptedItemType();
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

    private OnOffType valueToSwitchType(String value) {
        String stringValue = value.toLowerCase();
        List<String> onValues = Arrays.asList("on", "true", "open", "active");
        if (onValues.contains(stringValue)) {
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
    public String getCommand(Channel channel, org.eclipse.smarthome.core.types.Command command) {
        Object value = getCommandValue(command);
        Map<String, String> potentials = getPotentialCommands(channel);
        if (potentials.size() == 1) {
            return potentials.values().iterator().next();
        }
        if (value instanceof String) {
            String searchKey = channel.getUID().getId() + "_" + value;
            if (potentials.containsKey(searchKey)) {
                return potentials.get(searchKey);
            }
        }
        return null;
    }

    private Map<String, String> getPotentialCommands(Channel channel) {
        String searchKey = channel.getUID().getId();
        Map<String, String> potentials = new HashMap<>();
        for (String key : attributeCommandMap.keySet()) {
            if (key.startsWith(searchKey)) {
                potentials.put(key, attributeCommandMap.get(key));
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

    @Override
    public String getArguments(Channel channel, org.eclipse.smarthome.core.types.Command command) {
        // TODO Auto-generated method stub
        return null;
    }
}
