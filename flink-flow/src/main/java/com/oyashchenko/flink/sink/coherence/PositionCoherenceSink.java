package com.oyashchenko.flink.sink.coherence;

import com.oyashchenko.cache.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PositionCoherenceSink extends CoherenceSink<String, Position> {
    private static final Logger LOG = LoggerFactory.getLogger(PositionCoherenceSink.class);

    public PositionCoherenceSink(String cacheName) {
        super(cacheName);
    }

    @Override
    public void invoke(Position value, Context context) {
        long start = System.currentTimeMillis();
        cache.put(value.getKey(), value);
        LOG.info("Pushed position {} ms: {} ", System.currentTimeMillis() - start, value.getLegalEntityId());
    }

    @Override
    public void close() {
        if (cache != null) {
            cache.close();
        }
    }
}
