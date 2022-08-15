package com.oyashchenko.flink.operstions;

import com.oyashchenko.flink.model.BackpressureMetric;
import com.oyashchenko.flink.model.PriceTick;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BroadcastPriceBackPressureMetricsProcessFunction extends BroadcastProcessFunction<PriceTick, BackpressureMetric, PriceTick> {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcastPriceBackPressureMetricsProcessFunction.class);
    private final MapStateDescriptor descriptor = BackPressureMetricsStoreFunction.getMetricsState();


    public void processElement(PriceTick priceTick, KeyedBroadcastProcessFunction<Integer, PriceTick, BackpressureMetric, PriceTick>.ReadOnlyContext readOnlyContext, Collector<PriceTick> collector) throws Exception {
        LOG.info("Current key - " + readOnlyContext.getCurrentKey());
        Iterable<Map.Entry<String, BackpressureMetric>> broadcastState = readOnlyContext.getBroadcastState(descriptor).immutableEntries();
        if (!isUnderBackPressurePrice(broadcastState)) {
            collector.collect(priceTick);
        }

    }


    public void processBroadcastElement(BackpressureMetric backpressureMetric, KeyedBroadcastProcessFunction<Integer, PriceTick, BackpressureMetric, PriceTick>.Context context, Collector<PriceTick> collector) throws Exception {
         context.getBroadcastState(descriptor).put(backpressureMetric.getTaskName(), backpressureMetric);
    }

    @Override
    public void processElement(PriceTick priceTick, BroadcastProcessFunction<PriceTick, BackpressureMetric, PriceTick>.ReadOnlyContext readOnlyContext, Collector<PriceTick> collector) throws Exception {
        Iterable<Map.Entry<String, BackpressureMetric>> broadcastState = readOnlyContext.getBroadcastState(descriptor).immutableEntries();
        if (!isUnderBackPressurePrice(broadcastState)) {
            collector.collect(priceTick);
           // LOG.info("Collected price" + priceTick);
        } else {
            LOG.info("Excluded due to under backpressure : " + priceTick);
        }

    }

    @Override
    public void processBroadcastElement(BackpressureMetric backpressureMetric, BroadcastProcessFunction<PriceTick, BackpressureMetric, PriceTick>.Context context, Collector<PriceTick> collector) throws Exception {
        LOG.info("PUT EVENT:" + backpressureMetric.getTaskName());
        context.getBroadcastState(descriptor).put(backpressureMetric.getTaskName(), backpressureMetric);
    }

    private boolean isUnderBackPressurePrice(Iterable<Map.Entry<String, BackpressureMetric>> iterable) {
        boolean underBackPressure = false;

        for (Map.Entry<String, BackpressureMetric> item : iterable) {
            LOG.info("IN LOOP : " + item.getKey());
            if (item.getKey().startsWith("Price logic")) {
                BackpressureMetric metric = item.getValue();
                LOG.info(item.getKey() +":" +metric.getBackPressurePercentage());
                if (metric.getBackPressurePercentage() > 60 ) {
                    underBackPressure = true;
                    LOG.info("IN BACKPRESSURE");
                    break;
                }
            }
        }
        return underBackPressure;
    }



}
