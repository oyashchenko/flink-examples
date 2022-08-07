package com.oyashchenko.flink.restclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oyashchenko.flink.restclient.kafka.BackPressureEventsProducer;
import com.oyashchenko.flink.restclient.kafka.BackpressureMetric;
import com.oyashchenko.flink.restclient.response.*;
import okhttp3.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RestClient {
    private static final String PROPERTY_FILE_NAME = "application.properties";
    private static final String PROPERTY_PATH_OPTION = "propertyPath";

    private static final String FLINK_URL_PROPERTY = "flinkUrl";
    public static final String METRICS_URL = "metricsUrl";
    private static final String JOB_ID_TEMPLATE = "{:jobId}";
    private static final String VERTEX_ID_TEMPLATE = "{:vertexId}";

    //Track metrics every 5 sec
    private static final long REPEAT_INTERVAL = 10000;
    private static final String BACK_PRESSURE_EVENT_TOPIC = "backPressureEventTopic";
    private Properties properties;
    private OkHttpClient client = new OkHttpClient();

    private BackPressureEventsProducer backPressureEventsProducer;


    public static void main(String[] args) {
        try {
            RestClient restClient = new RestClient();
            restClient.run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }


    public RestClient() throws IOException {
        loadProperties();
        backPressureEventsProducer = new BackPressureEventsProducer(properties);

    }

    public void run() {
        String jobId = getRunningJobId();
        List<Vertex> vertexList = getVertexDetails(jobId);
        List<BackPressure> subtasks = new ArrayList<>();
        while (true) {
            vertexList.forEach(vertex -> {
                System.out.println("Getting metrics for " + vertex.getName() + ":" + vertex.getId());
                BackPressure backPressureMetrics = getBackPressureSubtasksMetrics(jobId, vertex.getId());
                backPressureMetrics.setVertexName(vertex.getName());
                backPressureMetrics.setVertexId(vertex.getId());
                subtasks.add(backPressureMetrics);
            });

            subtasks.stream().forEach(
                    backPressure -> {
                        Map<String, Double> subtaskUnderBackPressure = backPressure.getBackPressedSubtasks();
                        if (subtaskUnderBackPressure != null && !subtaskUnderBackPressure.isEmpty()) {
                            subtaskUnderBackPressure.entrySet().stream().forEach(
                                    task -> {
                                        backPressureEventsProducer.publish(BACK_PRESSURE_EVENT_TOPIC, new BackpressureMetric(
                                                task.getKey(), false, task.getValue()
                                        ));
                                        System.out.println(task.getKey() + " :backpressure % :" + task.getValue());
                                    }
                            );
                        }
                    }
            );
            try {
                Thread.sleep(REPEAT_INTERVAL);//20 sek
            } catch (InterruptedException e) {
                System.out.println("Interrupted :" + e.getMessage());
                break;
            }
        }
    }

    public String getRunningJobId() {
        Call listJobsUrl = null;
        try {
            Response res = client.newCall(new Request.Builder().url(
                    properties.getProperty(FLINK_URL_PROPERTY) + properties.getProperty("listJobsUrl")
            ).build()).execute();

            if (res.isSuccessful()) {
                ///System.out.println("Response" + res.body().string());
                JobsResponse jobs = new ObjectMapper().readValue(res.body().string(),
                        /*new TypeReference<List<Job>>(){}*/ JobsResponse.class);
                Optional<Job> running = jobs.getJobs().stream().filter(item -> "RUNNING".equalsIgnoreCase(item.getStatus())).findFirst();
                return running.isPresent() ? running.get().getId() : null;
            } else {
                System.out.println("HTTP Code " + res.code() + " :" + res.body().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void loadProperties() throws IOException {
        properties = new Properties();
        if (System.getProperty(PROPERTY_PATH_OPTION) != null) {
            Path path = Paths.get(System.getProperty(PROPERTY_PATH_OPTION) + PROPERTY_FILE_NAME);
            properties.load(new FileInputStream(path.toFile()));
        } else {
            properties.load(this.getClass().getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME));
        }

    }

    private List<Vertex> getVertexDetails(String jobId) {
        Request req = new Request.Builder().url(properties.getProperty(FLINK_URL_PROPERTY) +
                properties.getProperty("jobdetailsUrl").replace(JOB_ID_TEMPLATE, jobId)).build();


        try {
            Response res = client.newCall(req).execute();
            if (res.isSuccessful()) {
                JobDetails details = new ObjectMapper().readValue(res.body().string(), JobDetails.class);
                return details.getVertices();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return Collections.EMPTY_LIST;

    }

    private BackPressure getBackPressureSubtasksMetrics(String jobId, String vertexId) {
        HttpUrl req = HttpUrl.parse(properties.getProperty(FLINK_URL_PROPERTY) +
                properties.getProperty("vertexes").replace(JOB_ID_TEMPLATE, jobId).replace(VERTEX_ID_TEMPLATE, vertexId)
        );

        try {
            Response res = client.newCall(new Request.Builder().url(req.url()).build()).execute();
            if (res.isSuccessful()) {
                BackPressure backPressureDetails = new ObjectMapper().readValue(res.body().string(), BackPressure.class);
                return backPressureDetails;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }


}
