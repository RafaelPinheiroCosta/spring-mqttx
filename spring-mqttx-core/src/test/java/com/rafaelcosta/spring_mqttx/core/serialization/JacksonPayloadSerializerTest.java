package com.rafaelcosta.spring_mqttx.core.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafaelcosta.spring_mqttx.core.exception.MqttSerializationException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonPayloadSerializerTest {

    private final JacksonPayloadSerializer serializer = new JacksonPayloadSerializer(new ObjectMapper());

    @Test
    void shouldSerializeStringAsUtf8() {
        byte[] payload = serializer.write("olá mqtt");
        assertArrayEquals("olá mqtt".getBytes(StandardCharsets.UTF_8), payload);
    }

    @Test
    void shouldDeserializeRecordPayload() {
        SensorStatus source = new SensorStatus("ok", 21.5);
        byte[] payload = serializer.write(source);

        SensorStatus target = serializer.read(payload, SensorStatus.class);

        assertEquals(source.status(), target.status());
        assertEquals(source.temperature(), target.temperature());
    }

    @Test
    void shouldThrowExceptionWhenPayloadCannotBeRead() {
        assertThrows(MqttSerializationException.class,
                () -> serializer.read("{invalid-json".getBytes(StandardCharsets.UTF_8), SensorStatus.class));
    }

    private record SensorStatus(String status, double temperature) {
    }
}
