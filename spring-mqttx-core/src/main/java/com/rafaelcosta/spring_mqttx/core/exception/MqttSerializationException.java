package com.rafaelcosta.spring_mqttx.core.exception;

public class MqttSerializationException extends RuntimeException {
    public MqttSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
