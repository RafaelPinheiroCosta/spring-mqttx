package com.rafaelcosta.spring_mqttx.core.model;

public record MqttPublishRequest(String topic, int qos, Object payload) {
}
