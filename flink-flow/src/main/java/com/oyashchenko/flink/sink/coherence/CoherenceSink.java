package com.oyashchenko.flink.sink.coherence;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class CoherenceSink<KEY, IN> extends RichSinkFunction<IN> {
    private final String cacheName;
    protected NamedCache<KEY, IN> cache;
    protected boolean isExtendedProxyRun = false;

    public CoherenceSink(String cacheName) {
        this.cacheName = cacheName;
    }

    public CoherenceSink(String cacheName, boolean isExtendedProxyRun) {
        this.cacheName = cacheName;
        this.isExtendedProxyRun = isExtendedProxyRun;
    }


    @Override
    public void open(Configuration parameters) {
        System.setProperty("coherence.distributed.localstorage", "false");
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("tangosol.coherence.cacheconfig", "cache-config-client.xml");
        CacheFactory.ensureCluster();
        cache = CacheFactory.getCache(cacheName);
    }

    @Override
    public void close() {
        if (cache != null) {
            cache.destroy();
        }
    }

    @Override
    public void invoke(IN value, Context context) throws Exception {
        super.invoke(value, context);
    }
}
