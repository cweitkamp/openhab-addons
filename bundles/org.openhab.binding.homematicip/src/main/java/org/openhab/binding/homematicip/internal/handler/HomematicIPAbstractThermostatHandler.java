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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homematicip.internal.dto.AbstractHeatingThermostat;
import org.openhab.binding.homematicip.internal.utils.HomematicIPUtils;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HomematicIPAbstractThermostatHandler} is responsible for handling commands, which are sent to one of
 * the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class HomematicIPAbstractThermostatHandler extends HomematicIPAbstractDeviceHandler {

    private static final String ERROR_CODE_CLIENT_ACCESS_DENIED = "CLIENT_ACCESS_DENIED";
    private static final String ERROR_CODE_NOT_HEATING_GROUP = "NOT_HEATING_GROUP";

    final Gson gson = new Gson();

    public HomematicIPAbstractThermostatHandler(Thing thing) {
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
            // TODO check if display contrast and display orientation is changeable via API
            case CHANNEL_GROUP_CONTROLS + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_DISPLAY_MODE:
                handleDisplayModeCommand(command);
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

    private void handleDisplayModeCommand(Command command) {
        Bridge bridge = getBridge();
        if (bridge != null) {
            HomematicIPAccessPointHandler homematicIPAccessPointHandler = (HomematicIPAccessPointHandler) bridge
                    .getHandler();
            if (homematicIPAccessPointHandler != null) {
                if (command instanceof StringType) {
                    String deviceId = id;
                    if (deviceId != null) {
                        try {
                            homematicIPAccessPointHandler.setClimateControlDisplayMode(deviceId, command.toString());
                        } catch (ConfigurationException e) {
                            if (ERROR_CODE_CLIENT_ACCESS_DENIED.equals(e.getRawMessage())) {
                                logger.warn(
                                        "Client has insufficient user role (e.g. 'RESTRICTED_USER') to handle display mode commands. Please change permissions at least to 'USER'.");
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        TEXT_OFFLINE_CONF_ERROR_INSUFFICIENT_USER_ROLE);
                            }
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
                JsonObject localData = data;
                if (localData != null) {
                    JsonObject functionalChannels = localData.get(PROPERTY_FUNCTIONAL_CHANNELS).getAsJsonObject();
                    AbstractHeatingThermostat thermostatData = gson.fromJson(
                            functionalChannels.get(FUNCTIONAL_TYPE_CHANNEL_DEVICE_CHANNEL),
                            AbstractHeatingThermostat.class);
                    if (thermostatData != null) {
                        for (String groupId : thermostatData.groups) {
                            try {
                                homematicIPAccessPointHandler.setSetPointTemperature(groupId, temperature);
                            } catch (ConfigurationException e) {
                                if (ERROR_CODE_NOT_HEATING_GROUP.equals(e.getRawMessage())) {
                                    // try next group
                                    continue;
                                }
                            } catch (CommunicationException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        e.getRawMessage());
                            }
                            break;
                        }
                    }
                }
            }
        }
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
            case CHANNEL_DISPLAY_CONTRAST:
                JsonElement displayContrast = data.get(PROPERTY_DISPLAY_CONTRAST);
                if (!displayContrast.isJsonNull()) {
                    state = HomematicIPUtils.getDecimalTypeState(displayContrast.getAsInt());
                }
                break;
            case CHANNEL_DISPLAY_MODE:
                JsonElement displayMode = data.get(PROPERTY_DISPLAY_MODE);
                if (!displayMode.isJsonNull()) {
                    state = HomematicIPUtils.getStringTypeState(displayMode.getAsString());
                }
                break;
            case CHANNEL_DISPLAY_ORIENTATION:
                JsonElement displayOrientation = data.get(PROPERTY_DISPLAY_ORIENTATION);
                if (!displayOrientation.isJsonNull()) {
                    state = HomematicIPUtils.getStringTypeState(displayOrientation.getAsString());
                }
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
}
