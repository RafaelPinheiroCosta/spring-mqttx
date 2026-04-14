package com.rafaelcosta.spring_mqttx.spring;

import com.rafaelcosta.spring_mqttx.core.client.MqttClientOperations;
import com.rafaelcosta.spring_mqttx.core.dispatch.MqttHandlerRegistry;
import com.rafaelcosta.spring_mqttx.core.dispatch.MqttMessageDispatcher;
import com.rafaelcosta.spring_mqttx.core.logging.MqttLogSettings;
import com.rafaelcosta.spring_mqttx.core.logging.MqttPayloadLogFormatter;
import com.rafaelcosta.spring_mqttx.core.model.MqttInboundMessage;
import com.rafaelcosta.spring_mqttx.core.model.MqttSubscriptionDefinition;
import com.rafaelcosta.spring_mqttx.core.serialization.PayloadSerializer;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;

public class MqttSubscriberProcessor implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(MqttSubscriberProcessor.class);

    private final ApplicationContext applicationContext;
    private final MqttClientOperations clientOperations;
    private final MqttHandlerRegistry handlerRegistry;
    private final MqttMessageDispatcher dispatcher;
    private final PayloadSerializer payloadSerializer;
    private final MqttLogSettings logSettings;
    private volatile boolean running;

    public MqttSubscriberProcessor(ApplicationContext applicationContext,
                                   MqttClientOperations clientOperations,
                                   MqttHandlerRegistry handlerRegistry,
                                   MqttMessageDispatcher dispatcher,
                                   PayloadSerializer payloadSerializer,
                                   MqttLogSettings logSettings) {
        this.applicationContext = applicationContext;
        this.clientOperations = clientOperations;
        this.handlerRegistry = handlerRegistry;
        this.dispatcher = dispatcher;
        this.payloadSerializer = payloadSerializer;
        this.logSettings = logSettings;
    }

    @Override
    public void start() {
        log.info("Iniciando processamento de subscribers MQTT.");
        registerHandlers();
        subscribeTopics();
        configureCallback();
        running = true;
        log.info("MqttSubscriberProcessor inicializado. handlers={}, topicos={}",
                countHandlers(), handlerRegistry.getHandlers().size());
    }

    private void registerHandlers() {
        if (logSettings.isRegistryEnabled() && log.isDebugEnabled()) {
            log.debug("Iniciando varredura de beans para metodos anotados com @MqttSubscriber.");
        }

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> targetClass = AopUtils.getTargetClass(bean);

            if (logSettings.isRegistryEnabled() && log.isTraceEnabled()) {
                log.trace("Analisando bean para subscribers MQTT: bean='{}', classe='{}'", beanName, targetClass.getName());
            }

            Map<Method, MqttSubscriber> methods = MethodIntrospector.selectMethods(
                    targetClass,
                    (Method method) -> AnnotatedElementUtils.findMergedAnnotation(method, MqttSubscriber.class)
            );

            methods.forEach((method, annotation) -> {
                MqttSubscriptionDefinition subscription = new MqttSubscriptionDefinition(annotation.value(), annotation.qos());
                if (logSettings.isRegistryEnabled() && log.isDebugEnabled()) {
                    log.debug("Metodo subscriber MQTT encontrado: bean='{}', metodo='{}', topico='{}', filtro='{}', qos={}",
                            beanName, method.getName(), subscription.topic(), subscription.subscriptionFilter(), subscription.qos());
                }
                handlerRegistry.register(
                        subscription,
                        new SpringMqttMethodHandler(applicationContext, beanName, method, payloadSerializer, logSettings)
                );
            });
        }
    }

    private void subscribeTopics() {
        for (MqttSubscriptionDefinition subscription : handlerRegistry.getHandlers().keySet()) {
            clientOperations.subscribe(subscription.subscriptionFilter(), subscription.qos());
            if (logSettings.isSubscriptionEnabled() && log.isDebugEnabled()) {
                log.debug("Inscricao MQTT concluida: topico='{}', filtro='{}', qos={}",
                        subscription.topic(), subscription.subscriptionFilter(), subscription.qos());
            }
        }
    }

    private void configureCallback() {
        clientOperations.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.warn("Conexao MQTT perdida. Motivo: {}", cause == null ? "desconhecido" : cause.getMessage(), cause);
                try {
                    clientOperations.reconnect();
                    log.info("Reconexao MQTT solicitada apos perda de conexao.");
                } catch (Exception e) {
                    log.error("Falha ao solicitar reconexao MQTT apos perda de conexao: {}", e.getMessage(), e);
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                if (logSettings.isReceiveEnabled() && log.isDebugEnabled()) {
                    log.debug("Mensagem MQTT recebida: topico='{}', qos={}, bytes={}",
                            topic, message.getQos(), message.getPayload() == null ? 0 : message.getPayload().length);
                }
                if (logSettings.isPayloadEnabled() && log.isTraceEnabled()) {
                    log.trace("Payload MQTT recebido no topico '{}': {}",
                            topic, MqttPayloadLogFormatter.preview(message.getPayload()));
                }
                try {
                    dispatcher.dispatch(new MqttInboundMessage(topic, message));
                } catch (Exception e) {
                    log.error("Falha ao processar mensagem recebida no topico '{}': {}", topic, e.getMessage(), e);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                if (logSettings.isPublishEnabled() && log.isTraceEnabled()) {
                    log.trace("Entrega MQTT confirmada pelo broker. messageId={}", token == null ? null : token.getMessageId());
                }
            }
        });
    }

    @Override
    public void stop() {
        running = false;
        log.info("MqttSubscriberProcessor finalizado.");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    private int countHandlers() {
        return handlerRegistry.getHandlers().values().stream().mapToInt(java.util.List::size).sum();
    }
}
