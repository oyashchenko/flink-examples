package com.oyashchenko.flink.restclient.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

public class BackPressureMetricsSerializer implements Serializer<BackpressureMetric> {

    @Override
    public byte[] serialize(String s, BackpressureMetric backpressureMetric) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(backpressureMetric).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
