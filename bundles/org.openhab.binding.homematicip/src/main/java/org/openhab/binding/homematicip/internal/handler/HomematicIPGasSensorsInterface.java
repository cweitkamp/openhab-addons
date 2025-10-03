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
package org.openhab.binding.homematicip.internal.handler;

import static org.openhab.binding.homematicip.internal.HomematicIPBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homematicip.internal.utils.HomematicIPUtils;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HomematicIPGasSensorsInterface} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPGasSensorsInterface extends HomematicIPAbstractDeviceHandler {

    public HomematicIPGasSensorsInterface(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (command == RefreshType.REFRESH) {
            logger.debug("Cannot handle command '{}' for Channel '{}#{}' of Thing '{}'", command, channelGroupId,
                    channelId, getThing().getUID());
            return;
        }
        switch (channelGroupId + ChannelUID.CHANNEL_GROUP_SEPARATOR + channelId) {
            default:
                logger.debug("Channel '{}#{}' of Thing '{}' is read-only and cannot handle command '{}'.",
                        channelGroupId, channelId, getThing().getUID(), command);
                break;
        }
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
        }
    }

    @Override
    public void updateProperties(JsonObject data) {
        JsonObject functionalChannels = data.get(PROPERTY_FUNCTIONAL_CHANNELS).getAsJsonObject();
        JsonElement gasVolumePerImpulse = functionalChannels.get(FUNCTIONAL_TYPE_CHANNEL_DEVICE_CHANNEL)
                .getAsJsonObject().get(PROPERTY_GAS_VOLUME_PER_IMPULSE);
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, data.get(PROPERTY_FIRMWARE_VERSION).getAsString());
        properties.put(PROPERTY_GAS_VOLUME_PER_IMPULSE, gasVolumePerImpulse.getAsString() + " m³");
        updateProperties(properties);
    }

    private void updateSensorChannel(ChannelUID channelUID, JsonObject data) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_GAS_VOLUME:
                JsonElement gasVolume = data.get(PROPERTY_GAS_VOLUME);
                if (gasVolume.isJsonPrimitive()) {
                    state = HomematicIPUtils.getQuantityTypeState(gasVolume.getAsDouble(), SIUnits.CUBIC_METRE);
                    break;
                }
            case CHANNEL_GAS_FLOW:
                JsonElement currentGasFlow = data.get(PROPERTY_CURRENT_GAS_FLOW);
                if (currentGasFlow.isJsonPrimitive()) {
                    state = HomematicIPUtils.getQuantityTypeState(currentGasFlow.getAsDouble(),
                            Units.CUBICMETRE_PER_HOUR);
                }
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }
}
