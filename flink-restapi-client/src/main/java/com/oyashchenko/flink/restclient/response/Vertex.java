package com.oyashchenko.flink.restclient.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Vertex {
    @JsonProperty(value = "id")
    private String id;
    @JsonProperty(value = "name")
    private String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
