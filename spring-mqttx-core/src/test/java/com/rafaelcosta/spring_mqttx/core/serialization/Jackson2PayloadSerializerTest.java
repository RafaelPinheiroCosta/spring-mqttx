package com.rafaelcosta.spring_mqttx.core.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Jackson2PayloadSerializerTest {

    private final Jackson2PayloadSerializer serializer = new Jackson2PayloadSerializer(new ObjectMapper());

    @Test
    void shouldWriteAndReadObjectPayload() {
        SensorStatus payload = new SensorStatus("ok", 21.5);

        byte[] bytes = serializer.write(payload);
        SensorStatus restored = serializer.read(bytes, SensorStatus.class);

        assertEquals(payload.status(), restored.status());
        assertEquals(payload.temperature(), restored.temperature());
    }

    @Test
    void shouldWriteStringAsUtf8Bytes() {
        byte[] bytes = serializer.write("hello");
        assertEquals("hello", serializer.asString(bytes));
    }

    @Test
    void shouldReturnRawByteArrayWithoutTransformingIt() {
        byte[] payload = new byte[]{1, 2, 3};
        assertArrayEquals(payload, serializer.write(payload));
    }

    record SensorStatus(String status, double temperature) {
    }
}
