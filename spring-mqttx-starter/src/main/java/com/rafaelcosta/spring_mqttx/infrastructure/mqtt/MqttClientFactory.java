package com.rafaelcosta.spring_mqttx.infrastructure.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;

public interface MqttClientFactory {
    IMqttAsyncClient createClient();
}
