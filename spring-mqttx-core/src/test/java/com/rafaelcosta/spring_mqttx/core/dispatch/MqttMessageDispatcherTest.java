package com.rafaelcosta.spring_mqttx.core.dispatch;

import com.rafaelcosta.spring_mqttx.core.exception.MqttDispatchException;
import com.rafaelcosta.spring_mqttx.core.handler.MqttMessageHandler;
import com.rafaelcosta.spring_mqttx.core.model.MqttInboundMessage;
import com.rafaelcosta.spring_mqttx.core.model.MqttSubscriptionDefinition;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MqttMessageDispatcherTest {

    @Test
    void shouldDispatchMessageToRegisteredHandler() {
        MqttHandlerRegistry registry = new MqttHandlerRegistry();
        AtomicBoolean handled = new AtomicBoolean(false);

        registry.register(new MqttSubscriptionDefinition("test/topic", 1), message -> handled.set(true));

        MqttMessageDispatcher dispatcher = new MqttMessageDispatcher(registry);
        dispatcher.dispatch(new MqttInboundMessage("test/topic", new MqttMessage("payload".getBytes())));

        assertTrue(handled.get());
    }

    @Test
    void shouldDispatchMessageToPlaceholderSubscriberAndProvideVariables() {
        MqttHandlerRegistry registry = new MqttHandlerRegistry();
        AtomicBoolean handled = new AtomicBoolean(false);
        AtomicReference<Map<String, String>> variables = new AtomicReference<>();

        registry.register(new MqttSubscriptionDefinition("devices/{deviceId}/state", 1), message -> {
            handled.set(true);
            variables.set(message.topicParameters());
        });

        MqttMessageDispatcher dispatcher = new MqttMessageDispatcher(registry);
        dispatcher.dispatch(new MqttInboundMessage("devices/iot01/state", new MqttMessage("payload".getBytes())));

        assertTrue(handled.get());
        assertEquals("iot01", variables.get().get("deviceId"));
    }

    @Test
    void shouldThrowDispatchExceptionWhenHandlerFails() {
        MqttHandlerRegistry registry = new MqttHandlerRegistry();
        MqttMessageHandler failingHandler = message -> {
            throw new IllegalStateException("Falha proposital");
        };

        registry.register(new MqttSubscriptionDefinition("test/topic", 1), failingHandler);
        MqttMessageDispatcher dispatcher = new MqttMessageDispatcher(registry);

        assertThrows(MqttDispatchException.class,
                () -> dispatcher.dispatch(new MqttInboundMessage("test/topic", new MqttMessage("payload".getBytes()))));
    }
}
