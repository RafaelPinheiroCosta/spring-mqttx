package com.rafaelcosta.spring_mqttx.spring;

import com.rafaelcosta.spring_mqttx.core.logging.MqttLogSettings;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MqttPublisherCatalogLogger implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(MqttPublisherCatalogLogger.class);

    private final ApplicationContext applicationContext;
    private final MqttLogSettings logSettings;
    private volatile boolean running;

    public MqttPublisherCatalogLogger(ApplicationContext applicationContext, MqttLogSettings logSettings) {
        this.applicationContext = applicationContext;
        this.logSettings = logSettings;
    }

    @Override
    public void start() {
        if (!logSettings.isRegistryEnabled() || !log.isDebugEnabled()) {
            running = true;
            return;
        }

        AtomicInteger totalPublishers = new AtomicInteger();
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> targetClass = AopUtils.getTargetClass(bean);

            Map<Method, MqttPublisher> methods = MethodIntrospector.selectMethods(
                    targetClass,
                    (Method method) -> AnnotatedElementUtils.findMergedAnnotation(method, MqttPublisher.class)
            );

            methods.forEach((method, annotation) -> {
                totalPublishers.incrementAndGet();
                log.debug("Metodo publisher MQTT encontrado: bean='{}', metodo='{}', topico='{}', qos={}",
                        beanName, method.getName(), annotation.value(), annotation.qos());
            });
        }

        log.debug("Catalogo de publishers MQTT concluido. totalPublicadores={}", totalPublishers.get());
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 1;
    }
}
