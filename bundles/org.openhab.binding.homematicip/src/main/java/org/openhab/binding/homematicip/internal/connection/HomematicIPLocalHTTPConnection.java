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
import org.openhab.binding.homematicip.internal.config.HomematicIPHomeControlUnitConfiguration;
import org.openhab.binding.homematicip.internal.dto.ConfirmAuthTokenResponse;
import org.openhab.binding.homematicip.internal.dto.RequestAuthTokenResponse;
import org.openhab.binding.homematicip.internal.dto.params.ConfirmAuthTokenParams;
import org.openhab.binding.homematicip.internal.dto.params.RequestAuthTokenParams;
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
 * The {@link HomematicIPLocalHTTPConnection} is responsible for handling the connections to the homematic IP REST API.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class HomematicIPLocalHTTPConnection {

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String VERSION = "12";
    private static final int TIMEOUT = 10;

    private static final String JSON_VALUE_ERROR_CODE = "errorCode";

    static final String REQUEST_AUTH_TOKEN_PATH = "/hmip/auth/requestConnectApiAuthToken";
    static final String CONFIRM_AUTH_TOKEN_PATH = "/hmip/auth/confirmConnectApiAuthToken";

    private final Logger logger = LoggerFactory.getLogger(HomematicIPLocalHTTPConnection.class);

    private final HttpClient httpClient;
    private final Gson gson;
    private final HomematicIPHomeControlUnitConfiguration config;

    final URI requestAuthTokenUri;
    final URI confirmAuthTokenUri;

    public HomematicIPLocalHTTPConnection(HttpClient httpClient, Gson gson,
            HomematicIPHomeControlUnitConfiguration config) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.config = config;

        requestAuthTokenUri = URI.create(config.getURL() + REQUEST_AUTH_TOKEN_PATH);
        confirmAuthTokenUri = URI.create(config.getURL() + CONFIRM_AUTH_TOKEN_PATH);
    }

    public @Nullable RequestAuthTokenResponse requestAuthToken() throws CommunicationException, ConfigurationException {
        try {
            RequestAuthTokenParams requestAuthTokenParams = new RequestAuthTokenParams();
            requestAuthTokenParams.activationKey = config.activationKey;
            return gson.fromJson(
                    post(requestAuthTokenUri, new StringContentProvider(gson.toJson(requestAuthTokenParams))),
                    RequestAuthTokenResponse.class);
        } catch (JsonSyntaxException | CommunicationException | ConfigurationException e) {
            return null;
        }
    }

    public @Nullable ConfirmAuthTokenResponse confirmAuthToken(String authtoken)
            throws CommunicationException, ConfigurationException {
        try {
            ConfirmAuthTokenParams confirmAuthTokenParams = new ConfirmAuthTokenParams();
            confirmAuthTokenParams.activationKey = config.activationKey;
            confirmAuthTokenParams.authToken = authtoken;
            return gson.fromJson(
                    post(confirmAuthTokenUri, new StringContentProvider(gson.toJson(confirmAuthTokenParams))),
                    ConfirmAuthTokenResponse.class);
        } catch (JsonSyntaxException | CommunicationException | ConfigurationException e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    private String buildURL(String url, Map<String, String> requestParams) {
        return requestParams.keySet().stream().map(key -> key + "=" + encodeParam(requestParams.get(key)))
                .collect(Collectors.joining("&", url + "?", ""));
    }

    private String encodeParam(@Nullable String value) {
        return value == null ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
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
                    throw new CommunicationException(getErrorCode(content));
                case HttpStatus.FORBIDDEN_403:
                    logger.debug("homematic IP server responded with status code {}: {}", httpStatus, content);
                    throw new ConfigurationException(getErrorCode(content));
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
