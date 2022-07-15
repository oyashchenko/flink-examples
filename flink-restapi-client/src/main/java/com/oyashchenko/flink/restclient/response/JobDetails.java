package com.oyashchenko.flink.restclient.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JobDetails {
//    @JsonProperty(value = "jid" )
//    @JsonIgnore(value = true)
//    private String jid;
   @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "vertices")
    List<Vertex> vertices;

    public List<Vertex> getVertices() {
        return vertices;
    }
}
