package com.oyashchenko.flink.source;

import com.oyashchenko.flink.Utils;

import com.oyashchenko.flink.model.Position;
import com.oyashchenko.flink.model.PriceTick;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class PositionSource implements SourceFunction<Position> {

    @Override
    public void run(SourceContext<Position> sourceContext) throws Exception {
        for (int i = 0 ; i < 30000000 ; i++) {
            Integer secId = Utils.generateSecId();
            int legalEntityId = 1;
            if (i%2==0) {
                legalEntityId =2;
            }
            //if (!skip) {
            sourceContext.collect(
                new Position(secId,legalEntityId,secId * 10d, "USD", 1d)
            );
            Thread.sleep(5);
        }
    }

    @Override
    public void cancel() {

    }
}




