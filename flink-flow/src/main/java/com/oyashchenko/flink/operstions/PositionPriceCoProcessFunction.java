package com.oyashchenko.flink.operstions;

import com.oyashchenko.flink.model.Position;
import com.oyashchenko.flink.model.PriceTick;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PositionPriceCoProcessFunction extends CoProcessFunction<PriceTick, Position, Position> {
    private static final Logger LOG = LoggerFactory.getLogger(PositionPriceCoProcessFunction.class);
    private MapState<Integer, List<Position>> securityPositions;
    private ValueState<Tuple2<LocalDateTime, Double>> latestSecurityPrice;

    @Override
    public void processElement1(PriceTick priceTick, CoProcessFunction<PriceTick, Position, Position>.Context context, Collector<Position> collector) throws Exception {
        Tuple2<LocalDateTime, Double> current = latestSecurityPrice.value();


        if (current == null ||
                ((LocalDateTime)current.getField(0)).compareTo(priceTick.getEventTime()) < 0) {
            latestSecurityPrice.update(new Tuple2<>(priceTick.getEventTime(), priceTick.getPrice()));
            securityPositions.values().forEach(
                    lePositions -> lePositions.forEach(
                            pos -> {
                                if (!pos.isDeleted()) {
                                    pos.setPrice(priceTick.getPrice());
                                    collector.collect(pos);
                                } else {
                                   LOG.info("REMOVED POSITION: {}-{}, ignore price tick: {} ", pos.getLegalEntityId(), pos.getSecId(), priceTick );
                               }
                            }

                    )
            );
        } else {

            //nothing to do
        }
    }

@Override
    public void processElement2(Position position, CoProcessFunction<PriceTick, Position, Position>.Context context, Collector<Position> collector) throws Exception {
        if (!position.isDeleted()) {
            position.setPrice(latestSecurityPrice.value() == null ? 0d : latestSecurityPrice.value().getField(1));
            Integer secId = position.getSecId();
            List<Position> positions = securityPositions.get(secId);
            if (positions == null ) {
                List positionsSec = new ArrayList();
                positionsSec.add(position);
                securityPositions.put(secId, positionsSec);
            } else {
                Optional<Position> find = positions.stream().filter(
                        pos -> pos.equals(position)
                ).findFirst();

                if (find.isPresent()) {
                    find.get().setPrice(position.getPrice());
                    LOG.info("Updated price for {}-{}, price - {}", position.getLegalEntityId(), position.getSecId(), position.getPrice());
                } else {
                    positions.add(position);
                }
            }


            collector.collect(position);
        } else {
            LOG.info("Position for remove in PositionPriceCoProcess: {}-{}"
            , position.getLegalEntityId(), position.getSecId());
            List<Position> positions = securityPositions.get(position.getSecId());
            if (positions != null) {
                LOG.info("Positions size: {}, Diff le number {}",positions.size(), positions.stream().map(
                        position1 -> position1.getLegalEntityId()).count());
                positions.remove(position);
                LOG.info("Positions size after: {}, Diff le number {}",positions.size(), positions.stream().map(
                        position1 -> position1.getLegalEntityId()).count());
            }
        }
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        latestSecurityPrice = this.getRuntimeContext().getState(this.getValueDescriptor());
        securityPositions = this.getRuntimeContext().getMapState(this.getPositionsDescriptor());
    }

    private ValueStateDescriptor<Tuple2<LocalDateTime, Double>> getValueDescriptor() {
        return new ValueStateDescriptor<Tuple2<LocalDateTime, Double>>("latestSecPrice",
            TypeInformation.of(new TypeHint<Tuple2<LocalDateTime, Double>>() {}));
        }

        private MapStateDescriptor<Integer,List<Position>> getPositionsDescriptor() {
            return new MapStateDescriptor<Integer, List<Position>>("leSecPositions", TypeInformation.of(Integer.class), TypeInformation.of(new TypeHint<List<Position>>() {}));
        }

}
