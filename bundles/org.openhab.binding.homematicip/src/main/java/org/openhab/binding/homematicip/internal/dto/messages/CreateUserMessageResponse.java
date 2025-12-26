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
package org.openhab.binding.homematicip.internal.dto.messages;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Generated Plain Old Java Objects class for {@link CreateUserMessageResponse} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class CreateUserMessageResponse extends PluginMessage {

    private class Error {
        public String code;
        public @Nullable String message;
    }

    private class CreateUserMessageResponseBody {
        public boolean success;
        public String userMessageId;
        public @Nullable Error error;
    }

    private final CreateUserMessageResponseBody body;

    public CreateUserMessageResponse() {
        super(CREATE_USER_MESSAGE_RESPONSE_TYPE);
        this.body = new CreateUserMessageResponseBody();
    }
}
