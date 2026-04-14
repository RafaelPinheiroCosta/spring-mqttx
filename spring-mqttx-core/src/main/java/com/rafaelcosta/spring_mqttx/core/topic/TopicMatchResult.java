package com.rafaelcosta.spring_mqttx.core.topic;

import java.util.Collections;
import java.util.Map;

public record TopicMatchResult(boolean matched, Map<String, String> variables) {

    private static final TopicMatchResult NOT_MATCHED = new TopicMatchResult(false, Map.of());

    public TopicMatchResult {
        variables = variables == null ? Map.of() : Collections.unmodifiableMap(variables);
    }

    public static TopicMatchResult matched(Map<String, String> variables) {
        return new TopicMatchResult(true, variables);
    }

    public static TopicMatchResult notMatched() {
        return NOT_MATCHED;
    }
}
