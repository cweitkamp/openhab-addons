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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.homematicip.internal.dto.SecurityZone;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;

/**
 * Test cases for {@link HomematicIPUtils}.
 *
 * @author Christoph Weitkamp - Initial Contribution
 */
@NonNullByDefault
class HomematicIPUtilsTest {

    private static final Stream<Arguments> rssiConfigurations() {
        return Stream.of(Arguments.of(-180, HomematicIPUtils.SIGNAL_STRENGTH_NO_SIGNAL), //
                Arguments.of(-91, HomematicIPUtils.SIGNAL_STRENGTH_NO_SIGNAL), //
                Arguments.of(-90, HomematicIPUtils.SIGNAL_STRENGTH_WEAK), //
                Arguments.of(-81, HomematicIPUtils.SIGNAL_STRENGTH_WEAK), //
                Arguments.of(-80, HomematicIPUtils.SIGNAL_STRENGTH_AVERAGE), //
                Arguments.of(-71, HomematicIPUtils.SIGNAL_STRENGTH_AVERAGE), //
                Arguments.of(-70, HomematicIPUtils.SIGNAL_STRENGTH_GOOD), //
                Arguments.of(-51, HomematicIPUtils.SIGNAL_STRENGTH_GOOD), //
                Arguments.of(-50, HomematicIPUtils.SIGNAL_STRENGTH_EXCELLENT), //
                Arguments.of(0, HomematicIPUtils.SIGNAL_STRENGTH_EXCELLENT));
    }

    private static SecurityZone createSecurityZone(String label, boolean active) {
        SecurityZone securityZone = new SecurityZone();
        securityZone.label = label;
        securityZone.active = active;
        return securityZone;
    }

    private static final Stream<Arguments> alarmModeConfigurations() {
        return Stream.of(Arguments.of(List.of(), HomematicIPUtils.STRINGTYPE_ALARM_MODE_NONE), //
                Arguments.of(List.of(createSecurityZone(HomematicIPUtils.ALARM_MODE_INTERNAL, true)),
                        HomematicIPUtils.STRINGTYPE_ALARM_MODE_INTERNAL), //
                Arguments.of(
                        List.of(createSecurityZone(HomematicIPUtils.ALARM_MODE_INTERNAL, true),
                                createSecurityZone(HomematicIPUtils.ALARM_MODE_EXTERNAL, true)),
                        HomematicIPUtils.STRINGTYPE_ALARM_MODE_INTERNAL), //
                Arguments.of(List.of(createSecurityZone(HomematicIPUtils.ALARM_MODE_EXTERNAL, true)),
                        HomematicIPUtils.STRINGTYPE_ALARM_MODE_EXTERNAL), //
                Arguments.of(
                        List.of(createSecurityZone(HomematicIPUtils.ALARM_MODE_INTERNAL, false),
                                createSecurityZone(HomematicIPUtils.ALARM_MODE_EXTERNAL, true)),
                        HomematicIPUtils.STRINGTYPE_ALARM_MODE_EXTERNAL), //
                Arguments.of(List.of(createSecurityZone(HomematicIPUtils.ALARM_MODE_EXTERNAL, false)),
                        HomematicIPUtils.STRINGTYPE_ALARM_MODE_NONE),
                Arguments.of(
                        List.of(createSecurityZone(HomematicIPUtils.ALARM_MODE_INTERNAL, false),
                                createSecurityZone(HomematicIPUtils.ALARM_MODE_EXTERNAL, false)),
                        HomematicIPUtils.STRINGTYPE_ALARM_MODE_NONE));
    }

    @ParameterizedTest
    @MethodSource("rssiConfigurations")
    void testRSSIMapping(int rssi, DecimalType expectedSignalStrength) {
        assertEquals(expectedSignalStrength, HomematicIPUtils.mapRSSIValueToSignalStrength(rssi));
    }

    @ParameterizedTest
    @MethodSource("alarmModeConfigurations")
    void testAlarmModeMapping(List<@Nullable SecurityZone> securityZones, StringType expectedAlarmMode) {
        assertEquals(expectedAlarmMode, HomematicIPUtils.getAlarmMode(securityZones));
    }
}
