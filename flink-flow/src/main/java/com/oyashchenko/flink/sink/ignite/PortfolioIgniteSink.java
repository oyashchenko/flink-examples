package com.oyashchenko.flink.sink.ignite;

import com.oyashchenko.flink.model.Portfolio;


public class PortfolioIgniteSink extends IgniteSink<Integer, Portfolio> {

    public PortfolioIgniteSink(String cacheName, String igniteCfgFile) {
        super(cacheName, igniteCfgFile);
    }

    @Override
    public void invoke(Portfolio value, Context context) {
        this.igniteCache.put(value.getLegalEntityId(), value);
    }
}
