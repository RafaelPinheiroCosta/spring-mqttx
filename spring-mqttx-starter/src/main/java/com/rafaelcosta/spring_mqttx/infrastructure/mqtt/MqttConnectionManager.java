package com.rafaelcosta.spring_mqttx.infrastructure.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(MqttConnectionManager.class);

    private final IMqttAsyncClient client;

    public MqttConnectionManager(IMqttAsyncClient client) {
        this.client = client;
    }

    public IMqttAsyncClient getClient() {
        return client;
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void disconnectQuietly() {
        try {
            if (client.isConnected()) {
                log.info("Desconectando cliente MQTT de forma silenciosa: clientId='{}'.", client.getClientId());
                client.disconnect().waitForCompletion();
                log.info("Cliente MQTT desconectado de forma silenciosa: clientId='{}'.", client.getClientId());
            }
        } catch (Exception e) {
            log.warn("Erro ao desconectar cliente MQTT: {}", e.getMessage(), e);
        }
    }
}
