package com.rafaelcosta.spring_mqttx.core.connection;

import com.rafaelcosta.spring_mqttx.core.client.MqttClientOperations;
import com.rafaelcosta.spring_mqttx.core.logging.MqttLogSettings;
import com.rafaelcosta.spring_mqttx.core.logging.MqttPayloadLogFormatter;
import com.rafaelcosta.spring_mqttx.core.model.MqttPublishRequest;
import com.rafaelcosta.spring_mqttx.core.serialization.PayloadSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttPublishingGateway {

    private static final Logger log = LoggerFactory.getLogger(MqttPublishingGateway.class);

    private final MqttClientOperations clientOperations;
    private final PayloadSerializer payloadSerializer;
    private final MqttLogSettings logSettings;

    public MqttPublishingGateway(MqttClientOperations clientOperations, PayloadSerializer payloadSerializer) {
        this(clientOperations, payloadSerializer, MqttLogSettings.DISABLED);
    }

    public MqttPublishingGateway(MqttClientOperations clientOperations,
                                 PayloadSerializer payloadSerializer,
                                 MqttLogSettings logSettings) {
        this.clientOperations = clientOperations;
        this.payloadSerializer = payloadSerializer;
        this.logSettings = logSettings;
    }

    public void publish(MqttPublishRequest request) {
        if (!clientOperations.isConnected()) {
            log.warn("Publicacao MQTT ignorada: cliente desconectado. topico='{}', qos={}",
                    request.topic(), request.qos());
            return;
        }

        try {
            byte[] serializedPayload = payloadSerializer.write(request.payload());

            if (logSettings.isPublishEnabled() && log.isDebugEnabled()) {
                log.debug("Publicando mensagem MQTT: topico='{}', qos={}, payloadType='{}', bytes={}",
                        request.topic(), request.qos(),
                        request.payload() == null ? "null" : request.payload().getClass().getName(),
                        serializedPayload == null ? 0 : serializedPayload.length);
            }

            if (logSettings.isPayloadEnabled() && log.isTraceEnabled()) {
                log.trace("Payload MQTT serializado para envio no topico '{}': {}",
                        request.topic(), MqttPayloadLogFormatter.previewObject(request.payload()));
            }

            clientOperations.publish(request.topic(), serializedPayload, request.qos());

            log.info("Mensagem MQTT publicada com sucesso: topico='{}', qos={}", request.topic(), request.qos());
        } catch (RuntimeException e) {
            log.error("Falha ao publicar mensagem MQTT no topico '{}': {}",
                    request.topic(), e.getMessage(), e);
            throw e;
        }
    }
}
