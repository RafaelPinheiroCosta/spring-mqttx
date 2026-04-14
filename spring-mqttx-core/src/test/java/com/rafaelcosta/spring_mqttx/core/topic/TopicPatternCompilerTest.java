package com.rafaelcosta.spring_mqttx.core.topic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopicPatternCompilerTest {

    @Test
    void shouldMatchLiteralTopic() {
        TopicPattern pattern = TopicPatternCompiler.compile("devices/iot01/state");
        assertTrue(pattern.match("devices/iot01/state").matched());
        assertFalse(pattern.match("devices/iot02/state").matched());
    }

    @Test
    void shouldMatchSingleLevelWildcard() {
        TopicPattern pattern = TopicPatternCompiler.compile("devices/+/state");
        assertTrue(pattern.match("devices/iot01/state").matched());
        assertFalse(pattern.match("devices/iot01/state/extra").matched());
    }

    @Test
    void shouldMatchMultiLevelWildcard() {
        TopicPattern pattern = TopicPatternCompiler.compile("devices/#");
        assertTrue(pattern.match("devices/iot01/state").matched());
        assertTrue(pattern.match("devices").matched());
    }

    @Test
    void shouldMatchPlaceholderAndExtractVariable() {
        TopicPattern pattern = TopicPatternCompiler.compile("devices/{deviceId}/state");
        TopicMatchResult result = pattern.match("devices/iot01/state");
        assertTrue(result.matched());
        assertEquals("iot01", result.variables().get("deviceId"));
        assertEquals("devices/+/state", pattern.subscriptionFilter());
    }
}
