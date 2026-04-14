package com.rafaelcosta.spring_mqttx.core.dispatch;

import com.rafaelcosta.spring_mqttx.core.handler.MqttMessageHandler;
import com.rafaelcosta.spring_mqttx.core.logging.MqttLogSettings;
import com.rafaelcosta.spring_mqttx.core.model.MqttSubscriptionDefinition;
import com.rafaelcosta.spring_mqttx.core.topic.TopicMatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MqttHandlerRegistry {

    private static final Logger log = LoggerFactory.getLogger(MqttHandlerRegistry.class);

    private final Map<MqttSubscriptionDefinition, List<MqttMessageHandler>> handlers = new LinkedHashMap<>();
    private final MqttLogSettings logSettings;

    public MqttHandlerRegistry() {
        this(MqttLogSettings.DISABLED);
    }

    public MqttHandlerRegistry(MqttLogSettings logSettings) {
        this.logSettings = logSettings;
    }

    public void register(MqttSubscriptionDefinition subscription, MqttMessageHandler handler) {
        List<MqttMessageHandler> registeredHandlers = handlers.computeIfAbsent(subscription, ignored -> new ArrayList<>());
        registeredHandlers.add(handler);

        if (logSettings.isRegistryEnabled() && log.isDebugEnabled()) {
            log.debug("Handler MQTT registrado: topico='{}', filtro='{}', qos={}, totalHandlersNoTopico={}",
                    subscription.topic(), subscription.subscriptionFilter(), subscription.qos(), registeredHandlers.size());
        }
    }

    public Map<MqttSubscriptionDefinition, List<MqttMessageHandler>> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }

    public List<MqttMessageHandler> getHandlersByTopic(String topic) {
        return findHandlerMatches(topic).stream().map(MqttHandlerMatch::handler).toList();
    }

    public List<MqttHandlerMatch> findHandlerMatches(String topic) {
        List<MqttHandlerMatch> matches = new ArrayList<>();

        for (Map.Entry<MqttSubscriptionDefinition, List<MqttMessageHandler>> entry : handlers.entrySet()) {
            TopicMatchResult matchResult = entry.getKey().match(topic);
            if (!matchResult.matched()) {
                continue;
            }
            for (MqttMessageHandler handler : entry.getValue()) {
                matches.add(new MqttHandlerMatch(entry.getKey(), handler, matchResult));
            }
        }

        if (logSettings.isDispatchEnabled() && log.isTraceEnabled()) {
            log.trace("Consulta de handlers por topico='{}' retornou {} handler(s).", topic, matches.size());
        }

        return List.copyOf(matches);
    }
}
