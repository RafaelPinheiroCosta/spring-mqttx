package com.rafaelcosta.spring_mqttx.core.dispatch;

import com.rafaelcosta.spring_mqttx.core.handler.MqttMessageHandler;
import com.rafaelcosta.spring_mqttx.core.model.MqttSubscriptionDefinition;
import com.rafaelcosta.spring_mqttx.core.topic.TopicMatchResult;

public record MqttHandlerMatch(MqttSubscriptionDefinition subscription,
                               MqttMessageHandler handler,
                               TopicMatchResult matchResult) {
}
