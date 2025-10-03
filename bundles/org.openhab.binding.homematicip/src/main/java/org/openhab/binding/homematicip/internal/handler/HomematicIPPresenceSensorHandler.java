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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homematicip.internal.utils.HomematicIPUtils;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HomematicIPPresenceSensorHandler} is responsible for handling commands, which are sent to one of the
 * channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPPresenceSensorHandler extends HomematicIPAbstractDeviceHandler {

    public HomematicIPPresenceSensorHandler(Thing thing) {
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

    private void updateSensorChannel(ChannelUID channelUID, JsonObject data) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_PRESENCE:
                JsonElement presenceDetected = data.get(PROPERTY_PRESENCE_DETECTED);
                if (presenceDetected.isJsonPrimitive()) {
                    state = HomematicIPUtils.getOnOffTypeState(presenceDetected.getAsBoolean());
                    break;
                }
            case CHANNEL_LIGHT_LEVEL:
                JsonElement illumination = data.get(PROPERTY_ILLUMINATION);
                if (illumination.isJsonPrimitive()) {
                    state = HomematicIPUtils.getDecimalTypeState(illumination.getAsDouble());
                }
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }
}
