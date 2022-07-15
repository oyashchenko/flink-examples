package com.oyashchenko.flink.restclient.response;


import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class Job {
    @JsonProperty("id")
    private String id;
    @JsonProperty("status")
    private String status;


    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }
}
