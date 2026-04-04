package com.rafaelcosta.spring_mqttx.core.client;

import org.eclipse.paho.client.mqttv3.MqttCallback;

public interface MqttClientOperations {
    boolean isConnected();
    void publish(String topic, byte[] payload, int qos);
    void subscribe(String topic, int qos);
    void setCallback(MqttCallback callback);
    void connect();
    void disconnect();
    void reconnect();
}
