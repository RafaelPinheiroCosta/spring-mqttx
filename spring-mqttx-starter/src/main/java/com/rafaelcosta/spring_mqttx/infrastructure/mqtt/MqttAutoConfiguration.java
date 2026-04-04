package com.rafaelcosta.spring_mqttx.infrastructure.mqtt;

import com.rafaelcosta.spring_mqttx.core.client.MqttClientOperations;
import com.rafaelcosta.spring_mqttx.core.client.PahoMqttClientAdapter;
import com.rafaelcosta.spring_mqttx.core.connection.MqttPublishingGateway;
import com.rafaelcosta.spring_mqttx.core.dispatch.MqttHandlerRegistry;
import com.rafaelcosta.spring_mqttx.core.dispatch.MqttMessageDispatcher;
import com.rafaelcosta.spring_mqttx.core.logging.MqttLogSettings;
import com.rafaelcosta.spring_mqttx.core.serialization.PayloadSerializer;
import com.rafaelcosta.spring_mqttx.spring.MqttPublisherAspect;
import com.rafaelcosta.spring_mqttx.spring.MqttPublisherCatalogLogger;
import com.rafaelcosta.spring_mqttx.spring.MqttSubscriberProcessor;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@AutoConfiguration(afterName = {
        "com.rafaelcosta.spring_mqttx.infrastructure.mqtt.MqttJackson2AutoConfiguration",
        "com.rafaelcosta.spring_mqttx.infrastructure.mqtt.MqttJackson3AutoConfiguration"
})
@ConditionalOnClass(IMqttAsyncClient.class)
@ConditionalOnProperty(prefix = "mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(MqttProperties.class)
public class MqttAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MqttLogSettings mqttLogSettings(MqttProperties properties) {
        return new DefaultMqttLogSettings(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttClientFactory mqttClientFactory(MqttProperties properties) {
        return new DefaultMqttClientFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public IMqttAsyncClient mqttAsyncClient(MqttClientFactory mqttClientFactory) {
        return mqttClientFactory.createClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttConnectionManager mqttConnectionManager(IMqttAsyncClient client) {
        return new MqttConnectionManager(client);
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttClientOperations mqttClientOperations(IMqttAsyncClient client, MqttLogSettings mqttLogSettings) {
        return new PahoMqttClientAdapter(client, mqttLogSettings);
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttHandlerRegistry mqttHandlerRegistry(MqttLogSettings mqttLogSettings) {
        return new MqttHandlerRegistry(mqttLogSettings);
    }

    @Bean
    @ConditionalOnMissingBean
    public MqttMessageDispatcher mqttMessageDispatcher(MqttHandlerRegistry registry, MqttLogSettings mqttLogSettings) {
        return new MqttMessageDispatcher(registry, mqttLogSettings);
    }

    @Bean
    @ConditionalOnBean(PayloadSerializer.class)
    @ConditionalOnMissingBean
    public MqttPublishingGateway mqttPublishingGateway(
            MqttClientOperations clientOperations,
            PayloadSerializer payloadSerializer,
            MqttLogSettings mqttLogSettings
    ) {
        return new MqttPublishingGateway(clientOperations, payloadSerializer, mqttLogSettings);
    }


    @Bean
    @ConditionalOnMissingBean
    public MqttPublisherCatalogLogger mqttPublisherCatalogLogger(ApplicationContext applicationContext, MqttLogSettings mqttLogSettings) {
        return new MqttPublisherCatalogLogger(applicationContext, mqttLogSettings);
    }

    @Bean
    @ConditionalOnBean(PayloadSerializer.class)
    @ConditionalOnMissingBean
    public MqttSubscriberProcessor mqttSubscriberProcessor(
            ApplicationContext applicationContext,
            MqttClientOperations clientOperations,
            MqttHandlerRegistry registry,
            MqttMessageDispatcher dispatcher,
            PayloadSerializer payloadSerializer,
            MqttLogSettings mqttLogSettings
    ) {
        return new MqttSubscriberProcessor(
                applicationContext,
                clientOperations,
                registry,
                dispatcher,
                payloadSerializer,
                mqttLogSettings
        );
    }

    @Bean
    @ConditionalOnBean(MqttPublishingGateway.class)
    @ConditionalOnMissingBean
    public MqttPublisherAspect mqttPublisherAspect(MqttPublishingGateway publishingGateway, MqttLogSettings mqttLogSettings) {
        return new MqttPublisherAspect(publishingGateway, mqttLogSettings);
    }
}
