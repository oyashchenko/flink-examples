package com.oyashchenko.flink.sink;

import com.oyashchenko.flink.model.PriceTick;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class PriceSink implements SinkFunction<PriceTick> {
    private static final Logger LOG = LoggerFactory.getLogger(PriceSink.class);
    private static final AtomicLong counter = new AtomicLong();

    @Override
    public void invoke(PriceTick value, Context context) throws Exception {
        Thread.sleep(10);//10mil sec

       // System.out.println("Price tick : " +         context.currentProcessingTime() + ": " +value );

        LOG.info("PriceEvent " + counter.incrementAndGet() + "=="+ value);
    }

}
