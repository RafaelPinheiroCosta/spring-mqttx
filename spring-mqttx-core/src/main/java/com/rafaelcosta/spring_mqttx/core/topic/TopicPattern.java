package com.rafaelcosta.spring_mqttx.core.topic;

import java.util.List;

public record TopicPattern(String source,
                           String subscriptionFilter,
                           List<String> segments,
                           List<String> placeholderNames,
                           boolean hasWildcards,
                           boolean hasPlaceholders) {

    public TopicMatchResult match(String actualTopic) {
        return TopicPatternCompiler.match(this, actualTopic);
    }
}
