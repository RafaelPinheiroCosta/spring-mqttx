package com.rafaelcosta.spring_mqttx.core.model;

import com.rafaelcosta.spring_mqttx.core.topic.TopicMatchResult;
import com.rafaelcosta.spring_mqttx.core.topic.TopicPattern;
import com.rafaelcosta.spring_mqttx.core.topic.TopicPatternCompiler;

import java.util.Objects;

public final class MqttSubscriptionDefinition {

    private final String topic;
    private final int qos;
    private final TopicPattern pattern;

    public MqttSubscriptionDefinition(String topic, int qos) {
        this.topic = Objects.requireNonNull(topic, "topic must not be null");
        this.qos = qos;
        this.pattern = TopicPatternCompiler.compile(topic);
    }

    public String topic() {
        return topic;
    }

    public int qos() {
        return qos;
    }

    public String subscriptionFilter() {
        return pattern.subscriptionFilter();
    }

    public boolean matches(String actualTopic) {
        return match(actualTopic).matched();
    }

    public TopicMatchResult match(String actualTopic) {
        return pattern.match(actualTopic);
    }

    public boolean hasPatternSyntax() {
        return pattern.hasWildcards() || pattern.hasPlaceholders();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MqttSubscriptionDefinition that)) return false;
        return qos == that.qos && topic.equals(that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, qos);
    }

    @Override
    public String toString() {
        return "MqttSubscriptionDefinition{" +
                "topic='" + topic + '\'' +
                ", qos=" + qos +
                ", subscriptionFilter='" + subscriptionFilter() + '\'' +
                '}';
    }
}
