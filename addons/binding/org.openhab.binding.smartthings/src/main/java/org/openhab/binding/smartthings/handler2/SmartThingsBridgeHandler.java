/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.handler2;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.smartthings.client.SmartThingsClientException;
import org.openhab.binding.smartthings.client.SmartThingsService;
import org.openhab.binding.smartthings.client.model.Device;
import org.openhab.binding.smartthings.config.SmartThingsBridgeConfiguration;
import org.openhab.binding.smartthings.discovery2.SmartThingsDeviceDiscoveryService;
//import org.openhab.binding.smartthings.internal.common.SmartThingsConfig;
//import org.openhab.binding.smartthings.internal.communicator.SmartThingsGateway;
//import org.openhab.binding.smartthings.internal.communicator.SmartThingsGatewayFactory;
//import org.openhab.binding.smartthings.internal.communicator.SmartThingsGatewayListener;
//import org.openhab.binding.smartthings.internal.misc.SmartThingsClientException;
//import org.openhab.binding.smartthings.internal.misc.LocalNetworkInterface;
//import org.openhab.binding.smartthings.internal.model.HmDatapoint;
//import org.openhab.binding.smartthings.internal.model.HmDevice;
import org.openhab.binding.smartthings.type.SmartThingsTypeGenerator;
import org.openhab.binding.smartthings.type.UidUtils;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SmartThingsBridgeHandler} is the handler for a SmartThings gateway and connects it to the framework.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SmartThingsBridgeHandler extends BaseBridgeHandler {
    private static final Logger logger = LoggerFactory.getLogger(SmartThingsBridgeHandler.class);
    private static final long REINITIALIZE_DELAY_SECONDS = 10;
    // private static SimplePortPool portPool = new SimplePortPool();

    private final String authorizeUrl = "https://graph.api.smartthings.com/oauth/authorize";
    private final String redirectUrl = "http://www.openhab.org/oauth/smartthings";
    private final String tokenUrl = "https://graph.api.smartthings.com/oauth/token";

    private SmartThingsBridgeConfiguration config;
    private SmartThingsService gateway;
    private SmartThingsTypeGenerator typeGenerator;

    private SmartThingsDeviceDiscoveryService discoveryService;
    private ServiceRegistration<?> discoveryServiceRegistration;

    public SmartThingsBridgeHandler(Bridge bridge, SmartThingsTypeGenerator typeGenerator) {
        super(bridge);
        this.typeGenerator = typeGenerator;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SmartThings bridge handler.");

        config = createSmartThingsConfig();
        updateAuthenticatingUrl();

        if (config.token == null || config.token.trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE);
            if (config.code != null && config.code.trim().length() > 0) {
                requestAccessToken();
            }
            return;
        }
        initializeCommunication();

    }

    private void updateAuthenticatingUrl() {
        String url = getAuthenticationUrl();
        Configuration fig = getConfig();
        fig.put("authUrl", url);
        updateConfiguration(fig);
    }

    private String getAuthenticationUrl() {
        try {
            String scope = "app";
            String responseType = "code";
            OAuthClientRequest request = OAuthClientRequest.authorizationLocation(authorizeUrl)
                    .setClientId(config.clientId).setRedirectURI(redirectUrl).setScope(scope)
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
                    .setClientId(config.clientId).setClientSecret(config.clientSecret).setRedirectURI(redirectUrl)
                    .setCode(config.code).buildQueryMessage();

        } catch (OAuthSystemException ex) {
            logger.error(ex.getMessage(), ex);
            // printAuthenticationFailed(ex);
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
            initializeCommunication();
        } catch (OAuthSystemException | OAuthProblemException e) {
            logger.error(e.getMessage(), e);
            // printAuthenticationFailed(e);
            Configuration config = getConfig();
            config.remove("code");
            updateConfiguration(config);
            updateStatus(ThingStatus.OFFLINE);
            // updateThing(getThing());
            // return;
        }
    }

    /**
     * {@inheritDoc}
     */
    private void initializeCommunication() {
        // try {
        config = createSmartThingsConfig();

        gateway = new SmartThingsService(config.clientId, config.token);

        registerDeviceDiscoveryService();
        // scheduler.submit(new Runnable() {
        //
        // @Override
        // public void run() {
        // discoveryService.startScan(null);
        // discoveryService.waitForScanFinishing();
        // updateStatus(ThingStatus.ONLINE);
        // for (Thing hmThing : getThing().getThings()) {
        // hmThing.getHandler().thingUpdated(hmThing);
        // }
        // }
        // });

        try {
            List<Device> devices = gateway.getDevices();
            if (devices == null || devices.size() == 0) {
                throw new RuntimeException("No Devices Authorized");
            }
            for (Device device : devices) {
                onDeviceLoaded(device);
            }
            for (Thing hmThing : getThing().getThings()) {
                ((SmartThingsThingHandler)hmThing.getHandler()).thingDefinitionLoaded();
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            dispose();
            scheduleReinitialize();
        }

    }

    /**
     * Schedules a reinitialization, if the SmartThings gateway is not reachable at bridge startup.
     */
    private void scheduleReinitialize() {
        scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                initializeCommunication();
            }
        }, REINITIALIZE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        logger.debug("Disposing bridge '{}'", getThing().getUID().getId());
        super.dispose();
        if (discoveryService != null) {
            discoveryService.stopScan();
            unregisterDeviceDiscoveryService();
        }
        // if (gateway != null) {
        // gateway.dispose();
        // }
        if (config != null) {
            // portPool.release(config.getCallbackPort());
        }
    }

    /**
     * Registers the DeviceDiscoveryService.
     */
    private void registerDeviceDiscoveryService() {
        logger.trace("Registering SmartThingsDeviceDiscoveryService for bridge '{}'", getThing().getUID().getId());
        discoveryService = new SmartThingsDeviceDiscoveryService(this);
        discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
        discoveryService.activate();
    }

    /**
     * Unregisters the DeviceDisoveryService.
     */
    private void unregisterDeviceDiscoveryService() {
        if (discoveryServiceRegistration != null) {
            SmartThingsDeviceDiscoveryService service = (SmartThingsDeviceDiscoveryService) bundleContext
                    .getService(discoveryServiceRegistration.getReference());
            service.deactivate();

            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
            discoveryService = null;
        }
    }

    /**
     * Sets the OFFLINE status for all things of this bridge that has been removed from the gateway.
     */
    public void setOfflineStatus() {
        for (Thing hmThing : getThing().getThings()) {
            try {
                gateway.getDevice(UidUtils.getSmartThingsDeviceId(hmThing));
            } catch (SmartThingsClientException e) {
                ((SmartThingsThingHandler) hmThing.getHandler()).updateStatus(ThingStatus.OFFLINE);
            }
        }
    }

    /**
     * Creates the configuration for the SmartThingsGateway.
     */
    private SmartThingsBridgeConfiguration createSmartThingsConfig() {
        SmartThingsBridgeConfiguration smartthingsConfig = getThing().getConfiguration()
                .as(SmartThingsBridgeConfiguration.class);
        // if (smartthingsConfig.getCallbackHost() == null) {
        // smartthingsConfig.setCallbackHost(LocalNetworkInterface.getLocalNetworkInterface());
        // }
        // if (smartthingsConfig.getCallbackPort() == 0) {
        // smartthingsConfig.setCallbackPort(portPool.getNextPort());
        // } else {
        // portPool.setInUse(smartthingsConfig.getCallbackPort());
        // }
        logger.debug(smartthingsConfig.toString());
        return smartthingsConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            logger.debug("Refreshing bridge '{}'", getThing().getUID().getId());
            reloadAllDeviceValues();
        }
    }

    /**
     * Returns the TypeGenerator.
     */
    public SmartThingsTypeGenerator getTypeGenerator() {
        return typeGenerator;
    }

    /**
     * Returns the SmartThingsGateway.
     */
    public SmartThingsService getGateway() {
        return gateway;
    }

    /**
     * Updates the thing for the given SmartThings device.
     */
    public void updateThing(Device device) {
        Thing hmThing = getThingByUID(UidUtils.generateThingUID(device, getThing()));
        if (hmThing != null) {
            hmThing.getHandler().thingUpdated(hmThing);
        }
    }

    // /**
    // * {@inheritDoc}
    // */
    // public void onStateUpdated(Device device, CurrentValue dp) {
    // Thing hmThing = getThingByUID(UidUtils.generateThingUID(device, getThing()));
    // if (hmThing != null) {
    // SmartThingsThingHandler thingHandler = (SmartThingsThingHandler) hmThing.getHandler();
    // // thingHandler.updateDatapointState(dp);
    // }
    // }

    /**
     * {@inheritDoc}
     */
    // public void onNewDevice(Device device) {
    // onDeviceLoaded(device);
    // updateThing(device);
    // }

    /**
     * {@inheritDoc}
     */
    // public void onDeviceDeleted(Device device) {
    // discoveryService.deviceRemoved(device);
    // updateThing(device);
    // }

    /**
     * {@inheritDoc}
     */
    // public void onServerRestart() {
    // reloadAllDeviceValues();
    // }

    /**
     * {@inheritDoc}
     */
    // public void onConnectionLost() {
    // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
    // }

    /**
     * {@inheritDoc}
     */
    // public void onConnectionResumed() {
    // updateStatus(ThingStatus.ONLINE);
    // reloadAllDeviceValues();
    // }

    /**
     * {@inheritDoc}
     */
    public void onDeviceLoaded(Device device) {
        typeGenerator.generate(device);
        if (discoveryService != null) {
            discoveryService.deviceDiscovered(device);
        }
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public void reloadDeviceValues(Device device) {
        updateThing(device);
    }

    /**
     * {@inheritDoc}
     */
    public void reloadAllDeviceValues() {
        for (Thing hmThing : getThing().getThings()) {
            try {
                Device device = gateway.getDevice(UidUtils.getSmartThingsDeviceId(hmThing));
                reloadDeviceValues(device);
            } catch (SmartThingsClientException ex) {
                logger.warn(ex.getMessage());
            }
        }
    }

}
