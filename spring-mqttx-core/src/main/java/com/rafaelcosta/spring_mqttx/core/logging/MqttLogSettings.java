package com.rafaelcosta.spring_mqttx.core.logging;

public interface MqttLogSettings {

    boolean isRegistryEnabled();

    boolean isSubscriptionEnabled();

    boolean isPublishEnabled();

    boolean isReceiveEnabled();

    boolean isPayloadEnabled();

    boolean isDispatchEnabled();

    boolean isInvocationEnabled();

    MqttLogSettings DISABLED = new MqttLogSettings() {
        @Override
        public boolean isRegistryEnabled() {
            return false;
        }

        @Override
        public boolean isSubscriptionEnabled() {
            return false;
        }

        @Override
        public boolean isPublishEnabled() {
            return false;
        }

        @Override
        public boolean isReceiveEnabled() {
            return false;
        }

        @Override
        public boolean isPayloadEnabled() {
            return false;
        }

        @Override
        public boolean isDispatchEnabled() {
            return false;
        }

        @Override
        public boolean isInvocationEnabled() {
            return false;
        }
    };
}
