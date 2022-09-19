package com.oyashchenko.flink.sink.coherence;

import com.oyashchenko.cache.model.Portfolio;
import com.oyashchenko.cache.model.PriceTick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PriceCoherenceSink extends CoherenceSink<Integer, PriceTick> {
    private static final Logger LOG = LoggerFactory.getLogger(PriceCoherenceSink.class);

    public PriceCoherenceSink(String cacheName) {
        super(cacheName);
    }

    public PriceCoherenceSink(String cacheName, boolean isExtendedProxyRun) {
        super(cacheName, isExtendedProxyRun);
    }

    @Override
    public void invoke(PriceTick value, Context context) {
        long start = System.currentTimeMillis();
        //cache.put(value.getSecId(), value);
        if (isExtendedProxyRun) {
            cache.put(value.getSecId(), value);
        } else {
            cache.async().put(value.getSecId(), value);
        }
        //LOG.info("Pushed secId : {}, spend time : {} ms, eventTime : {} ", value.getSecId(), System.currentTimeMillis() - start, value.getEventTime());
    }
}
