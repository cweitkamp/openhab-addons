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

import org.openhab.core.types.StateOption;

/**
 * Generated Plain Old Java Objects class for {@link Profile} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class Profile {
    public String profileId;
    public String groupId;
    public String index;
    public String name;
    public boolean visible;
    public boolean enabled;

    public StateOption getAsStateOption() {
        return new StateOption(index, name);
    }
}
