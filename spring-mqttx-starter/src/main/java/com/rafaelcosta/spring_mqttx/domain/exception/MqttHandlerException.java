package com.rafaelcosta.spring_mqttx.domain.exception;

public class MqttHandlerException extends RuntimeException {
    public MqttHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
