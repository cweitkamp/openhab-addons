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
package org.openhab.binding.homematicip.internal.dto;

import java.util.List;

/**
 * Generated Plain Old Java Objects class for {@link AbstractHeatingThermostat} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class AbstractHeatingThermostat {
    public String label;
    public String deviceId;
    public int index;
    public int groupIndex;
    public String functionalChannelType;
    public List<String> groups = List.of();
    public double temperatureOffset;
    public double setPointTemperature;

    /**
     * Package protected default constructor to allow reflective instantiation.
     *
     * !!! DO NOT REMOVE - Gson needs it !!!
     */
    public AbstractHeatingThermostat() {
    }
}
