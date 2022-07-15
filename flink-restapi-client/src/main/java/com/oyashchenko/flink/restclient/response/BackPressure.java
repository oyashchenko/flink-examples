package com.oyashchenko.flink.restclient.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackPressure {
    @JsonIgnore
    private String vertexName;
    @JsonIgnore
    private String vertexId;
    @JsonProperty(value = "status")
    private String status;
    @JsonProperty(value = "backpressure-level")
    private String backpressureLevel;
    @JsonProperty(value = "end-timestamp")
    private long enfTimestamp;
    @JsonProperty(value = "subtasks")
    private List<Subtask> subtasks;

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    /**
     *
     * @return Map of task name and backpressure ration in %
     */
    public Map<String, Double> getBackPressedSubtasks() {
        Map<String, Double> metrics = new HashMap<>();
        if (subtasks != null && !subtasks.isEmpty()) {
            subtasks.stream().filter(item -> item.getBackpressureLevel().equalsIgnoreCase("high"))
                    .forEach( item ->
                            metrics.put(vertexName + item.getSubtask(), item.getRatio() * 100 )
                    );



        }

        return metrics;

    }

    public void setVertexName(String name) {
        this.vertexName = name;
    }

    public void setVertexId(String vertexId) {
       this.vertexId = vertexId;
    }
}
