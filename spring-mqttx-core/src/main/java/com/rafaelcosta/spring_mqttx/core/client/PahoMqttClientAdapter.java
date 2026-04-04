package com.rafaelcosta.spring_mqttx.core.client;

import com.rafaelcosta.spring_mqttx.core.exception.MqttPublishException;
import com.rafaelcosta.spring_mqttx.core.logging.MqttLogSettings;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PahoMqttClientAdapter implements MqttClientOperations {

    private static final Logger log = LoggerFactory.getLogger(PahoMqttClientAdapter.class);

    private final IMqttAsyncClient client;
    private final MqttLogSettings logSettings;

    public PahoMqttClientAdapter(IMqttAsyncClient client) {
        this(client, MqttLogSettings.DISABLED);
    }

    public PahoMqttClientAdapter(IMqttAsyncClient client, MqttLogSettings logSettings) {
        this.client = client;
        this.logSettings = logSettings;
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public void publish(String topic, byte[] payload, int qos) {
        try {
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);
            if (logSettings.isPublishEnabled() && log.isDebugEnabled()) {
                log.debug("Publicando via cliente MQTT: topico='{}', qos={}, bytes={}", topic, qos,
                        payload == null ? 0 : payload.length);
            }
            client.publish(topic, message).waitForCompletion();
        } catch (Exception e) {
            log.error("Falha ao publicar mensagem MQTT: topico='{}', qos={}. Motivo: {}",
                    topic, qos, e.getMessage(), e);
            throw new MqttPublishException("Erro ao publicar no tópico " + topic, e);
        }
    }

    @Override
    public void subscribe(String topic, int qos) {
        try {
            if (logSettings.isSubscriptionEnabled() && log.isDebugEnabled()) {
                log.debug("Solicitando inscricao MQTT: topico='{}', qos={}", topic, qos);
            }
            client.subscribe(topic, qos).waitForCompletion();
        } catch (Exception e) {
            log.error("Falha ao se inscrever no topico MQTT: topico='{}', qos={}. Motivo: {}",
                    topic, qos, e.getMessage(), e);
            throw new MqttPublishException("Erro ao se inscrever no tópico " + topic, e);
        }
    }

    @Override
    public void setCallback(MqttCallback callback) {
        try {
            client.setCallback(callback);
            if (logSettings.isSubscriptionEnabled() && log.isDebugEnabled()) {
                log.debug("Callback MQTT configurado com sucesso.");
            }
        } catch (Exception e) {
            log.error("Falha ao configurar callback MQTT: {}", e.getMessage(), e);
            throw new MqttPublishException("Erro ao configurar callback MQTT.", e);
        }
    }

    @Override
    public void connect() {
        try {
            if (!client.isConnected()) {
                log.info("Solicitando conexao/reconexao MQTT para clientId='{}'.", client.getClientId());
                client.reconnect();
            }
        } catch (Exception e) {
            log.error("Falha ao conectar/reconectar cliente MQTT: {}", e.getMessage(), e);
            throw new MqttPublishException("Erro ao conectar cliente MQTT.", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (client.isConnected()) {
                log.info("Solicitando desconexao MQTT para clientId='{}'.", client.getClientId());
                client.disconnect().waitForCompletion();
                log.info("Cliente MQTT desconectado com sucesso: clientId='{}'.", client.getClientId());
            }
        } catch (Exception e) {
            log.error("Falha ao desconectar cliente MQTT: {}", e.getMessage(), e);
            throw new MqttPublishException("Erro ao desconectar cliente MQTT.", e);
        }
    }

    @Override
    public void reconnect() {
        log.warn("Tentando reconectar cliente MQTT: clientId='{}'.", safeClientId());
        connect();
        log.info("Rotina de reconexao MQTT concluida: clientId='{}', conectado={}", safeClientId(), isConnected());
    }

    private String safeClientId() {
        try {
            return client.getClientId();
        } catch (Exception e) {
            return "desconhecido";
        }
    }
}
