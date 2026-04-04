package com.rafaelcosta.spring_mqttx.core.handler;

import com.rafaelcosta.spring_mqttx.core.model.MqttInboundMessage;

public interface MqttMessageHandler {
    void handle(MqttInboundMessage inboundMessage);
}
