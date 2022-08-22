package com.oyashchenko.flink.sink;

import com.oyashchenko.flink.model.PriceTick;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class PriceSink extends RichSinkFunction<PriceTick> {
    private static final Logger LOG = LoggerFactory.getLogger(PriceSink.class);
    private static final AtomicLong counter = new AtomicLong();

    @Override
    public void invoke(PriceTick value, Context context) throws Exception {
        Thread.sleep(10);//10mil sec


       // LOG.info("PriceEvent " + counter.incrementAndGet() + "=="+ value);
    }

}
