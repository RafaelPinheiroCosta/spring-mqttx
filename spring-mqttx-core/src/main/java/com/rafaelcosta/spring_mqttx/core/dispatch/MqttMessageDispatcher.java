package com.rafaelcosta.spring_mqttx.core.dispatch;

import com.rafaelcosta.spring_mqttx.core.exception.MqttDispatchException;
import com.rafaelcosta.spring_mqttx.core.handler.MqttMessageHandler;
import com.rafaelcosta.spring_mqttx.core.logging.MqttLogSettings;
import com.rafaelcosta.spring_mqttx.core.model.MqttInboundMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MqttMessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(MqttMessageDispatcher.class);

    private final MqttHandlerRegistry registry;
    private final MqttLogSettings logSettings;

    public MqttMessageDispatcher(MqttHandlerRegistry registry) {
        this(registry, MqttLogSettings.DISABLED);
    }

    public MqttMessageDispatcher(MqttHandlerRegistry registry, MqttLogSettings logSettings) {
        this.registry = registry;
        this.logSettings = logSettings;
    }

    public void dispatch(MqttInboundMessage inboundMessage) {
        List<MqttMessageHandler> handlers = registry.getHandlersByTopic(inboundMessage.topic());

        if (logSettings.isDispatchEnabled() && log.isDebugEnabled()) {
            log.debug("Despachando mensagem MQTT: topico='{}', handlersEncontrados={}",
                    inboundMessage.topic(), handlers.size());
        }

        if (handlers.isEmpty()) {
            log.warn("Nenhum handler MQTT registrado para o topico '{}'. Mensagem recebida sera ignorada.",
                    inboundMessage.topic());
            return;
        }

        int index = 0;
        for (MqttMessageHandler handler : handlers) {
            try {
                if (logSettings.isDispatchEnabled() && log.isTraceEnabled()) {
                    log.trace("Executando dispatch MQTT: topico='{}', handlerIndex={}, handlerTipo='{}'",
                            inboundMessage.topic(), index, handler.getClass().getName());
                }
                handler.handle(inboundMessage);
                index++;
            } catch (Exception e) {
                log.error("Falha ao despachar mensagem MQTT do topico '{}' para handler '{}': {}",
                        inboundMessage.topic(), handler.getClass().getName(), e.getMessage(), e);
                throw new MqttDispatchException("Erro ao despachar mensagem do tópico " + inboundMessage.topic(), e);
            }
        }
    }
}
