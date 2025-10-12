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
package org.openhab.binding.homematicip.internal.connection;

import static org.openhab.binding.homematicip.internal.HomematicIPBindingConstants.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.homematicip.internal.config.HomematicIPAccessPointConfiguration;
import org.openhab.binding.homematicip.internal.dto.CurrentState;
import org.openhab.binding.homematicip.internal.dto.Host;
import org.openhab.binding.homematicip.internal.dto.params.GetCurrentStateParams;
import org.openhab.binding.homematicip.internal.dto.params.SetActiveProfileParams;
import org.openhab.binding.homematicip.internal.dto.params.SetClimateControlDisplayParams;
import org.openhab.binding.homematicip.internal.dto.params.SetControlModeParams;
import org.openhab.binding.homematicip.internal.dto.params.SetSetPointTemperatureParams;
import org.openhab.binding.homematicip.internal.dto.params.SetSetZonesActivationParams;
import org.openhab.binding.homematicip.internal.dto.params.SetSwitchStateParams;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link HomematicIPHTTPConnection} is responsible for handling the connections to the homematic IP REST API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPHTTPConnection {

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String VERSION = "12";
    private static final int TIMEOUT = 10;

    private static final String JSON_VALUE_ERROR_CODE = "errorCode";

    private static final URI LOOKUP_HOST_URL = URI.create("https://lookup.homematic.com:48335/getHost");
    static final String GET_CURRENT_STATE_PATH = "/hmip/home/getCurrentState";
    static final String SET_SET_ZONES_ACTIVATION_PATH = "/hmip/home/security/setZonesActivation";
    static final String SET_SET_POINT_TEMPERATURE_PATH = "/hmip/group/heating/setSetPointTemperature";
    static final String SET_CONTROL_MODE_PATH = "/hmip/group/heating/setControlMode";
    static final String SET_ACTIVE_PROFILE_PATH = "/hmip/group/heating/setActiveProfile";
    static final String SET_CLIMATE_CONTROL_DISPLAY_PATH = "/hmip/device/configuration/setClimateControlDisplay";
    static final String SET_SWITCH_STATE_PATH = "/hmip/device/control/setSwitchState";

    private final Logger logger = LoggerFactory.getLogger(HomematicIPHTTPConnection.class);

    private final HttpClient httpClient;
    private final Gson gson;
    private final HomematicIPAccessPointConfiguration config;

    private final ExpiringCacheMap<String, String> cache;

    URI getCurrentStateUrl = URI.create(Host.REST_BASE_URL + GET_CURRENT_STATE_PATH);
    URI setSetZonesActivationUrl = URI.create(Host.REST_BASE_URL + SET_SET_ZONES_ACTIVATION_PATH);
    URI setSetPointTemperatureUrl = URI.create(Host.REST_BASE_URL + SET_SET_POINT_TEMPERATURE_PATH);
    URI setControlModeUrl = URI.create(Host.REST_BASE_URL + SET_CONTROL_MODE_PATH);
    URI setActiveProfileUrl = URI.create(Host.REST_BASE_URL + SET_ACTIVE_PROFILE_PATH);
    URI setClimateControlDisplayUrl = URI.create(Host.REST_BASE_URL + SET_CLIMATE_CONTROL_DISPLAY_PATH);
    URI setSwitchStateUrl = URI.create(Host.REST_BASE_URL + SET_SWITCH_STATE_PATH);

    public HomematicIPHTTPConnection(HttpClient httpClient, Gson gson, HomematicIPAccessPointConfiguration config) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.config = config;

        this.cache = new ExpiringCacheMap<>(
                TimeUnit.MINUTES.toMillis(config.refreshInterval) - TimeUnit.SECONDS.toMillis(1));
    }

    public void setBaseUrl(String host) {
        getCurrentStateUrl = URI.create(host + GET_CURRENT_STATE_PATH);
        setSetZonesActivationUrl = URI.create(host + SET_SET_ZONES_ACTIVATION_PATH);
        setSetPointTemperatureUrl = URI.create(host + SET_SET_POINT_TEMPERATURE_PATH);
        setControlModeUrl = URI.create(host + SET_CONTROL_MODE_PATH);
        setActiveProfileUrl = URI.create(host + SET_ACTIVE_PROFILE_PATH);
        setClimateControlDisplayUrl = URI.create(host + SET_CLIMATE_CONTROL_DISPLAY_PATH);
        setSwitchStateUrl = URI.create(host + SET_SWITCH_STATE_PATH);
    }

    public @Nullable Host lookupHost() {
        try {
            GetCurrentStateParams getCurrentStateParams = new GetCurrentStateParams();
            getCurrentStateParams.id = config.SGTIN;
            return gson.fromJson(post(LOOKUP_HOST_URL, new StringContentProvider(gson.toJson(getCurrentStateParams))),
                    Host.class);
        } catch (JsonSyntaxException | CommunicationException | ConfigurationException e) {
            return null;
        }
    }

    public @Nullable CurrentState getCurrentState()
            throws JsonSyntaxException, CommunicationException, ConfigurationException {
        GetCurrentStateParams getCurrentStateParams = new GetCurrentStateParams();
        getCurrentStateParams.id = config.SGTIN;
        return gson.fromJson(
                postFromCache(getCurrentStateUrl, new StringContentProvider(gson.toJson(getCurrentStateParams))),
                CurrentState.class);
    }

    public void setSetZonesActivation(boolean external, boolean internal)
            throws CommunicationException, ConfigurationException {
        SetSetZonesActivationParams setSetZonesActivationParams = new SetSetZonesActivationParams();
        setSetZonesActivationParams.zonesActivation.external = external;
        setSetZonesActivationParams.zonesActivation.internal = internal;
        post(setSetZonesActivationUrl, new StringContentProvider(gson.toJson(setSetZonesActivationParams)));
    }

    public void setSetPointTemperature(String groupId, double temperature)
            throws CommunicationException, ConfigurationException {
        SetSetPointTemperatureParams setPointTemperatureParams = new SetSetPointTemperatureParams();
        setPointTemperatureParams.groupId = groupId;
        setPointTemperatureParams.setPointTemperature = temperature;
        post(setSetPointTemperatureUrl, new StringContentProvider(gson.toJson(setPointTemperatureParams)));
    }

    public void setControlMode(String groupId, String mode) throws CommunicationException, ConfigurationException {
        SetControlModeParams setControlModeParams = new SetControlModeParams();
        setControlModeParams.groupId = groupId;
        setControlModeParams.controlMode = mode;
        post(setControlModeUrl, new StringContentProvider(gson.toJson(setControlModeParams)));
    }

    public void setActiveProfile(String groupId, String profileIndex)
            throws CommunicationException, ConfigurationException {
        SetActiveProfileParams setActiveProfileParams = new SetActiveProfileParams();
        setActiveProfileParams.groupId = groupId;
        setActiveProfileParams.profileIndex = profileIndex;
        post(setActiveProfileUrl, new StringContentProvider(gson.toJson(setActiveProfileParams)));
    }

    public void setClimateControlDisplayMode(String deviceId, String displayMode)
            throws CommunicationException, ConfigurationException {
        SetClimateControlDisplayParams setClimateControlDisplayParams = new SetClimateControlDisplayParams();
        setClimateControlDisplayParams.deviceId = deviceId;
        setClimateControlDisplayParams.display = displayMode;
        post(setClimateControlDisplayUrl, new StringContentProvider(gson.toJson(setClimateControlDisplayParams)));
    }

    public void setSwitchState(String deviceId, boolean on) throws CommunicationException, ConfigurationException {
        SetSwitchStateParams setSwitchStateParams = new SetSwitchStateParams();
        setSwitchStateParams.deviceId = deviceId;
        setSwitchStateParams.on = on;
        post(setSwitchStateUrl, new StringContentProvider(gson.toJson(setSwitchStateParams)));
    }

    @SuppressWarnings("unused")
    private String buildURL(String url, Map<String, String> requestParams) {
        return requestParams.keySet().stream().map(key -> key + "=" + encodeParam(requestParams.get(key)))
                .collect(Collectors.joining("&", url + "?", ""));
    }

    private String encodeParam(@Nullable String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private @Nullable String postFromCache(URI uri, ContentProvider body) {
        logger.trace("homematic IP request from cache: POST - URL = '{}'", uri);
        return cache.putIfAbsentAndGet(config.SGTIN + "#" + uri.toString(), () -> post(uri, body));
    }

    private String post(URI uri, ContentProvider body) throws CommunicationException, ConfigurationException {
        return executeRequest(HttpMethod.POST, uri, body);
    }

    private synchronized String executeRequest(HttpMethod httpMethod, URI uri, @Nullable ContentProvider body)
            throws CommunicationException, ConfigurationException {
        logger.trace("homematic IP HTTP request: {} - URL = '{}'", httpMethod, uri);
        try {
            final Request request = httpClient.newRequest(uri) //
                    .header(HttpHeader.ACCEPT, CONTENT_TYPE_APPLICATION_JSON) //
                    .header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON) //
                    .header(HTTP_HEADER_VERSION, VERSION) //
                    .header(HTTP_HEADER_AUTHTOKEN, config.authcode) //
                    .header(HTTP_HEADER_CLIENTAUTH, config.clientauth) //
                    .method(httpMethod) //
                    .timeout(TIMEOUT, TimeUnit.SECONDS);

            if (body != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("homematic IP HTTP request body: '{}'", body);
                }
                request.content(body);
            }

            final ContentResponse contentResponse = request.send();

            final int httpStatus = contentResponse.getStatus();
            final String content = contentResponse.getContentAsString();
            logger.trace("homematic IP HTTP response: status = {}, content = '{}'", httpStatus, content);
            switch (httpStatus) {
                case HttpStatus.OK_200:
                    return content;
                case HttpStatus.BAD_REQUEST_400:
                    logger.debug("homematic IP server responded with status code {}: {}", httpStatus, content);
                    throw new ConfigurationException(getErrorCode(content));
                case HttpStatus.TOO_MANY_REQUESTS_429:
                    // TODO disable refresh job temporarily
                default:
                    logger.debug("homematic IP server responded with status code {}: {}", httpStatus, content);
                    throw new CommunicationException(content);
            }
        } catch (ExecutionException e) {
            String message = e.getMessage();
            logger.debug("ExecutionException occurred during execution: {}", message, e);
            throw new CommunicationException(message == null ? TEXT_OFFLINE_COMMUNICATION_ERROR : message,
                    e.getCause());
        } catch (TimeoutException e) {
            String message = e.getMessage();
            logger.debug("TimeoutException occurred during execution: {}", message, e);
            throw new CommunicationException(message == null ? TEXT_OFFLINE_COMMUNICATION_ERROR : message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String message = e.getMessage();
            logger.debug("InterruptedException occurred during execution: {}", message, e);
            throw new CommunicationException(message == null ? TEXT_OFFLINE_COMMUNICATION_ERROR : message);
        }
    }

    private String getErrorCode(String content) {
        final JsonObject json = JsonParser.parseString(content).getAsJsonObject();
        final JsonElement errorCode = json.get(JSON_VALUE_ERROR_CODE);
        if (errorCode != null) {
            return errorCode.getAsString();
        }
        return TEXT_OFFLINE_CONF_ERROR_UNKNOWN;
    }
}
