package com.rafaelcosta.spring_mqttx.core.logging;

import java.nio.charset.StandardCharsets;

public final class MqttPayloadLogFormatter {

    private static final int DEFAULT_LIMIT = 300;

    private MqttPayloadLogFormatter() {
    }

    public static String describeBytes(byte[] payload) {
        if (payload == null) {
            return "null";
        }
        return payload.length + " bytes";
    }

    public static String preview(byte[] payload) {
        if (payload == null) {
            return "null";
        }
        String value = new String(payload, StandardCharsets.UTF_8).replaceAll("\\s+", " ").trim();
        if (value.length() <= DEFAULT_LIMIT) {
            return value;
        }
        return value.substring(0, DEFAULT_LIMIT) + "...";
    }

    public static String previewObject(Object payload) {
        if (payload == null) {
            return "null";
        }
        String value = String.valueOf(payload).replaceAll("\\s+", " ").trim();
        if (value.length() <= DEFAULT_LIMIT) {
            return value;
        }
        return value.substring(0, DEFAULT_LIMIT) + "...";
    }
}
