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
package org.openhab.binding.homematicip.internal.handler;

import static org.openhab.binding.homematicip.internal.HomematicIPBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homematicip.internal.utils.HomematicIPUtils;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HomematicIPHeatingThermostatHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPHeatingThermostatHandler extends HomematicIPAbstractThermostatHandler {

    public HomematicIPHeatingThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    @SuppressWarnings("null")
    void updateChannel(ChannelUID channelUID, JsonObject functionalChannels) {
        String channelGroupId = channelUID.getGroupId();
        switch (channelGroupId) {
            case CHANNEL_GROUP_DEVICE:
                updateDeviceChannel(channelUID,
                        functionalChannels.get(FUNCTIONAL_TYPE_CHANNEL_DEVICE_BASE).getAsJsonObject());
                break;
            case CHANNEL_GROUP_SENSORS:
                updateSensorChannel(channelUID,
                        functionalChannels.get(FUNCTIONAL_TYPE_CHANNEL_DEVICE_CHANNEL).getAsJsonObject());
                break;
            case CHANNEL_GROUP_CONTROLS:
                updateControlChannel(channelUID,
                        functionalChannels.get(FUNCTIONAL_TYPE_CHANNEL_DEVICE_CHANNEL).getAsJsonObject());
                break;
            case CHANNEL_GROUP_VALVE:
                updateValveChannel(channelUID,
                        functionalChannels.get(FUNCTIONAL_TYPE_CHANNEL_DEVICE_CHANNEL).getAsJsonObject());
                break;
        }
    }

    private void updateSensorChannel(ChannelUID channelUID, JsonObject data) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_TEMPERATURE:
                JsonElement valveActualTemperature = data.get(PROPERTY_VALVE_ACTUAL_TEMPERATURE);
                if (valveActualTemperature.isJsonPrimitive()) {
                    state = HomematicIPUtils.getQuantityTypeState(valveActualTemperature.getAsDouble(),
                            SIUnits.CELSIUS);
                }
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }

    private void updateValveChannel(ChannelUID channelUID, JsonObject data) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_STATE:
                // TODO handle Thing status based on valve state (e.g. "InS" (not installed) or "AdA" (adaption run))
                JsonElement valveState = data.get(PROPERTY_VALVE_STATE);
                if (!valveState.isJsonNull()) {
                    state = HomematicIPUtils.getStringTypeState(valveState.getAsString());
                }
                break;
            case CHANNEL_POSITION:
                JsonElement valvePosition = data.get(PROPERTY_VALVE_POSITION);
                if (valvePosition.isJsonPrimitive()) {
                    state = HomematicIPUtils.getQuantityTypeState(valvePosition.getAsDouble() * 100d, Units.PERCENT);
                }
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }
}
