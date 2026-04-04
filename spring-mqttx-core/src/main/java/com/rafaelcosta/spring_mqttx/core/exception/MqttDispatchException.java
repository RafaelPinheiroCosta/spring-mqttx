package com.rafaelcosta.spring_mqttx.core.exception;

public class MqttDispatchException extends RuntimeException {
    public MqttDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
