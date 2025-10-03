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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homematicip.internal.config.HomematicIPDeviceConfiguration;
import org.openhab.binding.homematicip.internal.connection.HomematicIPWebSocketListener;
import org.openhab.binding.homematicip.internal.utils.HomematicIPUtils;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link HomematicIPAbstractDeviceHandler} is responsible for handling commands, which are sent to one
 * of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class HomematicIPAbstractDeviceHandler extends BaseThingHandler
        implements HomematicIPWebSocketListener {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Nullable
    String id;

    // keeps track of the data to handle commands
    @Nullable
    JsonObject data;

    public HomematicIPAbstractDeviceHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        HomematicIPDeviceConfiguration config = getConfigAs(HomematicIPDeviceConfiguration.class);

        boolean configValid = true;
        if (config.id == null || config.id.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    TEXT_OFFLINE_CONF_ERROR_MISSING_ID);
            configValid = false;
        } else {
            this.id = config.id;
        }

        if (configValid) {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (ThingStatus.ONLINE.equals(bridgeStatusInfo.getStatus())
                && ThingStatusDetail.BRIDGE_OFFLINE.equals(getThing().getStatusInfo().getStatusDetail())) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else if (ThingStatus.OFFLINE.equals(bridgeStatusInfo.getStatus())
                && !ThingStatus.OFFLINE.equals(getThing().getStatus())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void onConnect() {
        // nothing to do - handled in {@link HomematicIPAccessPointHandler#onConnect()}
    }

    @Override
    public void onClose(String reason) {
        // nothing to do - handled in {@link HomematicIPAccessPointHandler#onClose()}
    }

    @Override
    public void onError() {
        // nothing to do - handled in {@link HomematicIPAccessPointHandler#onError()}
    }

    @Override
    public void messageReceived(JsonObject data) {
        updateChannels(data);
    }

    /**
     * Updates all channels of this handler from the latest data retrieved.
     *
     * @param data The Json data
     */
    public void updateChannels(JsonObject data) {
        JsonObject functionalChannels = data.get(PROPERTY_FUNCTIONAL_CHANNELS).getAsJsonObject();
        getThing().getChannels().stream().filter(c -> ChannelKind.STATE.equals(c.getKind())).map(c -> c.getUID())
                .filter(ChannelUID::isInGroup).filter(uid -> uid.getGroupId() != null).filter(uid -> isLinked(uid))
                .forEach(channelUID -> {
                    updateChannel(channelUID, functionalChannels);
                });
        // determine Thing status based on "unreach" flag
        JsonElement unreach = functionalChannels.get(FUNCTIONAL_TYPE_CHANNEL_DEVICE_BASE).getAsJsonObject()
                .get(PROPERTY_UNREACH);
        if (unreach.isJsonPrimitive() && unreach.getAsBoolean()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    TEXT_OFFLINE_COMMUNICATION_ERROR_DEVICE_NOT_REACHABLE);
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
        this.data = data;
    }

    /**
     *
     * @param channelUID
     * @param functionalChannels
     */
    abstract void updateChannel(ChannelUID channelUID, JsonObject functionalChannels);

    /**
     *
     * @param channelUID
     * @param data
     */
    protected void updateDeviceChannel(ChannelUID channelUID, JsonObject data) {
        String channelId = channelUID.getIdWithoutGroup();
        String channelGroupId = channelUID.getGroupId();
        State state = UnDefType.UNDEF;
        switch (channelId) {
            case CHANNEL_LOCKED:
                JsonElement operationLockActive = data.get(PROPERTY_OPERATION_LOCK_ACTIVE);
                if (operationLockActive.isJsonPrimitive()) {
                    state = operationLockActive.getAsBoolean() ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                }
                break;
            case CHANNEL_LOW_BATTERY:
                JsonElement lowBat = data.get(PROPERTY_LOW_BAT);
                if (lowBat.isJsonPrimitive()) {
                    state = OnOffType.from(lowBat.getAsBoolean());
                }
                break;
            case CHANNEL_SIGNAL_STRENGTH:
                JsonElement rssiDeviceValue = data.get(PROPERTY_RSSI_DEVICE_VALUE);
                if (rssiDeviceValue.isJsonPrimitive()) {
                    state = HomematicIPUtils.mapRSSIValueToSignalStrength(rssiDeviceValue.getAsInt());
                }
                break;
        }
        logger.debug("Update channel '{}' of group '{}' with new state '{}'.", channelId, channelGroupId, state);
        updateState(channelUID, state);
    }

    /**
     * Updates all properties of this handler from the latest data retrieved.
     *
     * @param data The Json data
     */
    public void updateProperties(JsonObject data) {
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, data.get(PROPERTY_FIRMWARE_VERSION).getAsString());
        updateProperties(properties);
    }
}
