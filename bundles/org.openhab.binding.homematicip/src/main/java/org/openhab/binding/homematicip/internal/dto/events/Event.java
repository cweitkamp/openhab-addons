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
package org.openhab.binding.homematicip.internal.dto.events;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Generated Plain Old Java Objects class for {@link Event} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class Event {

    public static final String EVENT_TYPE_DEVICE_CHANGED = "DEVICE_CHANGED";
    public static final String EVENT_TYPE_GROUP_CHANGED = "GROUP_CHANGED";
    public static final String EVENT_TYPE_HOME_CHANGED = "HOME_CHANGED";
    public static final String EVENT_TYPE_SECURITY_JOURNAL_CHANGED = "SECURITY_JOURNAL_CHANGED";

    @SerializedName("pushEventType")
    public String eventType;
    public @Nullable JsonObject device;
    public @Nullable JsonObject group;
    public @Nullable JsonObject home;
}
