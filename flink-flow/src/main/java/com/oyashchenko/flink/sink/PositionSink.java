package com.oyashchenko.flink.sink;

import com.oyashchenko.cache.model.Position;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionSink extends RichSinkFunction<Position>  {

    private static final Logger LOG = LoggerFactory.getLogger(PositionSink.class);


    @Override
    public void invoke(Position value, Context context) {
        //Thread.sleep(10000);//10sec
        System.out.println("Position Sink:" + value);

    }
}
