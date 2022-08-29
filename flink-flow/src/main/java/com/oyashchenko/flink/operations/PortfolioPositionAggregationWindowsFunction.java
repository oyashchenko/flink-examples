package com.oyashchenko.flink.operations;

import com.oyashchenko.flink.model.Portfolio;
import com.oyashchenko.flink.model.Position;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.RichAggregateFunction;

public class PortfolioPositionAggregationWindowsFunction implements AggregateFunction<Position, Portfolio, Portfolio> {
    @Override
    public Portfolio createAccumulator() {
        return new Portfolio();
    }

    @Override
    public Portfolio add(Position position, Portfolio portfolio) {
        if (portfolio.getLegalEntityId() == null) {
            portfolio = new Portfolio(position.getLegalEntityId());

        }
        if (!position.isDeleted()) {
            portfolio.gmv(position.getQuantity());
            portfolio.addPnl(position.getPnl());
            portfolio.updateModificationDate();
        } else {
            portfolio.removeGmv(position.getQuantity());
            portfolio.removePnl(position.getPnl());

        }

        return portfolio;
    }

    @Override
    public Portfolio getResult(Portfolio portfolio) {
        return portfolio;
    }

    @Override
    public Portfolio merge(Portfolio portfolio, Portfolio acc1) {
        System.out.println(portfolio + "Portf" + acc1);
        return  portfolio.getModificationTime().isAfter(acc1.getModificationTime()) ? portfolio : acc1;
    }
}
