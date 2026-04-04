package com.rafaelcosta.spring_mqttx.core.exception;

public class MqttPublishException extends RuntimeException {
    public MqttPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
