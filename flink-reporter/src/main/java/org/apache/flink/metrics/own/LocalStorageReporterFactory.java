package org.apache.flink.metrics.own;

import org.apache.flink.metrics.reporter.InterceptInstantiationViaReflection;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.apache.flink.metrics.reporter.MetricReporterFactory;


import java.util.Properties;

@InterceptInstantiationViaReflection(
        reporterClassName = "org.apache.flink.metrics.own.LocalStorageReporter"
)
public class LocalStorageReporterFactory implements MetricReporterFactory {

    public LocalStorageReporterFactory() {}

    public MetricReporter createMetricReporter(Properties properties) {

        return new LocalStorageReporter();
    }

}
