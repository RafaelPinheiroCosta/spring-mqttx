package com.rafaelcosta.spring_mqttx.core.dispatch;

import com.rafaelcosta.spring_mqttx.core.exception.MqttDispatchException;
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
        List<MqttHandlerMatch> matches = registry.findHandlerMatches(inboundMessage.topic());

        if (logSettings.isDispatchEnabled() && log.isDebugEnabled()) {
            log.debug("Despachando mensagem MQTT: topico='{}', handlersEncontrados={}",
                    inboundMessage.topic(), matches.size());
        }

        if (matches.isEmpty()) {
            log.warn("Nenhum handler MQTT registrado para o topico '{}'. Mensagem recebida sera ignorada.",
                    inboundMessage.topic());
            return;
        }

        int index = 0;
        for (MqttHandlerMatch match : matches) {
            try {
                if (logSettings.isDispatchEnabled() && log.isTraceEnabled()) {
                    log.trace("Executando dispatch MQTT: topico='{}', handlerIndex={}, handlerTipo='{}', filtro='{}', variaveis={}",
                            inboundMessage.topic(), index, match.handler().getClass().getName(),
                            match.subscription().subscriptionFilter(), match.matchResult().variables());
                }
                match.handler().handle(new MqttInboundMessage(
                        inboundMessage.topic(),
                        inboundMessage.message(),
                        match.matchResult().variables()
                ));
                index++;
            } catch (Exception e) {
                log.error("Falha ao despachar mensagem MQTT do topico '{}' para handler '{}': {}",
                        inboundMessage.topic(), match.handler().getClass().getName(), e.getMessage(), e);
                throw new MqttDispatchException("Erro ao despachar mensagem do tópico " + inboundMessage.topic(), e);
            }
        }
    }
}
