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
package org.openhab.binding.homematicip.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HomematicIPBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPBindingConstants {

    public static final String HTTP_HEADER_VERSION = "VERSION";
    public static final String HTTP_HEADER_AUTHTOKEN = "AUTHTOKEN";
    public static final String HTTP_HEADER_CLIENTAUTH = "CLIENTAUTH";

    public static final String TEXT_OFFLINE_COMMUNICATION_ERROR = "@text/offline.communication-error";
    public static final String TEXT_OFFLINE_COMMUNICATION_ERROR_DEVICE_NOT_REACHABLE = "@text/offline.communitcation-error-device-not-reachable";
    public static final String TEXT_OFFLINE_WEBSOCKET_CLOSED = "@text/offline.websocket-closed";
    public static final String TEXT_OFFLINE_WEBSOCKET_ERROR = "@text/offline.websocket-error";
    public static final String TEXT_OFFLINE_WEBSOCKET_SSL_ERROR = "@text/offline.websocket-ssl-error";
    public static final String TEXT_OFFLINE_CONF_ERROR_UNKNOWN = "@text/offline.conf-error-unknown";
    public static final String TEXT_OFFLINE_CONF_ERROR_MISSING_ID = "@text/offline.conf-error-missing-id";
    public static final String TEXT_OFFLINE_CONF_ERROR_INSUFFICIENT_USER_ROLE = "@text/offline.conf-error-insufficient-user-role";

    public static final String BINDING_ID = "homematicip";

    public static final ThingTypeUID BRIDGE_TYPE_ACCESS_POINT = new ThingTypeUID(BINDING_ID, "access-point");
    public static final ThingTypeUID THING_TYPE_ENERGY_SENSORS_INTERFACE = new ThingTypeUID(BINDING_ID,
            "energy-sensors-interface");
    public static final ThingTypeUID THING_TYPE_GAS_SENSORS_INTERFACE = new ThingTypeUID(BINDING_ID,
            "gas-sensors-interface");
    public static final ThingTypeUID THING_TYPE_SHUTTER_CONTACT = new ThingTypeUID(BINDING_ID, "shutter-contact");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT_EVO = new ThingTypeUID(BINDING_ID, "thermostat-evo");
    public static final ThingTypeUID THING_TYPE_WALL_MOUNTED_THERMOSTAT = new ThingTypeUID(BINDING_ID,
            "wall-mounted-thermostat");
    public static final ThingTypeUID THING_TYPE_UNDERFLOOR_HEATING_ACTUATOR = new ThingTypeUID(BINDING_ID,
            "underfloor-heating-actuator");
    public static final ThingTypeUID THING_TYPE_OUTLET = new ThingTypeUID(BINDING_ID, "outlet");
    public static final ThingTypeUID THING_TYPE_PRESENCE_SENSOR = new ThingTypeUID(BINDING_ID, "presence-sensor");
    public static final ThingTypeUID THING_TYPE_HEATING_GROUP = new ThingTypeUID(BINDING_ID, "heating-group");

    public static final String CHANNEL_GROUP_WAETHER = "weather";
    public static final String CHANNEL_GROUP_SECURITY = "security";
    public static final String CHANNEL_GROUP_DEVICE = "device";
    public static final String CHANNEL_GROUP_SENSORS = "sensors";
    public static final String CHANNEL_GROUP_CONTROLS = "controls";
    public static final String CHANNEL_GROUP_VALVE = "valve";

    public static final String CHANNEL_WIND_DIRECTION = "wind-direction";
    public static final String CHANNEL_WIND_SPEED = "wind-speed";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_MAX_TEMPERATURE = "max-temperature";
    public static final String CHANNEL_MIN_TEMPERATURE = "min-temperature";
    public static final String CHANNEL_CONDITION = "condition";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_ALARM_MODE = "alarm-mode";
    public static final String CHANNEL_LOW_BATTERY = "low-battery";
    public static final String CHANNEL_SIGNAL_STRENGTH = "signal-strength";
    public static final String CHANNEL_LOCKED = "locked";
    public static final String CHANNEL_DISPLAY_CONTRAST = "display-contrast";
    public static final String CHANNEL_DISPLAY_MODE = "display-mode";
    public static final String CHANNEL_DISPLAY_ORIENTATION = "display-orientation";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_CONTACT_STATE = "contact-state";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_PRESENCE = "presence";
    public static final String CHANNEL_ENERGY = "energy";
    public static final String CHANNEL_ENERGY_ONE = "energy-one";
    public static final String CHANNEL_ENERGY_TWO = "energy-two";
    public static final String CHANNEL_ENERGY_THREE = "energy-three";
    public static final String CHANNEL_GAS_FLOW = "gas-flow";
    public static final String CHANNEL_GAS_VOLUME = "gas-volume";
    public static final String CHANNEL_HEATING_PROFILE = "heating-profile";
    public static final String CHANNEL_LIGHT_LEVEL = "light-level";
    public static final String CHANNEL_MODE = "mode";

    public static final String PROPERTY_WEATHER = "weather";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_FIRMWARE_VERSION = "firmwareVersion";
    public static final String PROPERTY_MODEL_TYPE = "modelType";
    public static final String PROPERTY_OEM = "oem";
    public static final String PROPERTY_LABEL = "label";
    public static final String PROPERTY_FUNCTIONAL_CHANNELS = "functionalChannels";
    public static final String FUNCTIONAL_TYPE_CHANNEL_DEVICE_BASE = "0";
    public static final String FUNCTIONAL_TYPE_CHANNEL_DEVICE_CHANNEL = "1";
    public static final String PROPERTY_UNREACH = "unreach";
    public static final String PROPERTY_LOW_BAT = "lowBat";
    public static final String PROPERTY_RSSI_DEVICE_VALUE = "rssiDeviceValue";
    public static final String PROPERTY_WINDOW_STATE = "windowState";
    public static final String PROPERTY_SET_POINT_TEMPERATURE = "setPointTemperature";
    public static final String PROPERTY_ACTUAL_TEMPERATURE = "actualTemperature";
    public static final String PROPERTY_HUMIDITY = "humidity";
    public static final String PROPERTY_VALVE_ACTUAL_TEMPERATURE = "valveActualTemperature";
    public static final String PROPERTY_VALVE_STATE = "valveState";
    public static final String PROPERTY_VALVE_POSITION = "valvePosition";
    public static final String PROPERTY_OPERATION_LOCK_ACTIVE = "operationLockActive";
    public static final String PROPERTY_DISPLAY_CONTRAST = "displayContrast";
    public static final String PROPERTY_DISPLAY_MODE = "display";
    public static final String PROPERTY_DISPLAY_ORIENTATION = "mountingOrientation";
    public static final String PROPERTY_ON = "on";
    public static final String PROPERTY_CONNECTED_ENERGY_SENSOR_TYPE = "connectedEnergySensorType";
    public static final String PROPERTY_CURRENT_POWER_CONSUMPTION = "currentPowerConsumption";
    public static final String PROPERTY_ENERGY_COUNTER = "energyCounter";
    public static final String PROPERTY_ENERGY_COUNTER_ONE = "energyCounterOne";
    public static final String PROPERTY_ENERGY_COUNTER_TWO = "energyCounterTwo";
    public static final String PROPERTY_ENERGY_COUNTER_THREE = "energyCounterThree";
    public static final String PROPERTY_CURRENT_GAS_FLOW = "currentGasFlow";
    public static final String PROPERTY_GAS_VOLUME_PER_IMPULSE = "gasVolumePerImpulse";
    public static final String PROPERTY_GAS_VOLUME = "gasVolume";
    public static final String PROPERTY_ACTIVE_PROFILE = "activeProfile";
    public static final String PROPERTY_PROFILES = "profiles";
    public static final String PROPERTY_CONTROL_MODE = "controlMode";
    public static final String PROPERTY_PRESENCE_DETECTED = "presenceDetected";
    public static final String PROPERTY_ILLUMINATION = "illumination";

    public static final String HEATING_GROUP = "HEATING";
    public static final String META_GROUP = "META";
    public static final String SECURITY_GROUP = "SECURITY";
    public static final String SECURITY_ZONE_GROUP = "SECURITY_ZONE";
    public static final String SWITCHING_GROUP = "SWITCHING";
}
