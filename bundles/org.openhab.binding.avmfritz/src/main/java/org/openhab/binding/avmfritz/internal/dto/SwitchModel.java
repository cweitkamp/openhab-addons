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
package org.openhab.binding.avmfritz.internal.dto;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * See {@link DeviceListModel}.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added new channels `locked`, `mode` and `radiator_mode`
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "switch")
public class SwitchModel {
    public static final BigDecimal ON = BigDecimal.ONE;
    public static final BigDecimal OFF = BigDecimal.ZERO;
    public static final String MODE_FRITZ_AUTO = "auto";
    public static final String MODE_FRITZ_MANUAL = "manuell";

    public BigDecimal state;
    public String mode;
    public BigDecimal lock;
    @XmlElement(name = "devicelock")
    public BigDecimal deviceLock;

    public String getMode() {
        return MODE_FRITZ_AUTO.equals(mode) ? MODE_AUTO : MODE_MANUAL;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[state=").append(state).append(",mode=").append(getMode()).append(",lock=")
                .append(lock).append(",devicelock=").append(deviceLock).append("]").toString();
    }
}
