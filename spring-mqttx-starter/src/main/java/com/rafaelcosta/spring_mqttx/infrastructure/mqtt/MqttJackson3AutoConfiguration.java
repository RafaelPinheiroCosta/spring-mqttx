package com.rafaelcosta.spring_mqttx.infrastructure.mqtt;

import com.rafaelcosta.spring_mqttx.core.serialization.Jackson3PayloadSerializer;
import com.rafaelcosta.spring_mqttx.core.serialization.PayloadSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(afterName = {
        "org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration",
        "org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration"
})
@ConditionalOnClass(name = "tools.jackson.databind.json.JsonMapper")
@ConditionalOnBean(type = "tools.jackson.databind.json.JsonMapper")
public class MqttJackson3AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PayloadSerializer.class)
    public PayloadSerializer payloadSerializer(
            tools.jackson.databind.json.JsonMapper jsonMapper
    ) {
        return new Jackson3PayloadSerializer(jsonMapper);
    }
}