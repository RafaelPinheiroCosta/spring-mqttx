package com.rafaelcosta.spring_mqttx.core.dispatch;

import com.rafaelcosta.spring_mqttx.core.exception.MqttDispatchException;
import com.rafaelcosta.spring_mqttx.core.model.MqttInboundMessage;
import com.rafaelcosta.spring_mqttx.core.model.MqttSubscriptionDefinition;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MqttMessageDispatcherTest {

    @Test
    void shouldDispatchMessageToRegisteredHandlers() {
        MqttHandlerRegistry registry = new MqttHandlerRegistry();
        AtomicInteger count = new AtomicInteger();
        registry.register(new MqttSubscriptionDefinition("sensores/status", 1), message -> count.incrementAndGet());
        registry.register(new MqttSubscriptionDefinition("sensores/status", 1), message -> count.incrementAndGet());

        MqttMessageDispatcher dispatcher = new MqttMessageDispatcher(registry);
        dispatcher.dispatch(new MqttInboundMessage("sensores/status", new MqttMessage("ok".getBytes(StandardCharsets.UTF_8))));

        assertEquals(2, count.get());
    }

    @Test
    void shouldWrapHandlerExceptions() {
        MqttHandlerRegistry registry = new MqttHandlerRegistry();
        registry.register(new MqttSubscriptionDefinition("sensores/status", 1), message -> {
            throw new IllegalStateException("falha");
        });

        MqttMessageDispatcher dispatcher = new MqttMessageDispatcher(registry);

        assertThrows(MqttDispatchException.class,
                () -> dispatcher.dispatch(new MqttInboundMessage("sensores/status", new MqttMessage())));
    }
}
