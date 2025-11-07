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
package org.openhab.binding.gruenstromindex.internal.config;

import static org.openhab.binding.gruenstromindex.internal.GruenstromIndexBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gruenstromindex.internal.dto.Location;
import org.openhab.binding.gruenstromindex.internal.handler.GruenstromIndexGreenEnergyForecastHandler;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link GruenstromIndexZipcodeConfigOptionProvider} class contains fields mapping thing configuration parameters.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = { GruenstromIndexZipcodeConfigOptionProvider.class,
        ConfigOptionProvider.class })
public class GruenstromIndexZipcodeConfigOptionProvider implements ConfigOptionProvider, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(GruenstromIndexZipcodeConfigOptionProvider.class);

    private @Nullable GruenstromIndexGreenEnergyForecastHandler thingHandler;

    private List<Location> zipcodes = List.of();

    private final Gson gson = new Gson();

    @Activate
    @Override
    public void activate() {
        Location[] locations = getObjectFromJson("/zipcodes.de.json", Location[].class);
        if (locations != null) {
            zipcodes = Arrays.asList(locations);
        }
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if (GREEN_ENERGY_FORECAST.equals(uri.getSchemeSpecificPart()) && CONFIG_ZIPCODE.equals(param)) {
            return zipcodes.stream().map(Location::getAsParameterOption)
                    .sorted(Comparator.comparing(ParameterOption::getValue)).collect(Collectors.toUnmodifiableList());
        }
        return null;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.thingHandler = (GruenstromIndexGreenEnergyForecastHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return thingHandler;
    }

    /**
     *
     *
     * @return
     */
    public List<Location> getZipcodes() {
        return Collections.unmodifiableList(zipcodes);
    }

    private @Nullable <T> T getObjectFromJson(String filename, Class<T> clazz) {
        try (InputStream inputStream = GruenstromIndexZipcodeConfigOptionProvider.class.getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IOException("InputStream is null");
            }
            byte[] bytes = inputStream.readAllBytes();
            if (bytes == null) {
                throw new IOException("Resulting byte-array empty");
            }
            String json = new String(bytes, StandardCharsets.UTF_8);
            return gson.fromJson(json, clazz);
        } catch (IOException e) {
            logger.error("Unable to load zipcodes: ", e);
        }
        return null;
    }
}
