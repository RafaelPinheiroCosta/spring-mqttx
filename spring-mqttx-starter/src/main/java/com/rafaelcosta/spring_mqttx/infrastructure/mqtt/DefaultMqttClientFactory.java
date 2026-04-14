package com.rafaelcosta.spring_mqttx.infrastructure.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.UUID;

public class DefaultMqttClientFactory implements MqttClientFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultMqttClientFactory.class);

    private final MqttProperties properties;
    private final TrustMaterialBuilder trustMaterialBuilder;

    public DefaultMqttClientFactory(MqttProperties properties) {
        this(properties, new TrustMaterialBuilder(new CertificateResourceLoader()));
    }

    DefaultMqttClientFactory(MqttProperties properties, TrustMaterialBuilder trustMaterialBuilder) {
        this.properties = properties;
        this.trustMaterialBuilder = trustMaterialBuilder;
    }

    @Override
    public IMqttAsyncClient createClient() {
        try {
            String clientId = (properties.getClientId() == null || properties.getClientId().isBlank())
                    ? "spring-mqttx-" + UUID.randomUUID()
                    : properties.getClientId();

            log.info("Criando cliente MQTT: broker='{}', clientId='{}', cleanSession={}, reconnectAutomatico={}, keepAlive={}, timeout={}",
                    properties.getBrokerUrl(), clientId, properties.isCleanSession(), properties.isAutomaticReconnect(),
                    properties.getKeepAliveInterval(), properties.getConnectionTimeout());

            IMqttAsyncClient client = new MqttAsyncClient(properties.getBrokerUrl(), clientId, new MemoryPersistence());
            MqttConnectOptions options = buildOptions();
            client.connect(options).waitForCompletion();
            log.info("Cliente MQTT conectado ao broker {} com clientId={}", properties.getBrokerUrl(), clientId);
            return client;
        } catch (Exception e) {
            log.error("Falha ao criar cliente MQTT para broker '{}': {}", properties.getBrokerUrl(), e.getMessage(), e);
            throw new IllegalStateException("Não foi possível criar o cliente MQTT.", e);
        }
    }

    private MqttConnectOptions buildOptions() throws Exception {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(properties.isCleanSession());
        options.setAutomaticReconnect(properties.isAutomaticReconnect());
        options.setKeepAliveInterval(properties.getKeepAliveInterval());
        options.setConnectionTimeout(properties.getConnectionTimeout());

        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            options.setUserName(properties.getUsername());
            log.info("Autenticacao MQTT por usuario habilitada para broker '{}'.", properties.getBrokerUrl());
        }
        if (properties.getPassword() != null) {
            options.setPassword(properties.getPassword().toCharArray());
        }

        if (properties.getSsl().isEnabled()) {
            log.info("SSL MQTT habilitado: protocolo='{}', trustStore='{}', trustStoreType='{}', trustCertificate='{}', trustCertificateFormat='{}', clientCert='{}'",
                    properties.getSsl().getProtocol(),
                    properties.getSsl().getTrustStoreLocation(),
                    properties.getSsl().getTrustStoreType(),
                    properties.getSsl().getTrustCertificateLocation(),
                    properties.getSsl().getTrustCertificateFormat(),
                    hasText(properties.getSsl().getKeyStoreLocation()) ? properties.getSsl().getKeyStoreLocation() : "desabilitado");
            if (hasText(properties.getSsl().getKeyStoreLocation())) {
                log.info("Autenticacao MQTT por certificado de cliente (mTLS) habilitada: keyStore='{}', keyStoreType='{}'",
                        properties.getSsl().getKeyStoreLocation(), properties.getSsl().getKeyStoreType());
            }
            options.setSocketFactory(buildSslContext().getSocketFactory());
        }
        return options;
    }

    private SSLContext buildSslContext() throws Exception {
        TrustManagerFactory trustManagerFactory = buildTrustManagerFactory();
        KeyManagerFactory keyManagerFactory = buildKeyManagerFactory();

        SSLContext sslContext = SSLContext.getInstance(properties.getSsl().getProtocol());
        sslContext.init(
                keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
                trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null,
                null
        );
        return sslContext;
    }

    private TrustManagerFactory buildTrustManagerFactory() throws Exception {
        TrustManagerFactory trustManagerFactory = trustMaterialBuilder.build(properties.getSsl());
        if (trustManagerFactory == null) {
            log.info("Nenhum trust material MQTT customizado informado. Sera utilizado o truststore padrao da JVM.");
        }
        return trustManagerFactory;
    }

    private KeyManagerFactory buildKeyManagerFactory() throws Exception {
        if (!hasText(properties.getSsl().getKeyStoreLocation())) {
            return null;
        }

        Resource resource = new DefaultResourceLoader().getResource(properties.getSsl().getKeyStoreLocation());
        try (InputStream inputStream = resource.getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance(properties.getSsl().getKeyStoreType());
            char[] keyStorePassword = properties.getSsl().getKeyStorePassword() != null
                    ? properties.getSsl().getKeyStorePassword().toCharArray()
                    : null;
            keyStore.load(inputStream, keyStorePassword);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            char[] keyPassword = resolveKeyPassword();
            keyManagerFactory.init(keyStore, keyPassword);
            return keyManagerFactory;
        }
    }

    private char[] resolveKeyPassword() {
        if (properties.getSsl().getKeyPassword() != null) {
            return properties.getSsl().getKeyPassword().toCharArray();
        }
        if (properties.getSsl().getKeyStorePassword() != null) {
            return properties.getSsl().getKeyStorePassword().toCharArray();
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
