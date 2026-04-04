package com.rafaelcosta.spring_mqttx.core.model;

public record MqttSubscriptionDefinition(String topic, int qos) {
}
