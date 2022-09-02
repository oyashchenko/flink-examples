package com.oyashchenko.flink.operations;

import com.oyashchenko.cache.model.Position;
import com.oyashchenko.flink.model.PositionDeleteEvent;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

public class PositionDeleteProcessFunction extends CoProcessFunction<Position, PositionDeleteEvent, Position> {
    private static final Logger LOG = LoggerFactory.getLogger(PositionDeleteProcessFunction.class);
    private ValueState<PositionDeleteEvent> deletedEvent;

    private ValueState<Long> timeEvent;

    private MapState<Integer,Position> positionsState;

    @Override
    public void processElement1(Position position, CoProcessFunction<Position, PositionDeleteEvent, Position>.Context context, Collector<Position> collector) throws Exception {
        PositionDeleteEvent deleteEvent = deletedEvent.value();
        if (deleteEvent != null && position.getLegalEntityId().equals(deleteEvent.getLegalEntityId())
           && position.getEventTime().isBefore(deleteEvent.getEventTime())
        ) {
            position.setDeleted(true);
            LOG.info("POSITION FOR REFRESH:{}{}", position.getLegalEntityId(), position.getSecId());
            //need delete positions from state
           // positionsState.remove(position.getSecId());

        } else {
            LOG.info("DELETE POSITION EVENT:{}, Time :{}",deleteEvent != null,
               deleteEvent == null || position.getEventTime().isBefore(deleteEvent.getEventTime()));
            positionsState.put(position.getSecId(), position);
        }


        collector.collect(position);
    }

    @Override
    public void processElement2(PositionDeleteEvent positionDeleteEvent, CoProcessFunction<Position, PositionDeleteEvent, Position>.Context context, Collector<Position> collector) throws Exception {
        deletedEvent.update(positionDeleteEvent);
        LOG.info("DELETE EVENT : {}", deletedEvent.value().toString());
        Iterator<Map.Entry<Integer,Position>> iterator = positionsState.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Position> positionEntry = iterator.next();
            Position position = positionEntry.getValue();
            LOG.info("DELETE EVENT time {}-{}", position.getEventTime(), positionDeleteEvent.getEventTime());
            if (position.getEventTime().isBefore(positionDeleteEvent.getEventTime())) {
                position.setDeleted(true);
                iterator.remove();
            }

            collector.collect(position);

        }
    }

    @Override
    public void open(Configuration parameters) {
        deletedEvent = this.getRuntimeContext().getState(this.getValueDescriptor());
        positionsState = this.getRuntimeContext().getMapState(this.getPositionsDescriptor());
    }

    private ValueStateDescriptor<PositionDeleteEvent> getValueDescriptor() {
        return new ValueStateDescriptor<PositionDeleteEvent>("deletedLe", PositionDeleteEvent.class);
    }

    private MapStateDescriptor<Integer,Position> getPositionsDescriptor() {
        return new MapStateDescriptor<Integer,Position>("lePositions", Integer.class, Position.class);
    }
}
