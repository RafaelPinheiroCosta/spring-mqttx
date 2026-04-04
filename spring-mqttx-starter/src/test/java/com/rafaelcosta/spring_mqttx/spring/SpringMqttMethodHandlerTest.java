package com.rafaelcosta.spring_mqttx.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafaelcosta.spring_mqttx.core.logging.MqttLogSettings;
import com.rafaelcosta.spring_mqttx.core.model.MqttInboundMessage;
import com.rafaelcosta.spring_mqttx.core.serialization.JacksonPayloadSerializer;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPayload;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringMqttMethodHandlerTest {

    @Test
    void shouldResolvePayloadStringAndBytesArguments() throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean("testSubscriber", TestSubscriber.class);
        context.refresh();

        Method method = TestSubscriber.class.getDeclaredMethod("handle", SensorStatus.class, String.class, byte[].class);
        SpringMqttMethodHandler handler = new SpringMqttMethodHandler(
                context,
                "testSubscriber",
                method,
                new JacksonPayloadSerializer(new ObjectMapper()),
                MqttLogSettings.DISABLED
        );

        byte[] payload = new ObjectMapper().writeValueAsBytes(new SensorStatus("ok", 20.0));
        handler.handle(new MqttInboundMessage("sensores/status", new MqttMessage(payload)));

        TestSubscriber bean = context.getBean(TestSubscriber.class);
        assertEquals("ok", bean.payload.status());
        assertEquals(20.0, bean.payload.temperature());
        assertEquals(new String(payload, StandardCharsets.UTF_8), bean.rawText);
        assertArrayEquals(payload, bean.rawBytes);

        context.close();
    }

    static class TestSubscriber {
        private SensorStatus payload;
        private String rawText;
        private byte[] rawBytes;

        public void handle(@MqttPayload SensorStatus payload, String rawText, byte[] rawBytes) {
            this.payload = payload;
            this.rawText = rawText;
            this.rawBytes = rawBytes;
        }
    }

    record SensorStatus(String status, double temperature) {
    }
}