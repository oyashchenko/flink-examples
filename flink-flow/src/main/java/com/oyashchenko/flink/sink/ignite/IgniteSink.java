package com.oyashchenko.flink.sink.ignite;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.ignite.*;
import org.apache.ignite.cache.CacheExistsException;

import java.util.Map;

public class IgniteSink<KEY, IN>  extends RichSinkFunction<IN> {
    private final String igniteCfgFile;
    private final String cacheName;
    protected IgniteCache<KEY, IN> igniteCache;

    public IgniteSink(String cacheName, String igniteCfgFile) {
        this.cacheName = cacheName;
        this.igniteCfgFile = igniteCfgFile;
    }

    protected transient Ignite ignite;
    @Override
    public void open(Configuration parameters) {
        try {
            this.ignite = Ignition.start(igniteCfgFile);
        }
        catch (IgniteException e) {
            if (e.getMessage().contains("instance has already been started.")) {
                // ignite instance is already started in same JVM then use it
                try {
                    this.ignite = Ignition.ignite();
                }
                catch (IgniteIllegalStateException illegalStateException) {
                    throw new IgniteException("Cannot connect to existing ignite instance", e);
                }
            }
            else {
                throw e;
            }
        }

        this.igniteCache = this.ignite.getOrCreateCache(cacheName);

    }

    @Override
    public void close() {
        if (this.ignite != null) {
            if (igniteCache != null) {
                igniteCache.destroy();
            }
            ignite.close();

        }
    }


}
