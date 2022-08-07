package com.oyashchenko.flink.restclient.kafka;

public class BackpressureMetric {
    private final String taskName;
    private final boolean isBackPressured;
    private final double backPressurePercentage;

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
