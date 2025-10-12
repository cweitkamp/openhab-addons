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
package org.openhab.binding.homematicip.internal.dto.events;

/**
 * Generated Plain Old Java Objects class for {@link Origin} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class Origin {

    public static final String ORIGIN_TYPE_CLIENT = "CLIENT";
    public static final String ORIGIN_TYPE_DEVICE = "DEVICE";
    public static final String ORIGIN_TYPE_INTERNAL = "INTERNAL";
    public static final String ORIGIN_TYPE_RULE = "RULE";

    public String originType;
    public String id;
}
