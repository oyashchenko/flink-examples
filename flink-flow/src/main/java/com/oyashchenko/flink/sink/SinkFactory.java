package com.oyashchenko.flink.sink;

import com.oyashchenko.flink.model.Portfolio;
import com.oyashchenko.flink.model.Position;
import com.oyashchenko.flink.model.PriceTick;
import com.oyashchenko.flink.sink.ignite.PortfolioIgniteSink;
import com.oyashchenko.flink.sink.ignite.PositionIgniteSink;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class SinkFactory {

    private SinkFactory(){}

    private static final boolean IS_IGNITE = true;

    public static <T> RichSinkFunction getSink(Class<T> clazz) {
        if (clazz.isAssignableFrom(Position.class)) {
            return IS_IGNITE ?  new PositionIgniteSink("Position","ignite-cache.xml") : new PositionSink();
        } else if (clazz.isAssignableFrom(PriceTick.class)) {
            return new PriceSink();
        } else if (clazz.isAssignableFrom(Portfolio.class)) {
            return new PortfolioIgniteSink("Portfolio", "ignite-cache.xml");


        }
        throw  new UnsupportedOperationException("Unsupported class type.");

    }

}
