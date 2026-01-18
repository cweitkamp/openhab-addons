/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.gruenstromindex.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GruenstromIndexBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GruenstromIndexBindingConstants {

    public static final String BINDING_ID = "gruenstromindex";

    public static final String ACCOUNT = "account";
    public static final String GREEN_ENERGY_FORECAST = "green-energy-forecast";

    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, ACCOUNT);
    public static final ThingTypeUID THING_TYPE_GREEN_ENERGY_FORECAST = new ThingTypeUID(BINDING_ID,
            GREEN_ENERGY_FORECAST);

    public static final String CONFIG_ZIPCODE = "zipcode";

    public static final String CHANNEL_GROUP_ENERGY_FORECAST = "energy-forecast";

    public static final String CHANNEL_FORECASTED_GRUENSTROMINDEX = "gruenstromindex";
    public static final String CHANNEL_FORECASTED_CARBONDIOXIDE_EMISSIONS = "carbondioxide-emissions";

    public static final String TEXT_OFFLINE_CONF_ERROR_NOT_SUPPORTED_REFRESH_INTERVAL = "@text/offline.conf-error-not-supported-refreshInterval";
    public static final String TEXT_OFFLINE_CONF_ERROR_MISSING_ZIPCODE = "@text/offline.conf-error-missing-zipcode";
}
