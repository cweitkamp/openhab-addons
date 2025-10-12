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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homematicip.internal.HomematicIPDynamicStateDescriptionProvider;
import org.openhab.binding.homematicip.internal.dto.Profile;
import org.openhab.binding.homematicip.internal.utils.HomematicIPUtils;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HomematicIPHeatingGroupHandler} is responsible for handling commands, which are sent to one of
 * the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPHeatingGroupHandler extends HomematicIPAbstractDeviceHandler {

    final Gson gson = new Gson();

    private final HomematicIPDynamicStateDescriptionProvider homematicIPDynamicStateDescriptionProvider;

    public HomematicIPHeatingGroupHandler(Thing thing,
            HomematicIPDynamicStateDescriptionProvider homematicIPDynamicStateDescriptionProvider) {
        super(thing);
        this.homematicIPDynamicStateDescriptionProvider = homematicIPDynamicStateDescriptionProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        if (command == RefreshType.REFRESH) {
            logger.debug("Cannot handle command '{}' for Channel '{}#{}' of hing '{}'", command, channelGroupId,
                    channelId, getThing().getUID());
            return;
        }
        switch (channelGroupId + ChannelUID.CHANNEL_GROUP_SEPARATOR + channelId) {
            case CHANNEL_GROUP_CONTROLS + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_MODE:
                handleModeCommand(command);
                break;
            case CHANNEL_GROUP_CONTROLS + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_HEATING_PROFILE:
                handleHeatingProfileCommand(command);
                break;
            case CHANNEL_GROUP_CONTROLS + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_TEMPERATURE:
                handleTemperatureCommand(command);
                break;
            default:
                logger.debug("Channel '{}#{}' of Thing '{}' is read-only and cannot handle command '{}'.",
                        channelGroupId, channelId, getThing().getUID(), command);
                break;
        }
    }

    private void handleModeCommand(Command command) {
        Bridge bridge = getBridge();
        if (bridge != null) {
            HomematicIPAccessPointHandler homematicIPAccessPointHandler = (HomematicIPAccessPointHandler) bridge
                    .getHandler();
            if (homematicIPAccessPointHandler != null) {
                if (command instanceof StringType) {
                    String groupId = id;
                    if (groupId != null) {
                        try {
                            homematicIPAccessPointHandler.setControlMode(groupId, command.toString());
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

    private void handleHeatingProfileCommand(Command command) {
        Bridge bridge = getBridge();
        if (bridge != null) {
            HomematicIPAccessPointHandler homematicIPAccessPointHandler = (HomematicIPAccessPointHandler) bridge
                    .getHandler();
            if (homematicIPAccessPointHandler != null) {
                if (command instanceof StringType) {
                    String groupId = id;
                    if (groupId != null) {
                        try {
                            homematicIPAccessPointHandler.setActiveProfile(groupId, command.toString());
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

    private void handleTemperatureCommand(Command command) {
        if (command instanceof DecimalType) {
            setSetPointTemperature(((DecimalType) command).doubleValue());
        } else if (command instanceof QuantityType) {
            @SuppressWarnings("unchecked")
            QuantityType<Temperature> convertedCommand = ((QuantityType<Temperature>) command).toUnit(SIUnits.CELSIUS);
            if (convertedCommand != null) {
                setSetPointTemperature(convertedCommand.doubleValue());
            }
        }
    }

    private void setSetPointTemperature(double temperature) {
        Bridge bridge = getBridge();
        if (bridge != null) {
            HomematicIPAccessPointHandler homematicIPAccessPointHandler = (HomematicIPAccessPointHandler) bridge
                    .getHandler();
            if (homematicIPAccessPointHandler != null) {
                String groupId = id;
                if (groupId != null) {
                    try {
                        homematicIPAccessPointHandler.setSetPointTemperature(groupId, temperature);
                    } catch (ConfigurationException e) {
                        // do nothing
                    } catch (CommunicationException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getRawMessage());
                    }
                }
            }
        }
    }

    @Override
    public void updateChannels(JsonObject data) {
        getThing().getChannels().stream().filter(c -> ChannelKind.STATE.equals(c.getKind())).map(c -> c.getUID())
                .filter(ChannelUID::isInGroup).filter(uid -> uid.getGroupId() != null).filter(uid -> isLinked(uid))
                .forEach(channelUID -> {
                    updateChannel(channelUID, data);
                });
        this.data = data;
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    @SuppressWarnings("null")
    void updateChannel(ChannelUID channelUID, JsonObject data) {
        String channelGroupId = channelUID.getGroupId();
        switch (channelGroupId) {
            case CHANNEL_GROUP_SENSORS:
                updateSensorChannel(channelUID, data);
                break;
            case CHANNEL_GROUP_CONTROLS:
                updateControlChannel(channelUID, data);
                break;
        }
    }

    private void updateSensorChannel(ChannelUID channelUID, JsonObject data) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_HUMIDITY:
                JsonElement humidity = data.get(PROPERTY_HUMIDITY);
                if (humidity.isJsonPrimitive()) {
                    state = HomematicIPUtils.getQuantityTypeState(humidity.getAsDouble(), Units.PERCENT);
                }
                break;
            case CHANNEL_TEMPERATURE:
                JsonElement actualTemperature = data.get(PROPERTY_ACTUAL_TEMPERATURE);
                if (actualTemperature.isJsonPrimitive()) {
                    state = HomematicIPUtils.getQuantityTypeState(actualTemperature.getAsDouble(), SIUnits.CELSIUS);
                }
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }

    /**
     *
     * @param channelUID
     * @param data
     */
    protected void updateControlChannel(ChannelUID channelUID, JsonObject data) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_MODE:
                JsonElement controlMode = data.get(PROPERTY_CONTROL_MODE);
                if (!controlMode.isJsonNull()) {
                    state = HomematicIPUtils.getStringTypeState(controlMode.getAsString());
                }
                break;
            case CHANNEL_HEATING_PROFILE:
                JsonElement heatingProfile = data.get(PROPERTY_ACTIVE_PROFILE);
                if (!heatingProfile.isJsonNull()) {
                    state = HomematicIPUtils.getStringTypeState(heatingProfile.getAsString());
                }

                updateStateOptions(channelUID, data.get(PROPERTY_PROFILES).getAsJsonObject());
                break;
            case CHANNEL_TEMPERATURE:
                JsonElement setPointTemperature = data.get(PROPERTY_SET_POINT_TEMPERATURE);
                if (setPointTemperature.isJsonPrimitive()) {
                    state = HomematicIPUtils.getQuantityTypeState(setPointTemperature.getAsDouble(), SIUnits.CELSIUS);
                }
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }

    private void updateStateOptions(ChannelUID channelUID, JsonObject profileData) {
        List<StateOption> stateOptions = new ArrayList<>();
        Stream<@Nullable Profile> profiles = profileData.entrySet().stream()
                .map(e -> gson.fromJson(e.getValue(), Profile.class));
        profiles.forEach(p -> {
            if (p != null && p.enabled && p.visible && !p.name.isBlank()) {
                stateOptions.add(p.getAsStateOption());
            }
        });
        homematicIPDynamicStateDescriptionProvider.setStateOptions(channelUID, stateOptions);
    }
}
