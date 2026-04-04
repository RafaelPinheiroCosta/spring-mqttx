package com.rafaelcosta.spring_mqttx.infrastructure.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rafaelcosta.spring_mqttx.core.connection.MqttPublishingGateway;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class MqttAutoConfigurationTest {

    private final ApplicationContextRunner jackson2Runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MqttAutoConfiguration.class,
                    MqttJackson2AutoConfiguration.class
            ))
            .withUserConfiguration(Jackson2TestConfig.class)
            .withPropertyValues(
                    "mqtt.enabled=true",
                    "mqtt.broker-url=tcp://localhost:1883",
                    "mqtt.client-id=test-client"
            );

    private final ApplicationContextRunner jackson3Runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MqttAutoConfiguration.class,
                    MqttJackson3AutoConfiguration.class
            ))
            .withUserConfiguration(Jackson3TestConfig.class)
            .withPropertyValues(
                    "mqtt.enabled=true",
                    "mqtt.broker-url=tcp://localhost:1883",
                    "mqtt.client-id=test-client"
            );

    @Test
    void shouldCreateInfrastructureBeansWithJackson2() {
        jackson2Runner.run(context -> {
            assertThat(context).hasSingleBean(MqttPublishingGateway.class);
        });
    }

    @Test
    void shouldCreateInfrastructureBeansWithJackson3() {
        jackson3Runner.run(context -> {
            assertThat(context).hasSingleBean(MqttPublishingGateway.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class Jackson2TestConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        IMqttAsyncClient mqttAsyncClient() {
            return Mockito.mock(IMqttAsyncClient.class);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class Jackson3TestConfig {

        @Bean
        tools.jackson.databind.json.JsonMapper jsonMapper() {
            return tools.jackson.databind.json.JsonMapper.builder().build();
        }

        @Bean
        IMqttAsyncClient mqttAsyncClient() {
            return Mockito.mock(IMqttAsyncClient.class);
        }
    }
}