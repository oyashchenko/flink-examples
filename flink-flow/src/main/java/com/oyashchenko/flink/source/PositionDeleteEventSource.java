package com.oyashchenko.flink.source;

import com.oyashchenko.flink.Utils;
import com.oyashchenko.flink.model.PositionDeleteEvent;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class PositionDeleteEventSource implements SourceFunction<PositionDeleteEvent> {
    @Override
    public void run(SourceContext<PositionDeleteEvent> sourceContext) throws Exception {
        for (int i = 0; i < 30; i++) {
            int legalEntityId = 2;
            if (i % 2 == 0) {
                legalEntityId = 1;
            }
            Thread.sleep(60000);
            //if (!skip) {
            sourceContext.collect(
                    new PositionDeleteEvent(legalEntityId)
            );
            Thread.sleep(600000);//10 min

        }
    }

    @Override
    public void cancel() {

    }
}
