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
package org.openhab.binding.homematicip.internal.discovery;

import static org.openhab.binding.homematicip.internal.HomematicIPBindingConstants.*;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homematicip.internal.handler.HomematicIPAccessPointHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HomematicIPDeviceDiscoveryService} creates Things based on the found devices on the Access Point.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPDeviceDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final String ENERGY_SENSOR_IEC = "ES_IEC";
    private static final String ENERGY_SENSOR_GAS = "ES_GAS";

    private static final String ENERGY_SENSORS_INTERFACE = "ENERGY_SENSORS_INTERFACE";
    private static final String FLOOR_TERMINAL_BLOCK_12 = "FLOOR_TERMINAL_BLOCK_12";
    private static final String HEATING_THERMOSTAT = "HEATING_THERMOSTAT";
    private static final String HEATING_THERMOSTAT_EVO = "HEATING_THERMOSTAT_EVO";
    private static final String PLUGABLE_SWITCH_MEASURING = "PLUGABLE_SWITCH_MEASURING";
    private static final String PRESENCE_DETECTOR_INDOOR = "PRESENCE_DETECTOR_INDOOR";
    private static final String SHUTTER_CONTACT_INVISIBLE = "SHUTTER_CONTACT_INVISIBLE";
    private static final String SHUTTER_CONTACT_MAGNETIC = "SHUTTER_CONTACT_MAGNETIC";
    private static final String WALL_MOUNTED_THERMOSTAT_PRO = "WALL_MOUNTED_THERMOSTAT_PRO";

    private static final String THING_PROPERTY_ID = "id";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_ENERGY_SENSORS_INTERFACE,
            THING_TYPE_GAS_SENSORS_INTERFACE, THING_TYPE_SHUTTER_CONTACT, THING_TYPE_THERMOSTAT,
            THING_TYPE_THERMOSTAT_EVO, THING_TYPE_WALL_MOUNTED_THERMOSTAT, THING_TYPE_UNDERFLOOR_HEATING_ACTUATOR,
            THING_TYPE_OUTLET, THING_TYPE_PRESENCE_SENSOR, THING_TYPE_HEATING_GROUP);

    private static final int TIMEOUT = 10;

    private final Logger logger = LoggerFactory.getLogger(HomematicIPDeviceDiscoveryService.class);

    private @Nullable HomematicIPAccessPointHandler accessPointHandler;

    public HomematicIPDeviceDiscoveryService() {
        super(SUPPORTED_THING_TYPES, TIMEOUT, false);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof HomematicIPAccessPointHandler) {
            accessPointHandler = (HomematicIPAccessPointHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return accessPointHandler;
    }

    @Override
    public void activate() {
        super.activate(null);
        final HomematicIPAccessPointHandler handler = accessPointHandler;
        if (handler != null) {
            handler.registerDiscoveryService(this);
        }
    }

    @Override
    public void deactivate() {
        final HomematicIPAccessPointHandler handler = accessPointHandler;
        if (handler != null) {
            handler.unregisterDiscoveryListener();
        }
        super.deactivate();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public void startScan() {
        logger.debug("Start manual homematic IP devices scan.");
        final HomematicIPAccessPointHandler handler = accessPointHandler;
        if (handler != null) {
            handler.updateData();
        }
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop manual homematic IP devices scan.");
        super.stopScan();
    }

    public void onDeviceAdded(String deviceId, JsonObject device) {
        logger.trace("Discovered new device with data: {}", device);

        final HomematicIPAccessPointHandler handler = accessPointHandler;
        if (handler != null) {
            ThingTypeUID thingTypeUID = null;
            String type = device.get(PROPERTY_TYPE).getAsString();
            switch (type) {
                case ENERGY_SENSORS_INTERFACE:
                    JsonObject functionalChannels = device.get(PROPERTY_FUNCTIONAL_CHANNELS).getAsJsonObject();
                    JsonElement connectedEnergySensorType = functionalChannels
                            .get(FUNCTIONAL_TYPE_CHANNEL_DEVICE_CHANNEL).getAsJsonObject()
                            .get(PROPERTY_CONNECTED_ENERGY_SENSOR_TYPE);
                    switch (connectedEnergySensorType.getAsString()) {
                        case ENERGY_SENSOR_IEC:
                            thingTypeUID = THING_TYPE_ENERGY_SENSORS_INTERFACE;
                            break;
                        case ENERGY_SENSOR_GAS:
                            thingTypeUID = THING_TYPE_GAS_SENSORS_INTERFACE;
                            break;
                        default:
                            logger.debug(
                                    "Discovered unknown energy sensor interface type '{}'. Please contact the binding developer.",
                                    connectedEnergySensorType);
                    }
                    break;
                case FLOOR_TERMINAL_BLOCK_12:
                    thingTypeUID = THING_TYPE_UNDERFLOOR_HEATING_ACTUATOR;
                    break;
                case HEATING_THERMOSTAT:
                    thingTypeUID = THING_TYPE_THERMOSTAT;
                    break;
                case HEATING_THERMOSTAT_EVO:
                    thingTypeUID = THING_TYPE_THERMOSTAT_EVO;
                    break;
                case PLUGABLE_SWITCH_MEASURING:
                    thingTypeUID = THING_TYPE_OUTLET;
                    break;
                case PRESENCE_DETECTOR_INDOOR:
                    thingTypeUID = THING_TYPE_PRESENCE_SENSOR;
                    break;
                case SHUTTER_CONTACT_INVISIBLE:
                case SHUTTER_CONTACT_MAGNETIC:
                    thingTypeUID = THING_TYPE_SHUTTER_CONTACT;
                    break;
                case WALL_MOUNTED_THERMOSTAT_PRO:
                    thingTypeUID = THING_TYPE_WALL_MOUNTED_THERMOSTAT;
                    break;
                default:
                    logger.debug("Discovered unknown device of type '{}'. Please contact the binding developer.", type);
            }

            if (thingTypeUID != null) {
                ThingUID bridgeUID = handler.getThing().getUID();
                ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, deviceId);
                Map<String, Object> properties = Map.of( //
                        THING_PROPERTY_ID, deviceId, //
                        Thing.PROPERTY_VENDOR, device.get(PROPERTY_OEM).getAsString(), //
                        Thing.PROPERTY_MODEL_ID, device.get(PROPERTY_MODEL_TYPE).getAsString(), //
                        Thing.PROPERTY_FIRMWARE_VERSION, device.get(PROPERTY_FIRMWARE_VERSION).getAsString() //
                );

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withRepresentationProperty(THING_PROPERTY_ID).withBridge(bridgeUID)
                        .withLabel(device.get(PROPERTY_LABEL).getAsString()).build();

                thingDiscovered(discoveryResult);
            }
        }
    }

    public void onGroupAdded(String groupId, JsonObject group) {
        logger.trace("Discovered new group with data: {}", group);

        final HomematicIPAccessPointHandler handler = accessPointHandler;
        if (handler != null) {
            ThingTypeUID thingTypeUID = null;
            String discoveryResultLabel = "";
            String type = group.get(PROPERTY_TYPE).getAsString();
            switch (type) {
                case HEATING_GROUP:
                    discoveryResultLabel = "homematic IP Heizgruppe - " + group.get(PROPERTY_LABEL).getAsString();
                    thingTypeUID = THING_TYPE_HEATING_GROUP;
                    break;
                case SECURITY_ZONE_GROUP:
                    // this group can be skipped as it will be handled by the Access Point Handler in a separate Channel
                    // 'alarm-mode'
                    break;
                case META_GROUP:
                case SECURITY_GROUP:
                case SWITCHING_GROUP:
                    // TODO implement META_GROUP, SECURITY_GROUP and/or SWITCHING_GROUP
                    break;
                default:
                    logger.debug("Discovered unknown group of type '{}'. Please contact the binding developer.", type);
            }

            if (thingTypeUID != null) {
                ThingUID bridgeUID = handler.getThing().getUID();
                ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, groupId);
                Map<String, Object> properties = Map.of(THING_PROPERTY_ID, groupId);

                // TODO: translate label of group
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                        .withRepresentationProperty(THING_PROPERTY_ID).withBridge(bridgeUID)
                        .withLabel(discoveryResultLabel).build();

                thingDiscovered(discoveryResult);
            }

        }
    }
}
