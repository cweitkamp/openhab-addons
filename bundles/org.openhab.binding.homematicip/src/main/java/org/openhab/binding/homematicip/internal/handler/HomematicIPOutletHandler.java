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
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HomematicIPOutletHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPOutletHandler extends HomematicIPAbstractDeviceHandler {

    public HomematicIPOutletHandler(Thing thing) {
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
            case CHANNEL_GROUP_CONTROLS + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER:
                handlePowerCommand(command);
                break;
            default:
                logger.debug("Channel '{}#{}' of Thing '{}' is read-only and cannot handle command '{}'.",
                        channelGroupId, channelId, getThing().getUID(), command);
                break;
        }
    }

    private void handlePowerCommand(Command command) {
        Bridge bridge = getBridge();
        if (bridge != null) {
            HomematicIPAccessPointHandler homematicIPAccessPointHandler = (HomematicIPAccessPointHandler) bridge
                    .getHandler();
            if (homematicIPAccessPointHandler != null) {
                if (command instanceof OnOffType) {
                    String deviceId = id;
                    if (deviceId != null) {
                        try {
                            homematicIPAccessPointHandler.setSwitchState(deviceId, command == OnOffType.ON);
                        } catch (ConfigurationException e) {
                            // do nothing
                        } catch (CommunicationException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
                        }

                    }
                }
            }
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
            case CHANNEL_GROUP_CONTROLS:
                updateControlChannel(channelUID,
                        functionalChannels.get(FUNCTIONAL_TYPE_CHANNEL_DEVICE_CHANNEL).getAsJsonObject());
        }
    }

    private void updateSensorChannel(ChannelUID channelUID, JsonObject data) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_POWER:
                JsonElement currentPowerConsumption = data.get(PROPERTY_CURRENT_POWER_CONSUMPTION);
                if (currentPowerConsumption.isJsonPrimitive()) {
                    state = HomematicIPUtils.getQuantityTypeState(currentPowerConsumption.getAsDouble(), Units.WATT);
                    break;
                }
            case CHANNEL_ENERGY:
                JsonElement energyCounter = data.get(PROPERTY_ENERGY_COUNTER);
                if (energyCounter.isJsonPrimitive()) {
                    state = HomematicIPUtils.getQuantityTypeState(energyCounter.getAsDouble(), Units.KILOWATT_HOUR);
                }
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }

    private void updateControlChannel(ChannelUID channelUID, JsonObject data) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_POWER:
                JsonElement on = data.get(PROPERTY_ON);
                if (on.isJsonPrimitive()) {
                    state = OnOffType.from(on.getAsBoolean());
                }
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }
}
