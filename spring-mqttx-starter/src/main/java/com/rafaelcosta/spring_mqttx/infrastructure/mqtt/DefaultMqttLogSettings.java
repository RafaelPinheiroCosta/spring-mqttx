package com.rafaelcosta.spring_mqttx.infrastructure.mqtt;

import com.rafaelcosta.spring_mqttx.core.logging.MqttLogSettings;

public class DefaultMqttLogSettings implements MqttLogSettings {

    private final MqttProperties properties;

    public DefaultMqttLogSettings(MqttProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isRegistryEnabled() {
        return properties.getLogs().isRegistry();
    }

    @Override
    public boolean isSubscriptionEnabled() {
        return properties.getLogs().isSubscription();
    }

    @Override
    public boolean isPublishEnabled() {
        return properties.getLogs().isPublish();
    }

    @Override
    public boolean isReceiveEnabled() {
        return properties.getLogs().isReceive();
    }

    @Override
    public boolean isPayloadEnabled() {
        return properties.getLogs().isPayload();
    }

    @Override
    public boolean isDispatchEnabled() {
        return properties.getLogs().isDispatch();
    }

    @Override
    public boolean isInvocationEnabled() {
        return properties.getLogs().isInvocation();
    }
}
