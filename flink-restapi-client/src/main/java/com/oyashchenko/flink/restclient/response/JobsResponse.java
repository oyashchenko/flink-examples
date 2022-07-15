package com.oyashchenko.flink.restclient.response;



import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class JobsResponse {
    @JsonProperty(value = "jobs")
    private List<Job> jobs;

    public List<Job> getJobs() {
        return jobs;
    }
}
