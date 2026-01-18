/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.gruenstromindex.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Location} is the Java class used to map the JSON response to a GruenstromIndex API request.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class Forecast {
    @SerializedName("epochtime")
    public int epochtime;
    @SerializedName("eevalue")
    public int eevalue;
    @SerializedName("ewind")
    public int ewind;
    @SerializedName("esolar")
    public int esolar;
    @SerializedName("ensolar")
    public int ensolar;
    @SerializedName("enwind")
    public int enwind;
    @SerializedName("sci")
    public int sci;
    @SerializedName("gsi")
    public double gsi;
    @SerializedName("timeStamp")
    public long timeStamp;
    @SerializedName("energyprice")
    public @NonNullByDefault({}) String energyprice;
    @SerializedName("co2_avg")
    public int co2Avg;
    @SerializedName("co2_g_standard")
    public int co2GStandard;
    @SerializedName("co2_g_oekostrom")
    public int co2GOekostrom;
    @SerializedName("timeframe")
    public @NonNullByDefault({}) Timeframe timeframe;
    @SerializedName("iat")
    public long iat;
    @SerializedName("zip")
    public @NonNullByDefault({}) String zip;
    @SerializedName("signature")
    public @NonNullByDefault({}) String signature;
}
