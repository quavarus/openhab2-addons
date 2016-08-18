/**
 * Copyright (c) 2014-2015
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.config;

/**
 * The {@link SmartThingsBridgeConfiguration} is responsible for holding configuration
 * informations needed to access SmartThings OpenHAB SmartApp
 *
 * @author quavarus - Initial contribution
 */
public class SmartThingsBridgeConfiguration {
    public String clientId;
    public String clientSecret;
    public String code;
    public String token;
}
