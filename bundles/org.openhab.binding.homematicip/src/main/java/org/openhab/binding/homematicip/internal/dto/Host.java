/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.homematicip.internal.dto;

/**
 * Generated Plain Old Java Objects class for {@link Host} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class Host {
    public static final String REST_BASE_URL = "https://srz16.homematic.com:6969";
    public static final String WEBSOCKET_BASE_URL = "wss://srz16.homematic.com:8888";

    public String urlREST = REST_BASE_URL;
    public String urlWebSocket = WEBSOCKET_BASE_URL;
    public String apiVersion;
    public String primaryAccessPointId;
    public String requestingAccessPointId;
}
