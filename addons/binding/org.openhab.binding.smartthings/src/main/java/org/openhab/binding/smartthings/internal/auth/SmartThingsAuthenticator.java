/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.internal.auth;

import java.util.Dictionary;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SmartThingsAuthenticator} is responsible for authenticating openHAB
 * against the SmartThings API. It uses the OSGi console to instruct the user how
 * to execute the OAuth flow in the web browser. First the user needs to execute
 * <code>smartthings:startAuthentication</code>. A URL for an OAuth login site is
 * printed to the OSGi console. After login the user is redirected to a callback
 * page where he finds the necessary parameters. Then he needs to execute
 * <code>smartthings:finishAuthentication "&lt;oauth-verifier&gt;" "&lt;user-id&gt;"</code>
 * to finish the authentication process. The {@link SmartThingsAuthenticator} will
 * store the oauth tokens and the user id to the file system in the
 * {@link SmartThingsAuthenticator#contentDir} folder.
 *
 * @see http://www.smartthings.com/de/api/oauthguide
 *
 * @author Joshua Henry
 * @since 1.5.0
 */
public class SmartThingsAuthenticator implements ManagedService {

    private static final Logger logger = LoggerFactory.getLogger(SmartThingsAuthenticator.class);

    /** Default OAuth consumer key */
    private static final String DEFAULT_CONSUMER_KEY = "93c1c023-bb4c-4938-8ffb-f1a1c2d3f287";

    /** Default OAuth consumer secret */
    private static final String DEFAULT_CONSUMER_SECRET = "dfd05756-b5eb-4d00-a1a6-ec45243a54ba";

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
    private String consumerKey = DEFAULT_CONSUMER_KEY;
    private String consumerSecret = DEFAULT_CONSUMER_SECRET;
    private String authorizeUrl = OAUTH_AUTHORIZE_ENDPOINT_URL;
    private String tokenUrl = OAUTH_ACCESS_TOKEN_ENDPOINT_URL;
    private String responseType = OAUTH_RESPONSE_TYPE;
    private String scope = OAUTH_SCOPE;

    private ComponentContext componentContext;

    // private Map<String, SmartThingsAccount> accountsCache = new HashMap<String, SmartThingsAccount>();

    protected void activate(ComponentContext componentContext) {
        this.componentContext = componentContext;
    }

    protected void deactivate(ComponentContext componentContext) {
        this.componentContext = null;
        // unregisterAccounts();
    }

    // private SmartThingsAccount getAccount(String accountId) {
    // return accountsCache.get(accountId);
    // }

    /**
     * Starts the OAuth authentication flow.
     */
    public synchronized void startAuthentication() {

        try {
            OAuthClientRequest request = OAuthClientRequest.authorizationLocation(authorizeUrl).setClientId(consumerKey)
                    .setRedirectURI(redirectUrl).setScope(scope).setResponseType(responseType).buildQueryMessage();
            printSetupInstructions(request.getLocationUri());
        } catch (OAuthSystemException ex) {
            logger.error(ex.getMessage(), ex);
            printAuthenticationFailed(ex);
        }
    }

    /**
     * Finishes the OAuth authentication flow.
     *
     * @param verificationCode
     *            OAuth verification code
     * @param userId
     *            user id
     */
    public synchronized void finishAuthentication(String verificationCode) {

        OAuthClientRequest request = null;
        try {
            request = OAuthClientRequest.tokenLocation(tokenUrl).setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(consumerKey).setClientSecret(consumerSecret).setRedirectURI(redirectUrl)
                    .setCode(verificationCode).buildQueryMessage();

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
            long expiresIn = oAuthResponse.getExpiresIn();
        } catch (OAuthSystemException | OAuthProblemException e) {
            logger.error(e.getMessage(), e);
            printAuthenticationFailed(e);
            return;
        }

        // SmartThingsAccount smartthingsAccount = getAccount(accountId);
        // if (smartthingsAccount == null) {
        // logger.warn("Couldn't find Credentials of Account '{}'. Please check openhab.cfg or smartthings.cfg.",
        // accountId);
        // return;
        // }
        //
        // OAuthConsumer consumer = smartthingsAccount.consumer;
        //
        // if (provider == null || consumer == null) {
        // logger.warn("Could not finish authentication. Please execute 'startAuthentication' first.");
        // return;
        // }
        //
        // try {
        // provider.retrieveAccessToken(consumer, verificationCode);
        // } catch (OAuthException ex) {
        // logger.error(ex.getMessage(), ex);
        // printAuthenticationFailed(ex);
        // return;
        // }
        //
        // smartthingsAccount.userId = userId;
        // smartthingsAccount.setOuathToken(consumer.getToken(), consumer.getTokenSecret());
        // smartthingsAccount.registerAccount(componentContext.getBundleContext());
        // smartthingsAccount.persist();

        printAuthenticationSuccessful();
    }

    private void printSetupInstructions(String url) {
        logger.info(LINE);
        logger.info("# SmartThings Binding Setup: ");
        logger.info("# 1. Open URL '" + url + "' in your web browser");
        logger.info("# 2. Login, choose your user and allow openHAB to access your SmartThings data");
        logger.info(
                "# 3. Execute 'withings:finishAuthentication \"<accountId>\" \"<verifier>\" \"<userId>\"' on OSGi console");
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

    @Override
    public void updated(Dictionary<String, ?> config) throws ConfigurationException {
        if (config != null) {

            // String redirectUrl = (String) config.get("redirectUrl");
            // if (StringUtils.isNotBlank(redirectUrl)) {
            // this.redirectUrl = redirectUrl;
            // }
            //
            // String consumerKeyString = (String) config.get("consumerkey");
            // if (StringUtils.isNotBlank(consumerKeyString)) {
            // this.consumerKey = consumerKeyString;
            // }
            //
            // String consumerSecretString = (String) config.get("consumersecret");
            // if (StringUtils.isNotBlank(consumerSecretString)) {
            // this.consumerSecret = consumerSecretString;
            // }
            //
            // Enumeration<String> configKeys = config.keys();
            // while (configKeys.hasMoreElements()) {
            // String configKey = configKeys.nextElement();
            //
            // // the config-key enumeration contains additional keys that we
            // // don't want to process here ...
            // if ("redirectUrl".equals(configKey) || "consumerkey".equals(configKey)
            // || "consumersecret".equals(configKey) || "service.pid".equals(configKey)) {
            //
            // continue;
            // }
            //
            // String accountId;
            // String configKeyTail;
            //
            // if (configKey.contains(".")) {
            // String[] keyElements = configKey.split("\\.");
            // accountId = keyElements[0];
            // configKeyTail = keyElements[1];
            //
            // } else {
            // accountId = DEFAULT_ACCOUNT_ID;
            // configKeyTail = configKey;
            // }
            //
            // SmartThingsAccount account = accountsCache.get(accountId);
            // if (account == null) {
            // account = new SmartThingsAccount(accountId, consumerKey, consumerSecret);
            // accountsCache.put(accountId, account);
            // }
            //
            // String value = (String) config.get(configKeyTail);
            //
            // if ("userid".equals(configKeyTail)) {
            // account.userId = value;
            // } else if ("token".equals(configKeyTail)) {
            // account.token = value;
            // } else if ("tokensecret".equals(configKeyTail)) {
            // account.tokenSecret = value;
            // } else {
            // throw new ConfigurationException(configKey, "The given configuration key is unknown!");
            // }
            // }
            //
            // registerAccounts();
        }
    }

    // private void registerAccounts() {
    // for (Entry<String, SmartThingsAccount> entry : accountsCache.entrySet()) {
    //
    // String accountId = entry.getKey();
    // SmartThingsAccount account = entry.getValue();
    //
    // if (account.isAuthenticated()) {
    // account.registerAccount(componentContext.getBundleContext());
    // } else if (account.isValid()) {
    // printAuthenticationInfo(accountId);
    // } else {
    // logger.warn(
    // "Configuration details of Account '{}' are invalid please check openhab.cfg or withings.cfg.",
    // accountId);
    // }
    // }
    // }

    // private void unregisterAccounts() {
    // for (SmartThingsAccount account : accountsCache.values()) {
    // account.unregisterAccount();
    // }
    // }

}
