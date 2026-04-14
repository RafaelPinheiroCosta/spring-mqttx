package com.rafaelcosta.spring_mqttx.spring;

import com.rafaelcosta.spring_mqttx.core.handler.MqttMessageHandler;
import com.rafaelcosta.spring_mqttx.core.logging.MqttLogSettings;
import com.rafaelcosta.spring_mqttx.core.logging.MqttPayloadLogFormatter;
import com.rafaelcosta.spring_mqttx.core.model.MqttInboundMessage;
import com.rafaelcosta.spring_mqttx.core.serialization.PayloadSerializer;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPayload;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttTopicParam;
import com.rafaelcosta.spring_mqttx.domain.exception.MqttHandlerException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SpringMqttMethodHandler implements MqttMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(SpringMqttMethodHandler.class);

    private final ApplicationContext applicationContext;
    private final String beanName;
    private final Method method;
    private final PayloadSerializer payloadSerializer;
    private final MqttLogSettings logSettings;

    public SpringMqttMethodHandler(ApplicationContext applicationContext,
                                   String beanName,
                                   Method method,
                                   PayloadSerializer payloadSerializer,
                                   MqttLogSettings logSettings) {
        this.applicationContext = applicationContext;
        this.beanName = beanName;
        this.method = method;
        this.payloadSerializer = payloadSerializer;
        this.logSettings = logSettings;
    }

    @Override
    public void handle(MqttInboundMessage inboundMessage) {
        try {
            Object bean = applicationContext.getBean(beanName);
            Method targetMethod = AopUtils.getMostSpecificMethod(method, bean.getClass());

            if (logSettings.isInvocationEnabled() && log.isDebugEnabled()) {
                log.debug("Invocando handler MQTT: bean='{}', metodo='{}', topico='{}'",
                        beanName, targetMethod.getName(), inboundMessage.topic());
            }

            Object[] args = resolveArguments(targetMethod, inboundMessage);
            targetMethod.setAccessible(true);
            targetMethod.invoke(bean, args);

            if (logSettings.isInvocationEnabled() && log.isDebugEnabled()) {
                log.debug("Handler MQTT executado com sucesso: bean='{}', metodo='{}', topico='{}'",
                        beanName, targetMethod.getName(), inboundMessage.topic());
            }
        } catch (Exception e) {
            log.error("Erro ao invocar handler MQTT {}#{} para topico '{}': {}",
                    beanName, method.getName(), inboundMessage.topic(), e.getMessage(), e);
            throw new MqttHandlerException("Erro ao invocar handler MQTT " + beanName + "#" + method.getName(), e);
        }
    }

    private Object[] resolveArguments(Method targetMethod, MqttInboundMessage inboundMessage) {
        Parameter[] parameters = targetMethod.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(MqttPayload.class)) {
                args[i] = payloadSerializer.read(inboundMessage.message().getPayload(), parameter.getType());
                logParameterResolution(targetMethod, parameter, "@MqttPayload", args[i]);
            } else if (parameter.isAnnotationPresent(MqttTopicParam.class)) {
                MqttTopicParam annotation = parameter.getAnnotation(MqttTopicParam.class);
                args[i] = resolveTopicParamValue(parameter, annotation, inboundMessage);
                logParameterResolution(targetMethod, parameter, "@MqttTopicParam", args[i]);
            } else if (parameter.getType().equals(String.class)) {
                args[i] = payloadSerializer.asString(inboundMessage.message().getPayload());
                logParameterResolution(targetMethod, parameter, "String", args[i]);
            } else if (parameter.getType().equals(MqttMessage.class)) {
                args[i] = inboundMessage.message();
                logParameterResolution(targetMethod, parameter, "MqttMessage", "mensagem bruta");
            } else if (parameter.getType().equals(byte[].class)) {
                args[i] = inboundMessage.message().getPayload();
                logParameterResolution(targetMethod, parameter, "byte[]", MqttPayloadLogFormatter.describeBytes((byte[]) args[i]));
            } else {
                args[i] = null;
                log.warn("Parametro MQTT nao suportado. bean='{}', metodo='{}', parametro='{}', tipo='{}'. Valor null sera injetado.",
                        beanName, targetMethod.getName(), parameter.getName(), parameter.getType().getName());
            }
        }

        if (logSettings.isInvocationEnabled() && log.isTraceEnabled()) {
            String types = Arrays.stream(args)
                    .map(value -> value == null ? "null" : value.getClass().getSimpleName())
                    .collect(Collectors.joining(", "));
            log.trace("Argumentos resolvidos para bean='{}', metodo='{}': [{}]",
                    beanName, targetMethod.getName(), types);
        }

        return args;
    }

    private Object resolveTopicParamValue(Parameter parameter,
                                          MqttTopicParam annotation,
                                          MqttInboundMessage inboundMessage) {
        String value = inboundMessage.topicParameters().get(annotation.value());
        if (value == null) {
            return null;
        }
        Class<?> targetType = parameter.getType();
        try {
            if (targetType.equals(String.class)) {
                return value;
            }
            if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                return Integer.parseInt(value);
            }
            if (targetType.equals(Long.class) || targetType.equals(long.class)) {
                return Long.parseLong(value);
            }
            if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                return Boolean.parseBoolean(value);
            }
            if (targetType.equals(Double.class) || targetType.equals(double.class)) {
                return Double.parseDouble(value);
            }
            if (targetType.equals(Float.class) || targetType.equals(float.class)) {
                return Float.parseFloat(value);
            }
            if (targetType.equals(Short.class) || targetType.equals(short.class)) {
                return Short.parseShort(value);
            }
            if (targetType.equals(Byte.class) || targetType.equals(byte.class)) {
                return Byte.parseByte(value);
            }
        } catch (RuntimeException ex) {
            throw new MqttHandlerException("Falha ao converter placeholder MQTT '" + annotation.value() + "' para o tipo " + targetType.getName(), ex);
        }

        log.warn("Tipo nao suportado para @MqttTopicParam. bean='{}', metodo='{}', parametro='{}', tipo='{}'. Valor String sera injetado se compativel.",
                beanName, method.getName(), parameter.getName(), parameter.getType().getName());
        return value;
    }

    private void logParameterResolution(Method targetMethod, Parameter parameter, String strategy, Object resolvedValue) {
        if (logSettings.isInvocationEnabled() && log.isTraceEnabled()) {
            log.trace("Parametro MQTT resolvido: bean='{}', metodo='{}', parametro='{}', tipo='{}', estrategia='{}'",
                    beanName,
                    targetMethod.getName(),
                    parameter.getName(),
                    parameter.getType().getName(),
                    strategy);
        }
        if (logSettings.isPayloadEnabled() && log.isTraceEnabled() && resolvedValue != null) {
            String preview = resolvedValue instanceof byte[] bytes
                    ? MqttPayloadLogFormatter.preview(bytes)
                    : MqttPayloadLogFormatter.previewObject(resolvedValue);
            log.trace("Preview do parametro MQTT resolvido: bean='{}', metodo='{}', parametro='{}', valor='{}'",
                    beanName, targetMethod.getName(), parameter.getName(), preview);
        }
    }
}
