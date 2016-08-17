/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.smartthings.client.ApiClient;
import org.openhab.binding.smartthings.config.SmartThingsBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import io.swagger.client.ApiClient;
//import io.swagger.client.api.StationApi;
//import io.swagger.client.api.ThermostatApi;
//import io.swagger.client.auth.OAuth;
//import io.swagger.client.auth.OAuthFlow;
//import io.swagger.client.model.NAUserAdministrative;
//import io.swagger.client.model.NAUserResponse;
//import retrofit.RestAdapter.LogLevel;

/**
 * {@link SmartThingsBridgeHandler} is the handler for a SmartThings OpenHAB and connects it
 * to the framework. The devices and modules uses the
 * {@link SmartThingsBridgeHandler} to request informations about their status
 *
 * @author quavarus
 *
 */
public class SmartThingsBridgeHandler extends BaseBridgeHandler {
    private static Logger logger = LoggerFactory.getLogger(SmartThingsBridgeHandler.class);
    private SmartThingsBridgeConfiguration configuration;
    private ApiClient apiClient;
    // private StationApi stationApi = null;
    // private ThermostatApi thermostatApi = null;

    public SmartThingsBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SmartThings bridge handler.");

        configuration = getConfigAs(SmartThingsBridgeConfiguration.class);
        if (configuration.token == null || configuration.token.trim().length() == 0) {

        }
    }

    // private void updateChannels() {
    // NAUserResponse user = null;
    //
    // getStationApi();
    // if (stationApi != null) {
    // user = stationApi.getuser();
    // } else {
    // getThermostatApi();
    // if (thermostatApi != null) {
    // user = thermostatApi.getuser();
    // }
    // }
    //
    // if (user != null) {
    // NAUserAdministrative admin = user.getBody().getAdministrative();
    // for (Channel channel : getThing().getChannels()) {
    // String chanelId = channel.getUID().getId();
    // switch (chanelId) {
    // case CHANNEL_UNIT: {
    // updateState(channel.getUID(), new DecimalType(admin.getUnit()));
    // break;
    // }
    // case CHANNEL_WIND_UNIT: {
    // updateState(channel.getUID(), new DecimalType(admin.getWindunit()));
    // break;
    // }
    // case CHANNEL_PRESSURE_UNIT: {
    // updateState(channel.getUID(), new DecimalType(admin.getPressureunit()));
    // break;
    // }
    // }
    // }
    // updateStatus(ThingStatus.ONLINE);
    // } else {
    // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
    // "Unable to initialize Netatmo API connection (check credentials)");
    // }
    // }

    private String getApiScope() {
        StringBuilder stringBuilder = new StringBuilder();

        // if (configuration.readStation) {
        // stringBuilder.append("read_station ");
        // }
        //
        // if (configuration.readThermostat) {
        // stringBuilder.append("read_thermostat write_thermostat ");
        // }

        return stringBuilder.toString().trim();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("This Bridge is read-only and does not handle commands");
    }

    // public StationApi getStationApi() {
    // if (configuration.readStation && stationApi == null) {
    // stationApi = apiClient.createService(StationApi.class);
    // }
    // return stationApi;
    // }
    //
    // public ThermostatApi getThermostatApi() {
    // if (configuration.readThermostat && thermostatApi == null) {
    // thermostatApi = apiClient.createService(ThermostatApi.class);
    // }
    // return thermostatApi;
    // }

}
