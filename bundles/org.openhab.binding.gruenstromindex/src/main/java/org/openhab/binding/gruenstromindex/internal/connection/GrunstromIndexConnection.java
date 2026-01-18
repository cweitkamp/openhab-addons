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
package org.openhab.binding.gruenstromindex.internal.connection;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.openhab.binding.gruenstromindex.internal.GruenstromIndexBindingConstants.TEXT_OFFLINE_CONF_ERROR_MISSING_ZIPCODE;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.gruenstromindex.internal.config.GruenstromIndexAccountConfiguration;
import org.openhab.binding.gruenstromindex.internal.dto.GrunstromIndexGreenEnergyForecastData;
import org.openhab.binding.gruenstromindex.internal.handler.GruenstromIndexAccountHandler;
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
 * The {@link GrunstromIndexConnection} is responsible for handling the connections to GruenstromIndex API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GrunstromIndexConnection {

    private final Logger logger = LoggerFactory.getLogger(GrunstromIndexConnection.class);

    private static final String PROPERTY_MESSAGE = "message";

    private static final String PARAM_ZIP = "zip";
    private static final String PARAM_TOKEN = "token";

    // Green Energy Forecast (see https://console.corrently.io/gsi.html)
    private static final String GREEN_ENERGY_FORECAST_URL = "https://api.corrently.io/v2.0/gsi/prediction";

    private final GruenstromIndexAccountHandler handler;
    private final HttpClient httpClient;

    private final ExpiringCacheMap<String, String> cache;

    private final Gson gson = new Gson();

    public GrunstromIndexConnection(GruenstromIndexAccountHandler handler, HttpClient httpClient) {
        this.handler = handler;
        this.httpClient = httpClient;

        GruenstromIndexAccountConfiguration config = handler.getConfiguration();
        cache = new ExpiringCacheMap<>(TimeUnit.MINUTES.toMillis(config.refreshInterval));
    }

    /**
     * Requests the green energy forecast for the given zipcode.
     *
     * @param zipcode Zipcode of a city / village in Germany.
     * @return the energy forecast data
     * @throws JsonSyntaxException
     * @throws CommunicationException
     * @throws ConfigurationException
     */
    public synchronized @Nullable GrunstromIndexGreenEnergyForecastData getGreenEnergyForcastData(
            @Nullable String zipcode) throws JsonSyntaxException, CommunicationException, ConfigurationException {
        return gson.fromJson(
                getResponseFromCache(
                        buildURL(GREEN_ENERGY_FORECAST_URL, getRequestParams(handler.getConfiguration(), zipcode))),
                GrunstromIndexGreenEnergyForecastData.class);
    }

    private Map<String, String> getRequestParams(GruenstromIndexAccountConfiguration config, @Nullable String zipcode) {
        if (zipcode == null) {
            throw new ConfigurationException(TEXT_OFFLINE_CONF_ERROR_MISSING_ZIPCODE);
        }

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_ZIP, zipcode);

        if (!config.token.isBlank()) {
            params.put(PARAM_TOKEN, config.token);
        }
        return params;
    }

    private String buildURL(String url, Map<String, String> requestParams) {
        return requestParams.keySet().stream().map(key -> key + "=" + encodeParam(requestParams.get(key)))
                .collect(Collectors.joining("&", url + "?", ""));
    }

    private String encodeParam(@Nullable String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private @Nullable String getResponseFromCache(String url) {
        return cache.putIfAbsentAndGet(url, () -> getResponse(url));
    }

    private String getResponse(String url) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("GruenstromIndex request: URL = '{}'", uglifyApikey(url));
            }
            ContentResponse contentResponse = httpClient.newRequest(url).method(GET).timeout(10, TimeUnit.SECONDS)
                    .send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            String errorMessage = "";
            logger.trace("GruenstromIndex response: status = {}, content = '{}'", httpStatus, content);
            switch (httpStatus) {
                case OK_200:
                    return content;
                case BAD_REQUEST_400:
                case UNAUTHORIZED_401:
                case NOT_FOUND_404:
                    errorMessage = getErrorMessage(content);
                    logger.debug("GruenstromIndex server responded with status code {}: {}", httpStatus, errorMessage);
                    throw new ConfigurationException(errorMessage);
                case TOO_MANY_REQUESTS_429:
                    errorMessage = getErrorMessage(content);
                    logger.debug("GruenstromIndex server responded with status code {}: {}", httpStatus, errorMessage);
                    throw new CommunicationException(errorMessage);
                default:
                    errorMessage = getErrorMessage(content);
                    logger.debug("GruenstromIndex server responded with status code {}: {}", httpStatus, errorMessage);
                    throw new CommunicationException(errorMessage);
            }
        } catch (ExecutionException e) {
            String errorMessage = e.getMessage();
            logger.debug("ExecutionException occurred during execution: {}", errorMessage, e);
            if (e.getCause() instanceof HttpResponseException) {
                logger.debug("GruenstromIndex server responded with status code {}: Invalid API key.",
                        UNAUTHORIZED_401);
                throw new ConfigurationException("@text/offline.conf-error-invalid-apikey", e.getCause());
            } else {
                throw new CommunicationException(
                        errorMessage == null ? "@text/offline.communication-error" : errorMessage, e.getCause());
            }
        } catch (TimeoutException e) {
            String errorMessage = e.getMessage();
            logger.debug("TimeoutException occurred during execution: {}", errorMessage, e);
            throw new CommunicationException(errorMessage == null ? "@text/offline.communication-error" : errorMessage,
                    e.getCause());
        } catch (InterruptedException e) {
            String errorMessage = e.getMessage();
            logger.debug("InterruptedException occurred during execution: {}", errorMessage, e);
            Thread.currentThread().interrupt();
            throw new CommunicationException(errorMessage == null ? "@text/offline.communication-error" : errorMessage,
                    e.getCause());
        }
    }

    private String uglifyApikey(String url) {
        return url.replaceAll("(token=)+\\w+", "token=*****");
    }

    private String getErrorMessage(String response) {
        JsonElement jsonResponse = JsonParser.parseString(response);
        if (jsonResponse.isJsonObject()) {
            JsonObject json = jsonResponse.getAsJsonObject();
            if (json.has(PROPERTY_MESSAGE)) {
                return json.get(PROPERTY_MESSAGE).getAsString();
            }
        }
        return response;
    }
}
