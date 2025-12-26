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

import static org.openhab.binding.homematicip.internal.HomematicIPBindingConstants.PLUGIN_TITLE;

import java.time.Instant;
import java.util.UUID;

/**
 * Generated Plain Old Java Objects class for {@link CreateUserMessageRequest} from JSON.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class CreateUserMessageRequest extends PluginMessage {

    public static final String MESSAGE_CATEGORY_ERROR = "ERROR";
    public static final String MESSAGE_CATEGORY_INFO = "INFO";
    public static final String MESSAGE_CATEGORY_WARN = "WARN";

    public static final String BEHAVIOR_TYPE_ACKNOWLEDGEABLE_BY_OK = "ACKNOWLEDGEABLE_BY_OK";
    public static final String BEHAVIOR_TYPE_ACKNOWLEDGEABLE_BY_YES_NO = "ACKNOWLEDGEABLE_BY_YES_NO";
    public static final String BEHAVIOR_TYPE_DISMISSIBLE = "DISMISSIBLE";
    public static final String BEHAVIOR_TYPE_NOT_DISMISSIBLE = "NOT_DISMISSIBLE";

    private class Title {
        public final String en = PLUGIN_TITLE;
        public final String de = PLUGIN_TITLE;
    }

    private class Message {
        public final String en;
        public final String de;

        public Message(String message) {
            en = message;
            de = message;
        }
    }

    private class CreateUserMessageRequestBody {
        public final String messageCategory = MESSAGE_CATEGORY_INFO;
        public final String userMessageId = UUID.randomUUID().toString();
        public final Title title = new Title();
        public final Message message;
        public final String behaviorType = BEHAVIOR_TYPE_DISMISSIBLE;
        public final long timestamp = Instant.now().toEpochMilli();

        public CreateUserMessageRequestBody(String message) {
            this.message = new Message(message);
        }
    }

    private final CreateUserMessageRequestBody body;

    public CreateUserMessageRequest(String message) {
        super(CREATE_USER_MESSAGE_REQUEST_TYPE);
        this.body = new CreateUserMessageRequestBody(message);
    }
}
