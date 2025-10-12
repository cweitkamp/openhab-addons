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
package org.openhab.binding.homematicip.internal;

import static org.openhab.binding.homematicip.internal.HomematicIPBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.homematicip.internal.handler.HomematicIPAccessPointHandler;
import org.openhab.binding.homematicip.internal.handler.HomematicIPEnergySensorsInterface;
import org.openhab.binding.homematicip.internal.handler.HomematicIPGasSensorsInterface;
import org.openhab.binding.homematicip.internal.handler.HomematicIPHeatingGroupHandler;
import org.openhab.binding.homematicip.internal.handler.HomematicIPHeatingThermostatHandler;
import org.openhab.binding.homematicip.internal.handler.HomematicIPOutletHandler;
import org.openhab.binding.homematicip.internal.handler.HomematicIPPresenceSensorHandler;
import org.openhab.binding.homematicip.internal.handler.HomematicIPShutterContactHandler;
import org.openhab.binding.homematicip.internal.handler.HomematicIPUnderfloorHeatingActuatorHandler;
import org.openhab.binding.homematicip.internal.handler.HomematicIPWallMountedThermostatHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HomematicIPHandlerFactory} is responsible for creating Things and ThingHandlers.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.homematicip", service = ThingHandlerFactory.class)
public class HomematicIPHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BRIDGE_TYPE_ACCESS_POINT,
            THING_TYPE_ENERGY_SENSORS_INTERFACE, THING_TYPE_GAS_SENSORS_INTERFACE, THING_TYPE_SHUTTER_CONTACT,
            THING_TYPE_THERMOSTAT, THING_TYPE_THERMOSTAT_EVO, THING_TYPE_WALL_MOUNTED_THERMOSTAT,
            THING_TYPE_UNDERFLOOR_HEATING_ACTUATOR, THING_TYPE_OUTLET, THING_TYPE_PRESENCE_SENSOR,
            THING_TYPE_HEATING_GROUP);

    private final HttpClient httpClient;
    private final WebSocketFactory webSocketFactory;
    private final HomematicIPDynamicStateDescriptionProvider homematicIPDynamicStateDescriptionProvider;

    @Activate
    public HomematicIPHandlerFactory(final @Reference HttpClientFactory httpClientFactory, //
            final @Reference WebSocketFactory webSocketFactory, //
            final @Reference HomematicIPDynamicStateDescriptionProvider homematicIPDynamicStateDescriptionProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.webSocketFactory = webSocketFactory;
        this.homematicIPDynamicStateDescriptionProvider = homematicIPDynamicStateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_TYPE_ACCESS_POINT.equals(thingTypeUID)) {
            return new HomematicIPAccessPointHandler((Bridge) thing, httpClient, webSocketFactory);
        } else if (THING_TYPE_ENERGY_SENSORS_INTERFACE.equals(thingTypeUID)) {
            return new HomematicIPEnergySensorsInterface(thing);
        } else if (THING_TYPE_GAS_SENSORS_INTERFACE.equals(thingTypeUID)) {
            return new HomematicIPGasSensorsInterface(thing);
        } else if (THING_TYPE_SHUTTER_CONTACT.equals(thingTypeUID)) {
            return new HomematicIPShutterContactHandler(thing);
        } else if (THING_TYPE_THERMOSTAT.equals(thingTypeUID) || THING_TYPE_THERMOSTAT_EVO.equals(thingTypeUID)) {
            return new HomematicIPHeatingThermostatHandler(thing);
        } else if (THING_TYPE_WALL_MOUNTED_THERMOSTAT.equals(thingTypeUID)) {
            return new HomematicIPWallMountedThermostatHandler(thing);
        } else if (THING_TYPE_UNDERFLOOR_HEATING_ACTUATOR.equals(thingTypeUID)) {
            return new HomematicIPUnderfloorHeatingActuatorHandler(thing);
        } else if (THING_TYPE_OUTLET.equals(thingTypeUID)) {
            return new HomematicIPOutletHandler(thing);
        } else if (THING_TYPE_PRESENCE_SENSOR.equals(thingTypeUID)) {
            return new HomematicIPPresenceSensorHandler(thing);
        } else if (THING_TYPE_HEATING_GROUP.equals(thingTypeUID)) {
            return new HomematicIPHeatingGroupHandler(thing, homematicIPDynamicStateDescriptionProvider);
        }

        return null;
    }
}
