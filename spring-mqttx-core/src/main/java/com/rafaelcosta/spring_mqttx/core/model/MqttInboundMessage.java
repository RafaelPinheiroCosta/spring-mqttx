package com.rafaelcosta.spring_mqttx.core.model;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Collections;
import java.util.Map;

public record MqttInboundMessage(String topic, MqttMessage message, Map<String, String> topicParameters) {

    public MqttInboundMessage(String topic, MqttMessage message) {
        this(topic, message, Map.of());
    }

    public MqttInboundMessage {
        topicParameters = topicParameters == null ? Map.of() : Collections.unmodifiableMap(topicParameters);
    }
}
