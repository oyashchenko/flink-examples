package com.oyashchenko.flink.restclient.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Subtask {
    @JsonProperty("subtask")
    private String subtask;

    @JsonProperty(value = "backpressure-level")
    private String backpressureLevel;

    @JsonProperty(value = "ratio")
    private double ratio;

    @JsonProperty(value = "idleRatio")
    private double idleRatio;

    @JsonProperty(value = "busyRatio")
    private double busyRatio;

    public String getBackpressureLevel() {
        return backpressureLevel;
    }

    public double getRatio() {
        return ratio;
    }

    public String getSubtask() {
        return subtask;
    }
}

