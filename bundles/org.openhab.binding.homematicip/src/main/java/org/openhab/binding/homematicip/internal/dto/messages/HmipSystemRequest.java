/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homematicip.internal.dto.messages;

/**
 * Generated Plain Old Java Objects class for {@link HmipSystemRequest} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class HmipSystemRequest extends PluginMessage {

    public static final String HMIP_CONTROL_SET_SWITCH_STATE_PATH = "/hmip/device/control/setSwitchState";
    public static final String HMIP_GROUP_HEATING_SET_ACTIVE_PROFILE_PATH = "/hmip/group/heating/setActiveProfile";
    public static final String HMIP_GROUP_HEATING_SET_CONTROL_MODE_PATH = "/hmip/group/heating/setControlMode";
    public static final String HMIP_GROUP_HEATING_SET_SETPOINT_TEMPERATURE_PATH = "/hmip/group/heating/setSetPointTemperature";
    public static final String HMIP_HOME_GET_SYSTEM_STATE_PATH = "/hmip/home/getSystemState";
    public static final String HMIP_HOME_SECURITY_SET_ZONES_ACTIVATION_PATH = "/hmip/home/security/setZonesActivation";

    private class HmipSystemRequestBody {
        private final String path;
        private HmipSystemRequestBodyBody body;

        public HmipSystemRequestBody(String path) {
            this.path = path;
            this.body = new HmipSystemRequestBodyBody();
        }
    }

    private final HmipSystemRequestBody body;

    public HmipSystemRequest(String path) {
        super(HMIP_SYSTEM_REQUEST_TYPE);
        this.body = new HmipSystemRequestBody(path);
    }

    public HmipSystemRequest withBody(HmipSystemRequestBodyBody body) {
        this.body.body = body;
        return this;
    }
}
