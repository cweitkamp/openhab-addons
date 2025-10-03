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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homematicip.internal.utils.HomematicIPUtils;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HomematicIPUnderfloorHeatingActuatorHandler} is responsible for handling commands, which are sent to one
 * of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPUnderfloorHeatingActuatorHandler extends HomematicIPAbstractDeviceHandler {

    private static final Pattern CHANNEL_GROUP_VALVE_PREFIX_PATTERN = Pattern.compile(CHANNEL_GROUP_VALVE + "(\\d+)");

    public HomematicIPUnderfloorHeatingActuatorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel '{}' of Thing '{}' is read-only and cannot handle command '{}'.", channelUID,
                getThing().getUID(), command);
    }

    @Override
    @SuppressWarnings("null")
    void updateChannel(ChannelUID channelUID, JsonObject functionalChannels) {
        String channelGroupId = channelUID.getGroupId();
        switch (channelGroupId) {
            // case CHANNEL_GROUP_VALVE:
            default:
                if (channelGroupId.startsWith(CHANNEL_GROUP_VALVE)) {
                    int i;
                    Matcher valveMatcher = CHANNEL_GROUP_VALVE_PREFIX_PATTERN.matcher(channelGroupId);
                    if (valveMatcher.find() && (i = Integer.parseInt(valveMatcher.group(1))) > 0) {
                        updateValveChannel(channelUID, functionalChannels.get(Integer.toString(i)).getAsJsonObject());
                    }
                }
                break;
        }
    }

    private void updateValveChannel(ChannelUID channelUID, JsonObject data) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_STATE:
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
