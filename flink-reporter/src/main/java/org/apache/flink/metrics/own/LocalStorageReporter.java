package org.apache.flink.metrics.own;

import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.metrics.*;
import org.apache.flink.metrics.reporter.AbstractReporter;
import org.apache.flink.metrics.reporter.InstantiateViaFactory;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.apache.flink.metrics.reporter.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;

@InstantiateViaFactory(
        factoryClassName = "org.apache.flink.metrics.own.LocalStorageReporterFactory"
)
public class LocalStorageReporter extends AbstractReporter implements  Scheduled {
    private static final Logger LOG = LoggerFactory.getLogger(LocalStorageReporter.class);
    private static final String lineSeparator = System.lineSeparator();


    public Map<Gauge<?>, String> getGauges() {
        return this.gauges;
    }


    Map<Counter, String> getCounters() {
        return this.counters;
    }


    Map<Histogram, String> getHistograms() {
        return this.histograms;
    }


    Map<Meter, String> getMeters() {
        return this.meters;
    }


    @Override
    public void report() {
        try {
            this.tryReport();
        } catch (ConcurrentModificationException var2) {
        }

    }

    private void tryReport() {
        StringBuilder builder = new StringBuilder();
        builder.append("=========================== own Starting metrics report own ===========================");
        builder.append("-- Counters -------------------------------------------------------------------");
        Iterator var2 = this.counters.entrySet().iterator();

        Map.Entry metric;
        while(var2.hasNext()) {
            metric = (Map.Entry)var2.next();
            builder.append((String)metric.getValue()).append(": ").append(((Counter)metric.getKey()).getCount());
        }

        builder.append("-- Gauges ---------------------------------------------------------------------");
        var2 = this.gauges.entrySet().iterator();

        while(var2.hasNext()) {
            metric = (Map.Entry)var2.next();
            builder.append((String)metric.getValue()).append(": ").append(((Gauge)metric.getKey()).getValue()).append(lineSeparator);
        }

        builder.append("-- Meters ---------------------------------------------------------------------").append(lineSeparator);
        var2 = this.meters.entrySet().iterator();

        while(var2.hasNext()) {
            metric = (Map.Entry)var2.next();
            builder.append((String)metric.getValue()).append(": ").append(((Meter)metric.getKey()).getRate()).append(lineSeparator);
        }

        builder.append("-- Histograms -----------------------------------------------------------------").append(lineSeparator);
        var2 = this.histograms.entrySet().iterator();

        while(var2.hasNext()) {
            metric = (Map.Entry)var2.next();
            HistogramStatistics stats = ((Histogram)metric.getKey()).getStatistics();
            builder.append((String)metric.getValue()).append(": count=").append(stats.size()).append(", min=").append(stats.getMin()).append(", max=").append(stats.getMax()).append(", mean=").append(stats.getMean()).append(", stddev=").append(stats.getStdDev()).append(", p50=").append(stats.getQuantile(0.5)).append(", p75=").append(stats.getQuantile(0.75)).append(", p95=").append(stats.getQuantile(0.95)).append(", p98=").append(stats.getQuantile(0.98)).append(", p99=").append(stats.getQuantile(0.99)).append(", p999=").append(stats.getQuantile(0.999)).append(lineSeparator);
        }

        builder.append("=========================== Finished metrics report ===========================").append(lineSeparator);
        LOG.info(builder.toString());

    }


    @Override
    public String filterCharacters(String s) {
        return s;
    }

    @Override
    public void open(MetricConfig metricConfig) {
        System.out.println("Created metrics");

    }

    @Override
    public void close() {

    }
}
