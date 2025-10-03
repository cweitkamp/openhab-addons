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
package org.openhab.binding.homematicip.internal.dto.params;

import com.google.gson.annotations.SerializedName;

/**
 * Generated Plain Old Java Objects class for {@link ZonesActivationParams} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class ZonesActivationParams {
    @SerializedName("EXTERNAL")
    public boolean external = false;
    @SerializedName("INTERNAL")
    public boolean internal = false;
}
