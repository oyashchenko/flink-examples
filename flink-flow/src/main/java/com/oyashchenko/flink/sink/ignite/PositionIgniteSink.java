package com.oyashchenko.flink.sink.ignite;

import com.oyashchenko.cache.model.Position;

public class PositionIgniteSink extends IgniteSink<String, Position>  {

    public PositionIgniteSink(String cacheName, String igniteCfgFile) {
        super(cacheName, igniteCfgFile);
    }

    @Override
    public void invoke(Position value, Context context) {
        if (value.isDeleted()) {

            //this.igniteCache.remove(value.getKey());
        }
        this.igniteCache.put(value.getKey(), value);
    }
}
