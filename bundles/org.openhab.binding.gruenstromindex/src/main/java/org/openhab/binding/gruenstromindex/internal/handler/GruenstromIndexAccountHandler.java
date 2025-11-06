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
package org.openhab.binding.gruenstromindex.internal.handler;

import static org.openhab.binding.gruenstromindex.internal.GruenstromIndexBindingConstants.TEXT_OFFLINE_CONF_ERROR_NOT_SUPPORTED_REFRESH_INTERVAL;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.gruenstromindex.internal.config.GruenstromIndexAccountConfiguration;
import org.openhab.binding.gruenstromindex.internal.config.GruenstromIndexZipcodeConfigOptionProvider;
import org.openhab.binding.gruenstromindex.internal.connection.GrunstromIndexConnection;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GruenstromIndexAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GruenstromIndexAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(GruenstromIndexAccountHandler.class);

    private static final Collection<Class<? extends ThingHandlerService>> SUPPORTED_THING_ACTIONS = Set
            .of(GruenstromIndexZipcodeConfigOptionProvider.class);
    private static final long INITIAL_DELAY_IN_SECONDS = 15;

    private @Nullable ScheduledFuture<?> refreshJob;

    private final HttpClient httpClient;
    private @NonNullByDefault({}) GrunstromIndexConnection connection;

    private @NonNullByDefault({}) GruenstromIndexAccountConfiguration config;

    public GruenstromIndexAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initialize GruenstromIndexAccount handler '{}'.", getThing().getUID());
        config = getConfigAs(GruenstromIndexAccountConfiguration.class);

        boolean configValid = true;
        int refreshInterval = config.refreshInterval;
        if (refreshInterval < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    TEXT_OFFLINE_CONF_ERROR_NOT_SUPPORTED_REFRESH_INTERVAL);
            configValid = false;
        }

        if (configValid) {
            connection = new GrunstromIndexConnection(this, httpClient);

            ScheduledFuture<?> localRefreshJob = refreshJob;
            if (localRefreshJob == null || localRefreshJob.isCancelled()) {
                logger.debug("Start refresh job at interval {} min.", refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateThings, INITIAL_DELAY_IN_SECONDS,
                        TimeUnit.MINUTES.toSeconds(refreshInterval), TimeUnit.SECONDS);
            }

            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.schedule(this::updateThings, INITIAL_DELAY_IN_SECONDS, TimeUnit.SECONDS);
        } else {
            logger.debug("The GruenstromIndex binding is a read-only binding and cannot handle command '{}'.", command);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return SUPPORTED_THING_ACTIONS;
    }

    public GruenstromIndexAccountConfiguration getConfiguration() {
        return config;
    }

    private void updateThings() {
        ThingStatus status = ThingStatus.ONLINE;
        List<Thing> childs = getThing().getThings().stream().filter(Thing::isEnabled).collect(Collectors.toList());
        if (!childs.isEmpty()) {
            status = ThingStatus.OFFLINE;
            for (Thing thing : childs) {
                if (ThingStatus.ONLINE
                        .equals(updateThing((GruenstromIndexGreenEnergyForecastHandler) thing.getHandler(), thing))) {
                    status = ThingStatus.ONLINE;
                }
            }
        }
        updateStatus(status);
    }

    private ThingStatus updateThing(@Nullable GruenstromIndexGreenEnergyForecastHandler handler, Thing thing) {
        if (handler != null && ThingHandlerHelper.isHandlerInitialized(handler) && connection != null) {
            handler.updateData(connection);
            return thing.getStatus();
        } else {
            logger.debug("Cannot update gree energy forecast data of thing '{}' as thing handler is null.",
                    thing.getUID());
            return ThingStatus.OFFLINE;
        }
    }
}
