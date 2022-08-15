package com.oyashchenko.flink.operations;

import com.oyashchenko.flink.model.BackpressureMetric;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class BackPressureMetricsStoreFunction extends RichFlatMapFunction<BackpressureMetric, String> {
    private transient MapState<String, Double> taskBackPressureSate;
    @Override
    public void flatMap(BackpressureMetric backpressureMetric, Collector<String> collector) throws Exception {
        taskBackPressureSate.put(backpressureMetric.getTaskName(), backpressureMetric.getBackPressurePercentage());
        collector.collect("Updated state for " + backpressureMetric.getTaskName());
    }

    @Override
    public void open(Configuration config) {
        MapStateDescriptor<String, Double> descriptor =
                new MapStateDescriptor<String, Double>(
                        "backPressureMetrics", TypeInformation.of(String.class),
        TypeInformation.of(Double.class));

        taskBackPressureSate = getRuntimeContext().getMapState(descriptor);
    }

    public static MapStateDescriptor<String, BackpressureMetric> getMetricsState() {
        return  new MapStateDescriptor<String, BackpressureMetric>(
            "backPressureMetrics", TypeInformation.of(String.class),
            TypeInformation.of(new TypeHint<BackpressureMetric>() {})
        );
    }
}
