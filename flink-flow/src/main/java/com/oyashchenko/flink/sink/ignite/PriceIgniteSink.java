package com.oyashchenko.flink.sink.ignite;

import com.oyashchenko.flink.model.PriceTick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceIgniteSink extends IgniteSink<Integer, PriceTick> {
    private static final Logger LOG = LoggerFactory.getLogger(PriceIgniteSink.class);

    public PriceIgniteSink(String cacheName, String igniteCfgFile) {
        super(cacheName, igniteCfgFile);
    }

    @Override
    public void invoke(PriceTick value, Context context) throws Exception {
        long start = System.currentTimeMillis();
        LOG.info("Put price : {} before", value.getRic());
        igniteCache.putAsync(value.getSecId(), value);
        LOG.info("Put price : {} after. Spend time in ms: {}", value.getRic(), System.currentTimeMillis() - start);
    }
}
