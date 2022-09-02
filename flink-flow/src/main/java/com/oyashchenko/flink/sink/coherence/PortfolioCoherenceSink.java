package com.oyashchenko.flink.sink.coherence;

import com.oyashchenko.cache.model.Portfolio;
import com.oyashchenko.flink.sink.ignite.IgniteSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PortfolioCoherenceSink extends CoherenceSink<Integer, Portfolio> {
    private static final Logger LOG = LoggerFactory.getLogger(PortfolioCoherenceSink.class);

    public PortfolioCoherenceSink(String cacheName) {
        super(cacheName);
    }

    @Override
    public void invoke(Portfolio value, Context context) {
            long start = System.currentTimeMillis();
            cache.async().put(value.getLegalEntityId(), value);
            LOG.info("Pushed portfolio in {}ms : {} - {} : {} ", System.currentTimeMillis() - start, value.getLegalEntityId(),
                    value.getPnl(), value.getModificationTime());
    }
}
