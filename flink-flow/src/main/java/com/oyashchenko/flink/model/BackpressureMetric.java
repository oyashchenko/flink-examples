package com.oyashchenko.flink.model;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class BackpressureMetric {
    private String taskName;

    @JsonProperty(value = "backPressured")
    private boolean isBackPressured;
    private double backPressurePercentage;

    public BackpressureMetric() {
    }

    public BackpressureMetric(String taskName, boolean isBackPressured, double backPressurePercentage) {
        this.taskName = taskName;
        this.isBackPressured = isBackPressured;
        this.backPressurePercentage = backPressurePercentage;
    }

    public String getTaskName() {
        return taskName;
    }

    public boolean isBackPressured() {
        return isBackPressured;
    }

    public double getBackPressurePercentage() {
        return backPressurePercentage;
    }
}
