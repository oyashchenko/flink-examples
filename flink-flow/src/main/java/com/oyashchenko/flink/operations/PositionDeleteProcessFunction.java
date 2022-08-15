package com.oyashchenko.flink.operations;

import com.oyashchenko.flink.model.Position;
import com.oyashchenko.flink.model.PositionDeleteEvent;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionDeleteProcessFunction extends CoProcessFunction<Position, PositionDeleteEvent, Position> {
    private static final Logger LOG = LoggerFactory.getLogger(PositionDeleteProcessFunction.class);
    private ValueState<PositionDeleteEvent> deletedEvent;

    private ValueState<Long> timeEvent;
    ListState<Position> positionListState;

    @Override
    public void processElement1(Position position, CoProcessFunction<Position, PositionDeleteEvent, Position>.Context context, Collector<Position> collector) throws Exception {
        PositionDeleteEvent deleteEvent = deletedEvent.value();
        if (deleteEvent != null && position.getLegalEntityId().equals(deleteEvent.getLegalEntityId())
           && position.getEventTime().isBefore(deleteEvent.getEventTime())
        ) {
            position.setDeleted(true);
            LOG.info("POSITION FOR REFRESH:{}{}", position.getLegalEntityId(), position.getSecId());
            //need delete positions from state

        } else {
            LOG.info("DELETE POSITION EVENT:{}, Time :{}",deleteEvent != null,
                    deleteEvent != null ?position.getEventTime().isBefore(deleteEvent.getEventTime()) : true);
        }

        positionListState.add(position);
        collector.collect(position);
    }

    @Override
    public void processElement2(PositionDeleteEvent positionDeleteEvent, CoProcessFunction<Position, PositionDeleteEvent, Position>.Context context, Collector<Position> collector) throws Exception {
        deletedEvent.update(positionDeleteEvent);
        LOG.info("DELETE EVENT : {}", deletedEvent.value().toString());
        positionListState.get().forEach(
                position -> {
                    LOG.info("DELETE EVENT time {}-{}",position.getEventTime(), positionDeleteEvent.getEventTime());
                    position.setDeleted(true);
                    collector.collect(position);

                }
        );
        positionListState.clear();
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        deletedEvent = this.getRuntimeContext().getState(this.getValueDescriptor());
        positionListState = this.getRuntimeContext().getListState(this.getPositionsDescriptor());
    }

    private ValueStateDescriptor<PositionDeleteEvent> getValueDescriptor() {
        return new ValueStateDescriptor<PositionDeleteEvent>("deletedLe", PositionDeleteEvent.class);
    }

    private ListStateDescriptor<Position> getPositionsDescriptor() {
        return new ListStateDescriptor<Position>("listPositions", Position.class);
    }
}
