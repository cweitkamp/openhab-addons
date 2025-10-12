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
package org.openhab.binding.homematicip.internal.utils;

import java.util.List;
import java.util.Optional;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homematicip.internal.dto.SecurityZone;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPUtils {

    public static final DecimalType SIGNAL_STRENGTH_NO_SIGNAL = DecimalType.valueOf("0");
    public static final DecimalType SIGNAL_STRENGTH_WEAK = DecimalType.valueOf("1");
    public static final DecimalType SIGNAL_STRENGTH_AVERAGE = DecimalType.valueOf("2");
    public static final DecimalType SIGNAL_STRENGTH_GOOD = DecimalType.valueOf("3");
    public static final DecimalType SIGNAL_STRENGTH_EXCELLENT = DecimalType.valueOf("4");

    public static final String ALARM_MODE_NONE = "NONE";
    public static final String ALARM_MODE_EXTERNAL = "EXTERNAL";
    public static final String ALARM_MODE_INTERNAL = "INTERNAL";
    public static final StringType STRINGTYPE_ALARM_MODE_NONE = StringType.valueOf(ALARM_MODE_NONE);
    public static final StringType STRINGTYPE_ALARM_MODE_EXTERNAL = StringType.valueOf(ALARM_MODE_EXTERNAL);
    public static final StringType STRINGTYPE_ALARM_MODE_INTERNAL = StringType.valueOf(ALARM_MODE_INTERNAL);

    /**
     * Maps the given RSSI value to a resulting signal strength. Returns a {@link DecimalType}.
     *
     * <ul>
     * <li>< -90: no signal</li>
     * <li>[-90..-80[: weak</li>
     * <li>[-80..-70[: average</li>
     * <li>[-70..-50[: good</li>
     * <li>>= -50: excellent</li>
     * </ul>
     *
     * @param rssi the RSSI value
     * @return the resulting signal strength
     */
    public static DecimalType mapRSSIValueToSignalStrength(int rssi) {
        if (rssi < -90) {
            return SIGNAL_STRENGTH_NO_SIGNAL;
        } else if (rssi >= -90 && rssi < -80) {
            return SIGNAL_STRENGTH_WEAK;
        } else if (rssi >= -80 && rssi < -70) {
            return SIGNAL_STRENGTH_AVERAGE;
        } else if (rssi >= -70 && rssi < -50) {
            return SIGNAL_STRENGTH_GOOD;
        } else { /* rssi >= -50 */
            return SIGNAL_STRENGTH_EXCELLENT;
        }
    }

    /**
     * Gets the alarm mode. If Returns a {@link StringType}.
     *
     * @param securityZones {@link List} of {@link SecurityZone}s
     * @return the resulting alarm mode
     */
    public static StringType getAlarmMode(List<@Nullable SecurityZone> securityZones) {
        @SuppressWarnings("null")
        Optional<@Nullable SecurityZone> internalSecurityZone = securityZones.stream()
                .filter(s -> ALARM_MODE_INTERNAL.equals(s.label)).findFirst();
        @SuppressWarnings("null")
        Optional<@Nullable SecurityZone> externalSecurityZone = securityZones.stream()
                .filter(s -> ALARM_MODE_EXTERNAL.equals(s.label)).findFirst();
        if (internalSecurityZone.isPresent() && internalSecurityZone.get().active) {
            return STRINGTYPE_ALARM_MODE_INTERNAL;
        } else if (externalSecurityZone.isPresent() && externalSecurityZone.get().active) {
            return STRINGTYPE_ALARM_MODE_EXTERNAL;
        }
        return STRINGTYPE_ALARM_MODE_NONE;
    }

    /**
     *
     * @param value
     * @return
     */
    public static State getDecimalTypeState(@Nullable Number value) {
        return (value == null) ? UnDefType.UNDEF : new DecimalType(value);
    }

    /**
     *
     * @param value
     * @return
     */
    public static State getOnOffTypeState(@Nullable Boolean value) {
        return (value == null) ? UnDefType.UNDEF : OnOffType.from(value);
    }

    /**
     *
     * @param value
     * @param unit
     * @return
     */
    public static State getQuantityTypeState(@Nullable Number value, Unit<?> unit) {
        return (value == null) ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    /**
     *
     * @param value
     * @return
     */
    public static State getStringTypeState(@Nullable String value) {
        return (value == null) ? UnDefType.UNDEF : new StringType(value);
    }
}
