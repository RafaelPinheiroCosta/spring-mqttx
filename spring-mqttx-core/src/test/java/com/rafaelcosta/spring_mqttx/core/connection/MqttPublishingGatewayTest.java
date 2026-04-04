package com.rafaelcosta.spring_mqttx.core.connection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafaelcosta.spring_mqttx.core.client.MqttClientOperations;
import com.rafaelcosta.spring_mqttx.core.model.MqttPublishRequest;
import com.rafaelcosta.spring_mqttx.core.serialization.JacksonPayloadSerializer;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MqttPublishingGatewayTest {

    @Test
    void shouldPublishWhenClientIsConnected() {
        RecordingClientOperations client = new RecordingClientOperations(true);
        MqttPublishingGateway gateway = new MqttPublishingGateway(client, new JacksonPayloadSerializer(new ObjectMapper()));

        gateway.publish(new MqttPublishRequest("sensores/status", 1, "ok"));

        assertEquals("sensores/status", client.lastTopic.get());
        assertEquals("ok", new String(client.lastPayload.get(), StandardCharsets.UTF_8));
        assertEquals(1, client.lastQos.get());
    }

    @Test
    void shouldSkipPublishWhenClientIsDisconnected() {
        RecordingClientOperations client = new RecordingClientOperations(false);
        MqttPublishingGateway gateway = new MqttPublishingGateway(client, new JacksonPayloadSerializer(new ObjectMapper()));

        gateway.publish(new MqttPublishRequest("sensores/status", 1, "ok"));

        assertNull(client.lastTopic.get());
        assertNull(client.lastPayload.get());
        assertNull(client.lastQos.get());
    }

    private static final class RecordingClientOperations implements MqttClientOperations {
        private final boolean connected;
        private final AtomicReference<String> lastTopic = new AtomicReference<>();
        private final AtomicReference<byte[]> lastPayload = new AtomicReference<>();
        private final AtomicReference<Integer> lastQos = new AtomicReference<>();

        private RecordingClientOperations(boolean connected) {
            this.connected = connected;
        }

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public void publish(String topic, byte[] payload, int qos) {
            lastTopic.set(topic);
            lastPayload.set(payload);
            lastQos.set(qos);
        }

        @Override
        public void subscribe(String topic, int qos) {
        }

        @Override
        public void setCallback(MqttCallback callback) {
        }

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
        }

        @Override
        public void reconnect() {
        }
    }
}
