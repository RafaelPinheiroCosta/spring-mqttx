package com.rafaelcosta.spring_mqttx.core.model;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public record MqttInboundMessage(String topic, MqttMessage message) {
}
