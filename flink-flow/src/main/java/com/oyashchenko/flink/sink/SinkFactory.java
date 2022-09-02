package com.oyashchenko.flink.sink;

import com.oyashchenko.cache.model.Portfolio;
import com.oyashchenko.cache.model.Position;
import com.oyashchenko.cache.model.PriceTick;
import com.oyashchenko.flink.sink.coherence.PortfolioCoherenceSink;
import com.oyashchenko.flink.sink.coherence.PositionCoherenceSink;
import com.oyashchenko.flink.sink.coherence.PriceCoherenceSink;
import com.oyashchenko.flink.sink.ignite.PortfolioIgniteSink;
import com.oyashchenko.flink.sink.ignite.PositionIgniteSink;
import com.oyashchenko.flink.sink.ignite.PriceIgniteSink;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class SinkFactory {

    private SinkFactory(){}

    private static final boolean IS_IGNITE = false;

    public static <T> RichSinkFunction getSink(Class<T> clazz) {
        if (clazz.isAssignableFrom(Position.class)) {
            return IS_IGNITE ?  new PositionIgniteSink("Position", "ignite-cache.xml") : new PositionCoherenceSink("Position");
        } else if (clazz.isAssignableFrom(PriceTick.class)) {
            return IS_IGNITE ? new PriceIgniteSink("Price", "ignite-cache.xml") : new PriceCoherenceSink("Price");
        } else if (clazz.isAssignableFrom(Portfolio.class)) {
            return  IS_IGNITE ? new PortfolioIgniteSink("Portfolio", "ignite-cache.xml") : new PortfolioCoherenceSink("Portfolio");
        }

        throw  new UnsupportedOperationException("Unsupported class type.");
    }

}
