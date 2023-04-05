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

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * See {@link DeviceListModel}.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Refactoring of temperature conversion from celsius to FRITZ!Box values
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "celsius", "offset" })
@XmlRootElement(name = "temperature")
public class TemperatureModel {
    public static final BigDecimal TEMP_FACTOR = new BigDecimal("0.1");

    public BigDecimal celsius;
    public BigDecimal offset;

    public BigDecimal getCelsius() {
        return celsius != null ? TEMP_FACTOR.multiply(celsius) : BigDecimal.ZERO;
    }

    public BigDecimal getOffset() {
        return offset != null ? TEMP_FACTOR.multiply(offset) : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[celsius=").append(getCelsius()).append(",offset=").append(getOffset())
                .append("]").toString();
    }
}
