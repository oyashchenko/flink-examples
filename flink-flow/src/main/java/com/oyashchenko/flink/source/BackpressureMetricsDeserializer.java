package com.oyashchenko.flink.source;

import com.oyashchenko.flink.model.BackpressureMetric;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class BackpressureMetricsDeserializer implements Deserializer<BackpressureMetric> {
    @Override
    public BackpressureMetric deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(bytes, BackpressureMetric.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
