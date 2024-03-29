package com.oyashchenko.flink.operations;

import com.oyashchenko.cache.model.PriceTick;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CalculatePriceVolumeFlatMap extends RichFlatMapFunction<PriceTick, PriceTick> {
        private final Logger LOG = LoggerFactory.getLogger(CalculatePriceVolumeFlatMap.class.getName());
        private static final long PER_TIME = 1; //1
        private final AtomicLong counter = new AtomicLong(1);
        private final Long startTime = System.currentTimeMillis();
        private volatile long eventsSnap = startTime;
        private  final AtomicLong eventsPerSec = new AtomicLong(0);

        private final MapStateDescriptor descriptor = BackPressureMetricsStoreFunction.getMetricsState();

        @Override
        public void flatMap(PriceTick priceTick, Collector<PriceTick> collector) {

            long current = System.currentTimeMillis();
            long diff = current - eventsSnap;
            long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diff);
            synchronized (counter) {
               // if (eventsPerSec.get() <= 60) {
                collector.collect(priceTick);
                //LOG.info("Added price tick : {} :{} counter : {}", priceTick.getRic(), eventsPerSec.get(), counter.get() );
                counter.incrementAndGet();
                //} else {
                ///    LOG.info("Ignore price tick :", priceTick.getRic());
                //    counter.decrementAndGet();
                //}
            }
            synchronized (counter) {
                if (diffInSec >= PER_TIME) {
                    eventsPerSec.getAndSet(counter.get() / diffInSec);
                    //LOG.info("EVENTS:Current event time : {}, count: {} , Diff time in sec : {} , events per sec: {}",
                    //        current, counter.get(), diffInSec, eventsPerSec);
                    counter.getAndSet(0);

                    eventsSnap = current;
                  //  LOG.info("Updated counter : {}, eventSnap : {}", counter.get(), eventsSnap);
                }
            }

        }
}
