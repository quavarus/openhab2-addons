/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.handler;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.smartthings.config.SmartThingsBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /** Default Redirect URL to which the user is redirected after the login */
    private static final String DEFAULT_REDIRECT_URL = "http://www.openhab.org/oauth/smartthings";

    private static final String LINE = "#########################################################################################";

    private static final String OAUTH_ACCESS_TOKEN_ENDPOINT_URL = "https://graph.api.smartthings.com/oauth/token";

    private static final String OAUTH_AUTHORIZE_ENDPOINT_URL = "https://graph.api.smartthings.com/oauth/authorize";

    private static final String OAUTH_RESPONSE_TYPE = "code";
    private static final String OAUTH_SCOPE = "app";

    static final String DEFAULT_ACCOUNT_ID = "DEFAULT_ACCOUNT_ID";

    /** Redirect URL to which the user is redirected after the login */
    private String redirectUrl = DEFAULT_REDIRECT_URL;
    private String authorizeUrl = OAUTH_AUTHORIZE_ENDPOINT_URL;
    private String tokenUrl = OAUTH_ACCESS_TOKEN_ENDPOINT_URL;
    private String responseType = OAUTH_RESPONSE_TYPE;
    private String scope = OAUTH_SCOPE;

    public SmartThingsBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SmartThings bridge handler.");

        configuration = getConfigAs(SmartThingsBridgeConfiguration.class);
        if (configuration.token == null || configuration.token.trim().length() == 0) {
            requestAuthentication();
            throw new RuntimeException("Authentication Required. See Console for instructions.");
        }
    }

    private void requestAuthentication() {
        try {
            OAuthClientRequest request = OAuthClientRequest.authorizationLocation(authorizeUrl).setClientId(consumerKey)
                    .setRedirectURI(redirectUrl).setScope(scope).setResponseType(responseType).buildQueryMessage();
            printSetupInstructions(request.getLocationUri());
        } catch (OAuthSystemException ex) {
            logger.error(ex.getMessage(), ex);
            printAuthenticationFailed(ex);
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

    private void printSetupInstructions(String url) {
        logger.info(LINE);
        logger.info("# SmartThings Binding Setup: ");
        logger.info("# 1. Open URL '" + url + "' in your web browser");
        logger.info("# 2. Login, choose your SmartThings to allow openHAB access to.");
        logger.info("# 3. Execute 'smartThings:finishAuthentication \"<CODE>\"' on OSGi console");
        logger.info(LINE);
    }

    private void printAuthenticationInfo(String accountId) {
        logger.info(LINE);
        logger.info("# SmartThings Binding needs authentication of Account '{}'.", accountId);
        logger.info("# Execute 'withings:startAuthentication' \"<accountId>\" on OSGi console.");
        logger.info(LINE);
    }

    private void printAuthenticationSuccessful() {
        logger.info(LINE);
        logger.info("# SmartThings authentication SUCCEEDED. Binding is now ready to work.");
        logger.info(LINE);
    }

    private void printAuthenticationFailed(Exception ex) {
        logger.info(LINE);
        logger.info("# SmartThings authentication FAILED: " + ex.getMessage());
        logger.info("# Try to restart authentication by executing 'withings:startAuthentication'");
        logger.info(LINE);
    }

}
