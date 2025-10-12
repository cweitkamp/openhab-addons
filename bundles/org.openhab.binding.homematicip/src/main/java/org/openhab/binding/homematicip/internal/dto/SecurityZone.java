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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonObject;

/**
 * Generated Plain Old Java Objects class for {@link SecurityZone} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class SecurityZone {
    public String id;
    public String homeId;
    public @Nullable String metaGroupId;
    public String label;
    public long lastStatusUpdate;
    public @Nullable Boolean unreach;
    public @Nullable Boolean lowBat;
    public @Nullable Boolean dutyCycle;
    public String type;
    public List<JsonObject> channels;
    public boolean active;
    public boolean silent;
    public List<JsonObject> ignorableDeviceChannels;
    public @Nullable String windowState;
    public @Nullable Boolean motionDetected;
    public @Nullable Boolean presenceDetected;
    public @Nullable Boolean sabotage;
    public String zoneAssignmentIndex;
    public @Nullable Boolean configPending;
}
