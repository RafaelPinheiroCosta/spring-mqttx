package com.rafaelcosta.spring_mqttx.core.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafaelcosta.spring_mqttx.core.exception.MqttSerializationException;

import java.nio.charset.StandardCharsets;

public class Jackson2PayloadSerializer implements PayloadSerializer {

    private final ObjectMapper objectMapper;

    public Jackson2PayloadSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] write(Object value) {
        try {
            if (value instanceof byte[] bytes) {
                return bytes;
            }
            if (value instanceof String text) {
                return text.getBytes(StandardCharsets.UTF_8);
            }
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new MqttSerializationException("Erro ao serializar payload MQTT com Jackson 2.", e);
        }
    }

    @Override
    public <T> T read(byte[] payload, Class<T> type) {
        try {
            if (type.equals(String.class)) {
                return type.cast(asString(payload));
            }
            return objectMapper.readValue(payload, type);
        } catch (Exception e) {
            throw new MqttSerializationException("Erro ao desserializar payload MQTT para " + type.getName() + " com Jackson 2.", e);
        }
    }

    @Override
    public String asString(byte[] payload) {
        return new String(payload, StandardCharsets.UTF_8);
    }
}
