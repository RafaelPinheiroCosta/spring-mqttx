package com.rafaelcosta.spring_mqttx.infrastructure.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    private boolean enabled = true;
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId;
    private String username;
    private String password;
    private boolean cleanSession = true;
    private boolean automaticReconnect = true;
    private int keepAliveInterval = 60;
    private int connectionTimeout = 30;
    private int defaultQos = 1;
    private final Ssl ssl = new Ssl();
    private final Logs logs = new Logs();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public boolean isAutomaticReconnect() {
        return automaticReconnect;
    }

    public void setAutomaticReconnect(boolean automaticReconnect) {
        this.automaticReconnect = automaticReconnect;
    }

    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getDefaultQos() {
        return defaultQos;
    }

    public void setDefaultQos(int defaultQos) {
        this.defaultQos = defaultQos;
    }

    public Ssl getSsl() {
        return ssl;
    }

    public Logs getLogs() {
        return logs;
    }

    public static class Ssl {
        private boolean enabled;
        private String protocol = "TLS";
        private String trustStoreLocation;
        private String trustStorePassword;
        private String trustStoreType = "PKCS12";
        private String keyStoreLocation;
        private String keyStorePassword;
        private String keyStoreType = "PKCS12";
        private String keyPassword;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }


        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }
        public String getTrustStoreLocation() {
            return trustStoreLocation;
        }

        public void setTrustStoreLocation(String trustStoreLocation) {
            this.trustStoreLocation = trustStoreLocation;
        }

        public String getTrustStorePassword() {
            return trustStorePassword;
        }

        public void setTrustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
        }

        public String getTrustStoreType() {
            return trustStoreType;
        }

        public void setTrustStoreType(String trustStoreType) {
            this.trustStoreType = trustStoreType;
        }

        public String getKeyStoreLocation() {
            return keyStoreLocation;
        }

        public void setKeyStoreLocation(String keyStoreLocation) {
            this.keyStoreLocation = keyStoreLocation;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public void setKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
        }

        public String getKeyStoreType() {
            return keyStoreType;
        }

        public void setKeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        public String getKeyPassword() {
            return keyPassword;
        }

        public void setKeyPassword(String keyPassword) {
            this.keyPassword = keyPassword;
        }
    }

    public static class Logs {
        private boolean registry;
        private boolean subscription;
        private boolean publish;
        private boolean receive;
        private boolean payload;
        private boolean dispatch;
        private boolean invocation;

        public boolean isRegistry() {
            return registry;
        }

        public void setRegistry(boolean registry) {
            this.registry = registry;
        }

        public boolean isSubscription() {
            return subscription;
        }

        public void setSubscription(boolean subscription) {
            this.subscription = subscription;
        }

        public boolean isPublish() {
            return publish;
        }

        public void setPublish(boolean publish) {
            this.publish = publish;
        }

        public boolean isReceive() {
            return receive;
        }

        public void setReceive(boolean receive) {
            this.receive = receive;
        }

        public boolean isPayload() {
            return payload;
        }

        public void setPayload(boolean payload) {
            this.payload = payload;
        }

        public boolean isDispatch() {
            return dispatch;
        }

        public void setDispatch(boolean dispatch) {
            this.dispatch = dispatch;
        }

        public boolean isInvocation() {
            return invocation;
        }

        public void setInvocation(boolean invocation) {
            this.invocation = invocation;
        }
    }
}
