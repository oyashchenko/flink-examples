package com.oyashchenko.flink.operations;

import com.oyashchenko.flink.model.Portfolio;
import com.oyashchenko.flink.model.Position;
import com.oyashchenko.flink.model.PositionDeleteEvent;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public class PortfolioPositionsPriceJoin extends KeyedCoProcessFunction<Integer,Portfolio, Position, Portfolio> {
    private static final Logger LOG = LoggerFactory.getLogger(PortfolioPositionsPriceJoin.class);
    private MapState<Integer,Position> securityPositions;
    private ValueState<Portfolio> portfolioState;

    @Override
    public void processElement1(Portfolio portfolio, KeyedCoProcessFunction<Integer, Portfolio, Position, Portfolio>.Context context, Collector<Portfolio> collector) throws Exception {
        if (portfolio == null) {
            if (securityPositions != null && !securityPositions.isEmpty()) {
                Portfolio portfolio1 = new Portfolio(securityPositions.iterator().next().getKey());
                portfolioState.update(portfolio1);
                calculatePortfolioPnl();
                collector.collect(portfolio1);
            }

        } else {
            LOG.info("Portfolio Join : {}", portfolio.getLegalEntityId());
            portfolioState.update(portfolio);
            calculatePortfolioPnl();

        }
        portfolio.updateModificationDate();
        collector.collect(portfolio);
    }

    @Override
    public void processElement2(Position position, KeyedCoProcessFunction<Integer, Portfolio, Position, Portfolio>.Context context, Collector<Portfolio> collector) throws Exception {
        Portfolio portfolio = portfolioState.value();
        if (position.isDeleted()) {
            Position stored = securityPositions.get(position.getSecId());
            if (stored != null) {
                portfolio.removeGmv(stored.getQuantity());
                portfolio.removePnl(stored.getPnl());
            }
            securityPositions.remove(position.getSecId());
        } else {
            Position old = securityPositions.get(position.getSecId());
            if (old != null) {
                portfolio.removePnl(old.getPnl());
                portfolio.removeGmv(old.getQuantity());
            }
            securityPositions.put(position.getSecId(), position);

            portfolio.addPnl(position.getPnl());
            portfolio.gmv(position.getQuantity());
        }
        portfolio.updateModificationDate();
        collector.collect(portfolio);

    }

    private void calculatePortfolioPnl() throws Exception {
        Portfolio portfolio = portfolioState.value();

        securityPositions.values().forEach(
                position -> {
                    if (!position.isDeleted()) {
                        portfolio.addPnl(position.getPnl());
                        portfolio.gmv(position.getQuantity());
                    } else {
                        try {
                            Position stored = securityPositions.get(position.getSecId());

                            portfolio.removeGmv(stored.getQuantity());
                            portfolio.removePnl(stored.getPnl());
                            securityPositions.remove(position.getSecId());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }


        );
        if (portfolio != null) {
            LOG.info("Portfolio is not null");
            portfolio.updateModificationDate();
            portfolioState.update(portfolio);
        }
    }

    @Override
    public void open(Configuration parameters) {
        portfolioState = this.getRuntimeContext().getState(this.getValueDescriptor());
        securityPositions = this.getRuntimeContext().getMapState(this.getPositionsDescriptor());
    }

    private ValueStateDescriptor<Portfolio> getValueDescriptor() {
        return new ValueStateDescriptor<>("portfolio", Portfolio.class);
    }

    private MapStateDescriptor<Integer,Position> getPositionsDescriptor() {
        return new MapStateDescriptor<>("leListPositions", Integer.class, Position.class);
    }
}