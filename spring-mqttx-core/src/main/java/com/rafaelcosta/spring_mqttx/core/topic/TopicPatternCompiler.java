package com.rafaelcosta.spring_mqttx.core.topic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TopicPatternCompiler {

    private TopicPatternCompiler() {
    }

    public static TopicPattern compile(String source) {
        Objects.requireNonNull(source, "source must not be null");
        if (source.isBlank()) {
            throw new IllegalArgumentException("MQTT topic pattern must not be blank");
        }

        String[] rawSegments = source.split("/", -1);
        List<String> segments = new ArrayList<>(rawSegments.length);
        List<String> placeholderNames = new ArrayList<>();
        List<String> filterSegments = new ArrayList<>(rawSegments.length);
        boolean hasWildcards = false;
        boolean hasPlaceholders = false;

        for (int i = 0; i < rawSegments.length; i++) {
            String segment = rawSegments[i];
            if (segment.equals("#")) {
                if (i != rawSegments.length - 1) {
                    throw new IllegalArgumentException("MQTT wildcard '#' must appear only as the last topic segment: " + source);
                }
                hasWildcards = true;
                segments.add(segment);
                filterSegments.add(segment);
                continue;
            }
            if (segment.contains("#")) {
                throw new IllegalArgumentException("MQTT wildcard '#' must occupy the whole topic segment: " + source);
            }
            if (segment.equals("+")) {
                hasWildcards = true;
                segments.add(segment);
                filterSegments.add(segment);
                continue;
            }
            if (segment.contains("+")) {
                throw new IllegalArgumentException("MQTT wildcard '+' must occupy the whole topic segment: " + source);
            }
            if (isPlaceholder(segment)) {
                String placeholderName = segment.substring(1, segment.length() - 1).trim();
                if (placeholderName.isEmpty()) {
                    throw new IllegalArgumentException("MQTT placeholder name must not be empty: " + source);
                }
                hasPlaceholders = true;
                placeholderNames.add(placeholderName);
                segments.add(segment);
                filterSegments.add("+");
                continue;
            }
            segments.add(segment);
            filterSegments.add(segment);
        }

        return new TopicPattern(
                source,
                String.join("/", filterSegments),
                List.copyOf(segments),
                List.copyOf(placeholderNames),
                hasWildcards,
                hasPlaceholders
        );
    }

    public static TopicMatchResult match(TopicPattern pattern, String actualTopic) {
        Objects.requireNonNull(pattern, "pattern must not be null");
        Objects.requireNonNull(actualTopic, "actualTopic must not be null");

        String[] actualSegments = actualTopic.split("/", -1);
        List<String> patternSegments = pattern.segments();
        Map<String, String> variables = new LinkedHashMap<>();

        int pi = 0;
        int ai = 0;

        while (pi < patternSegments.size() && ai < actualSegments.length) {
            String patternSegment = patternSegments.get(pi);
            String actualSegment = actualSegments[ai];

            if ("#".equals(patternSegment)) {
                return TopicMatchResult.matched(variables);
            }
            if ("+".equals(patternSegment)) {
                pi++;
                ai++;
                continue;
            }
            if (isPlaceholder(patternSegment)) {
                String placeholderName = patternSegment.substring(1, patternSegment.length() - 1).trim();
                variables.put(placeholderName, actualSegment);
                pi++;
                ai++;
                continue;
            }
            if (!patternSegment.equals(actualSegment)) {
                return TopicMatchResult.notMatched();
            }

            pi++;
            ai++;
        }

        if (pi < patternSegments.size() && "#".equals(patternSegments.get(pi)) && pi == patternSegments.size() - 1) {
            return TopicMatchResult.matched(variables);
        }

        if (pi == patternSegments.size() && ai == actualSegments.length) {
            return TopicMatchResult.matched(variables);
        }

        return TopicMatchResult.notMatched();
    }

    private static boolean isPlaceholder(String segment) {
        return segment.startsWith("{") && segment.endsWith("}") && segment.length() >= 3;
    }
}
