package com.rafaelcosta.spring_mqttx.core.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @deprecated Use {@link Jackson2PayloadSerializer}. Mantido para compatibilidade com código existente.
 */
@Deprecated
public class JacksonPayloadSerializer extends Jackson2PayloadSerializer {

    public JacksonPayloadSerializer(ObjectMapper objectMapper) {
        super(objectMapper);
    }
}
