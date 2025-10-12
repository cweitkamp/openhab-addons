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
package org.openhab.binding.homematicip.internal.dto;

/**
 * Generated Plain Old Java Objects class for {@link HeatingThermostat} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class HeatingThermostat extends AbstractHeatingThermostat {

    public static final String VALE_STATE_WAIT_FOR_ADAPTION = "WAIT_FOR_ADAPTION";
    public static final String VALE_STATE_ADAPTION_DONE = "ADAPTION_DONE";
    public static final String VALE_STATE_ADJUSTMENT_TOO_SMALL = "ADJUSTMENT_TOO_SMALL";
    public static final String VALE_STATE_ADJUSTMENT_TOO_BIG = "ADJUSTMENT_TOO_BIG";

    public String channelRole;
    public double valvePosition;
    public String valveState;
    public double valveActualTemperature;
}
