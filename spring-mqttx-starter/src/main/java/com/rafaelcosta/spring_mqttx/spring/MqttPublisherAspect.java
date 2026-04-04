package com.rafaelcosta.spring_mqttx.spring;

import com.rafaelcosta.spring_mqttx.core.connection.MqttPublishingGateway;
import com.rafaelcosta.spring_mqttx.core.logging.MqttLogSettings;
import com.rafaelcosta.spring_mqttx.core.logging.MqttPayloadLogFormatter;
import com.rafaelcosta.spring_mqttx.core.model.MqttPublishRequest;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPublisher;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class MqttPublisherAspect {

    private static final Logger log = LoggerFactory.getLogger(MqttPublisherAspect.class);

    private final MqttPublishingGateway publishingGateway;
    private final MqttLogSettings logSettings;

    public MqttPublisherAspect(MqttPublishingGateway publishingGateway, MqttLogSettings logSettings) {
        this.publishingGateway = publishingGateway;
        this.logSettings = logSettings;
    }

    @Around("@annotation(mqttPublisher)")
    public Object around(ProceedingJoinPoint joinPoint, MqttPublisher mqttPublisher) throws Throwable {
        if (logSettings.isPublishEnabled() && log.isDebugEnabled()) {
            log.debug("Metodo anotado com @MqttPublisher interceptado: classe='{}', metodo='{}', topico='{}', qos={}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    mqttPublisher.value(),
                    mqttPublisher.qos());
        }

        Object result = joinPoint.proceed();

        if (result == null) {
            if (logSettings.isPublishEnabled() && log.isDebugEnabled()) {
                log.debug("Publicacao MQTT ignorada: metodo='{}' retornou null.", joinPoint.getSignature().getName());
            }
            return null;
        }

        if (logSettings.isPayloadEnabled() && log.isTraceEnabled()) {
            log.trace("Payload retornado pelo metodo publisher '{}': {}",
                    joinPoint.getSignature().getName(), MqttPayloadLogFormatter.previewObject(result));
        }

        publishingGateway.publish(new MqttPublishRequest(mqttPublisher.value(), mqttPublisher.qos(), result));

        if (logSettings.isPublishEnabled() && log.isDebugEnabled()) {
            log.debug("Publicacao MQTT disparada via aspect: metodo='{}', topico='{}', payloadType='{}'",
                    joinPoint.getSignature().getName(),
                    mqttPublisher.value(),
                    result.getClass().getName());
        }

        return result;
    }
}
