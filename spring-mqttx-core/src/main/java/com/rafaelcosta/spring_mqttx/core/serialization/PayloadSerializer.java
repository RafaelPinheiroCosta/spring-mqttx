package com.rafaelcosta.spring_mqttx.core.serialization;

public interface PayloadSerializer {
    byte[] write(Object value);
    <T> T read(byte[] payload, Class<T> type);
    String asString(byte[] payload);
}
