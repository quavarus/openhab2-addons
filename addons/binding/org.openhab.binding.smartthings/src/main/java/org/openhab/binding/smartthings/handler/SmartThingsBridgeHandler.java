/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.handler;

import static org.openhab.binding.smartthings.SmartThingsBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.smartthings.client.SmartAppApi;
import org.openhab.binding.smartthings.client.SmartThingsAPI;
import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.client.model.Endpoint;
import org.openhab.binding.smartthings.config.SmartThingsBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    private static final int DEFAULT_POLLING_INTERVAL = 10; // in seconds

    /** Redirect URL to which the user is redirected after the login */
    private String redirectUrl = DEFAULT_REDIRECT_URL;
    private String authorizeUrl = OAUTH_AUTHORIZE_ENDPOINT_URL;
    private String tokenUrl = OAUTH_ACCESS_TOKEN_ENDPOINT_URL;
    private String responseType = OAUTH_RESPONSE_TYPE;
    private String scope = OAUTH_SCOPE;

    OkHttpClient httpClient;
    private ScheduledFuture<?> pollingJob;

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            updateDevices();
        }
    };

    public SmartThingsBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SmartThings bridge handler.");

        configuration = getConfigAs(SmartThingsBridgeConfiguration.class);

        String url = getAuthenticationUrl();
        Configuration config = getConfig();
        config.put("authUrl", url);
        updateConfiguration(config);

        if (configuration.token == null || configuration.token.trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE);
            if (configuration.code != null && configuration.code.trim().length() > 0) {
                requestAccessToken();
            } else {
                printSetupInstructions(url);
            }
            return;
        }
        initHttp();
        updateStatus(ThingStatus.ONLINE);
    }

    private Map<String, Thing> getThingMap() {
        Map<String, Thing> returnValue = new HashMap<>();
        List<Thing> childThings = getThing().getThings();
        for (Thing thing : childThings) {
            returnValue.put(thing.getConfiguration().get(SMARTTHING_ID).toString(), thing);
        }
        return returnValue;
    }

    private String getAuthenticationUrl() {
        try {
            OAuthClientRequest request = OAuthClientRequest.authorizationLocation(authorizeUrl)
                    .setClientId(configuration.clientId).setRedirectURI(redirectUrl).setScope(scope)
                    .setResponseType(responseType).buildQueryMessage();
            return request.getLocationUri();
        } catch (OAuthSystemException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

    private void requestAccessToken() {
        OAuthClientRequest request = null;
        try {
            request = OAuthClientRequest.tokenLocation(tokenUrl).setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(configuration.clientId).setClientSecret(configuration.clientSecret)
                    .setRedirectURI(redirectUrl).setCode(configuration.code).buildQueryMessage();

        } catch (OAuthSystemException ex) {
            logger.error(ex.getMessage(), ex);
            printAuthenticationFailed(ex);
            return;
        }

        // create OAuth client that uses custom http client under the hood
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        OAuthJSONAccessTokenResponse oAuthResponse;
        try {
            oAuthResponse = oAuthClient.accessToken(request);
            String accessToken = oAuthResponse.getAccessToken();
            Configuration config = getConfig();
            config.put("token", accessToken);
            config.remove("code");
            updateConfiguration(config);
            initHttp();
            updateStatus(ThingStatus.ONLINE);
        } catch (OAuthSystemException | OAuthProblemException e) {
            logger.error(e.getMessage(), e);
            printAuthenticationFailed(e);
            Configuration config = getConfig();
            config.remove("code");
            updateConfiguration(config);
            updateStatus(ThingStatus.OFFLINE);
            // updateThing(getThing());
            // return;
        }
    }

    private void initHttp() {
        httpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + configuration.token).build();
                return chain.proceed(request);
            }
        }).build();
    }

    public SmartThingsAPI getSmartThingsApi() {
        String endpointUrl = "https://graph.api.smartthings.com/api/";
        Retrofit retrofit = new Retrofit.Builder().baseUrl(endpointUrl).client(httpClient)
                .addConverterFactory(GsonConverterFactory.create()).build();
        SmartThingsAPI service = retrofit.create(SmartThingsAPI.class);
        return service;
    }

    public <T> T executeCall(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                T value = response.body();
                return value;
            } else {
                throw new RuntimeException(response.errorBody().string());
            }

        } catch (Exception e) {
            throw new RuntimeException("Error executing api call.", e);
        }
    }

    public SmartAppApi getSmartAppApi() {
        SmartThingsAPI api = getSmartThingsApi();
        List<Endpoint> endpoints = executeCall(api.getEndpoints(configuration.clientId));
        if (endpoints != null && endpoints.size() > 0) {
            String baseUrl = endpoints.get(0).getUri().toString() + "/";
            Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create()).build();
            SmartAppApi service = retrofit.create(SmartAppApi.class);
            return service;
        } else {
            throw new RuntimeException("No Endpoints Authorized");
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("This Bridge is read-only and does not handle commands");
    }

    private synchronized void onUpdate() {
        if (httpClient != null) {
            if (pollingJob == null || pollingJob.isCancelled()) {
                int pollingInterval = DEFAULT_POLLING_INTERVAL;
                try {
                    Object pollingIntervalConfig = getConfig().get(POLLING_INTERVAL);
                    if (pollingIntervalConfig != null) {
                        pollingInterval = ((BigDecimal) pollingIntervalConfig).intValue();
                    } else {
                        logger.info("Polling interval not configured for this hue bridge. Using default value: {}s",
                                pollingInterval);
                    }
                } catch (NumberFormatException ex) {
                    logger.info("Wrong configuration value for polling interval. Using default value: {}s",
                            pollingInterval);
                }
                pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 1, pollingInterval, TimeUnit.SECONDS);
            }
        }
    }

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

    public Device getDeviceById(String deviceId) {
        return executeCall(getSmartAppApi().getDevice(deviceId));
    }

    public Device runDeviceCommand(String deviceId, String command, String... arugments) {
        return executeCall(getSmartAppApi().runDeviceCommand(deviceId, command));
    }

    public List<Device> getDevices() {
        return executeCall(getSmartAppApi().getDevices());
    }

    public void updateDevices() {
        try {
            // try {
            // FullConfig fullConfig = bridge.getFullConfig();
            // if (!lastBridgeConnectionState) {
            // lastBridgeConnectionState = tryResumeBridgeConnection();
            // }
            // if (lastBridgeConnectionState) {
            // Map<String, FullLight> lastLightStateCopy = new HashMap<>(lastLightStates);
            Map<String, Thing> thingMap = getThingMap();
            List<String> updatedThings = new ArrayList<>();
            for (final Device device : getDevices()) {
                final String deviceId = device.getId();
                if (thingMap.containsKey(deviceId)) {
                    updatedThings.add(deviceId);
                    Thing updateThing = thingMap.get(deviceId);
                    SmartThingsHandler updateHandler = (SmartThingsHandler) updateThing.getHandler();
                    updateHandler.refreshFromDevice(device);
                    // if (!isEqual(lastFullLightState, device.getState())) {
                    // logger.debug("Status update for Hue light {} detected.", deviceId);
                    // notifyLightStatusListeners(device, LIGHT_STATE_CHANGED);
                    // }
                }
                // else {
                // lastLightStates.put(deviceId, device);
                // logger.debug("Hue light {} added.", deviceId);
                // notifyLightStatusListeners(device, LIGHT_STATE_ADDED);
                // }
            }
            // // Check for removed lights
            // for (Entry<String, FullLight> fullLightEntry : lastLightStateCopy.entrySet()) {
            // lastLightStates.remove(fullLightEntry.getKey());
            // logger.debug("Hue light {} removed.", fullLightEntry.getKey());
            // for (LightStatusListener lightStatusListener : lightStatusListeners) {
            // try {
            // lightStatusListener.onLightRemoved(bridge, fullLightEntry.getValue());
            // } catch (Exception e) {
            // logger.error("An exception occurred while calling the BridgeHeartbeatListener", e);
            // }
            // }
            // }
            //
            // final Config config = fullConfig.getConfig();
            // if (config != null) {
            // Map<String, String> properties = editProperties();
            // properties.put(Thing.PROPERTY_SERIAL_NUMBER, config.getMACAddress());
            // properties.put(Thing.PROPERTY_FIRMWARE_VERSION, config.getSoftwareVersion());
            // updateProperties(properties);
            // }
            // }
            // } catch (UnauthorizedException | IllegalStateException e) {
            // if (isReachable(bridge.getIPAddress())) {
            // lastBridgeConnectionState = false;
            // onNotAuthenticated(bridge);
            // } else {
            // if (lastBridgeConnectionState || thing.getStatus() == ThingStatus.INITIALIZING) {
            // lastBridgeConnectionState = false;
            // onConnectionLost(bridge);
            // }
            // }
            // } catch (Exception e) {
            // if (bridge != null) {
            // if (lastBridgeConnectionState) {
            // logger.debug("Connection to Hue Bridge {} lost.", bridge.getIPAddress());
            // lastBridgeConnectionState = false;
            // onConnectionLost(bridge);
            // }
            // }
            // }
        } catch (Throwable t) {
            logger.error("An unexpected error occurred: {}", t.getMessage(), t);
        }
    }

    // private boolean isReachable(String ipAddress) {
    // try {
    // // note that InetAddress.isReachable is unreliable, see
    // //
    // http: //
    // stackoverflow.com/questions/9922543/why-does-inetaddress-isreachable-return-false-when-i-can-ping-the-ip-address
    // // That's why we do an HTTP access instead
    //
    // // If there is no connection, this line will fail
    // bridge.authenticate("invalid");
    // } catch (IOException e) {
    // return false;
    // } catch (ApiException e) {
    // if (e.getMessage().contains("SocketTimeout") || e.getMessage().contains("ConnectException")
    // || e.getMessage().contains("SocketException")) {
    // return false;
    // } else {
    // // this seems to be only an authentication issue
    // return true;
    // }
    // }
    // return true;
    // }

}