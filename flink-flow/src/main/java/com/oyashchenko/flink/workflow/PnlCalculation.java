package com.oyashchenko.flink.workflow;

import com.oyashchenko.flink.Utils;
import com.oyashchenko.flink.model.Position;
import com.oyashchenko.flink.sink.PriceSink;
import com.oyashchenko.flink.model.PriceTick;
import com.oyashchenko.flink.sink.PositionSink;
import com.oyashchenko.flink.source.PriceSource;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.configuration.Configuration;

import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PnlCalculation {
    public static void main(String[] args) throws Exception {
        Logger logger = LoggerFactory.getLogger(PnlCalculation.class.getName());
        // set up the execution environment
        Map<String, String> localSettings = new HashMap();
        localSettings.put("metrics.reporter.myReporter.factory.class", "org.apache.flink.metrics.own.LocalStorageReporterFactory");
        localSettings.put("metrics.reporter.myReporter.interval","10 SECONDS");

        //localSettings.put("metrics.reporter.slf.factory.class", "org.apache.flink.metrics.slf4j.Slf4jReporterFactory");
        //localSettings.put("metrics.reporter.slf.interval","20 SECONDS");

        Configuration configuration = Configuration.fromMap(localSettings);
        //configuration.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true);
       // localSettings.put(ConfigConstants.LOCAL_START_WEBSERVER, "true");
        //localSettings.put("rest.port", "8082");
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(
                configuration
        );




        env.setParallelism(3);
        DataStreamSource<Position> positionDataSource = env.fromCollection(Utils.generatePosition());
        DataStreamSource<PriceTick> priceTickDataSource = env.addSource(new PriceSource(),"Price ticks");
        PriceSink priceCache = new PriceSink();
        positionDataSource.flatMap(
                //taskmanager.495f4c43-82da-4bc5-86ba-a7f18291efd5.Backpressure .Source: Price ticks.0.isBackPressured: true
                new RichFlatMapFunction<Position, String>() {
                    @Override
                    public void open(Configuration parameters) throws Exception {
                        //TaskIOMetricGroup groups = new TaskIOMetricGroup()
                    }
                    @Override
                    public void flatMap(Position position, Collector<String> collector) throws Exception {
                        Map<String, String> allVariables = getRuntimeContext().getMetricGroup().getIOMetricGroup().getAllVariables();

                        allVariables.entrySet().stream().forEach(
                                item -> {
                                    collector.collect(item.getKey());
                                    System.out.println("Key:" + item.getKey() + " Value:" +item.getValue() );
                                }
                        );

                    }
                }
        );

        SingleOutputStreamOperator<PriceTick> priceCountEventsStream  = priceTickDataSource.flatMap(new RichFlatMapFunction<PriceTick, PriceTick>() {
            private final Logger LOG = LoggerFactory.getLogger(PnlCalculation.class.getName());
            private static final long PER_TIME = 1; //1
            private AtomicLong counter = new AtomicLong(1);
            private Long startTime = System.currentTimeMillis();
            private volatile long eventsSnap = startTime;
            private  AtomicLong eventsPerSec = new AtomicLong(0);

            @Override
            public void flatMap(PriceTick priceTick, Collector<PriceTick> collector) throws Exception {


                long current = System.currentTimeMillis();
                long diff = current - eventsSnap;
                long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diff);
                synchronized (counter) {
                if (eventsPerSec.get() <= 60) {
                    collector.collect(priceTick);
                    LOG.info("Added price tick : {} :{} counter : {}", priceTick.getRic(), eventsPerSec.get(), counter.get() );
                    counter.incrementAndGet();
                } else {
                    LOG.info("Ignore price tick :", priceTick.getRic());
                    counter.decrementAndGet();
                }}
                synchronized (counter) {
                    if (diffInSec >= PER_TIME) {
                        eventsPerSec.getAndSet(counter.get() / diffInSec);
                        LOG.info("EVENTS:Current event time : {}, count: {} , Diff time in sec : {} , events per sec: {}",
                                current, counter.get(), diffInSec, eventsPerSec);
                        counter.getAndSet(0);

                        eventsSnap = current;
                        LOG.info("Updated counter : {}, eventSnap : {}", counter.get(), eventsSnap);
                    } else {

                    }
                }

            }
        }).name("Price logic").setParallelism(2);

        priceCountEventsStream.addSink(priceCache).name("PriceSink").setParallelism(3);



        /*ConnectedStreams<DataSource<Position>, DataSource<PriceTick>>> join = new ConnectedStreams(env, positionDataSource, priceTickDataSource);

        positionDataSource.hashCode
                (priceTickDataSource) .
                .where(pos -> pos.getSecId() = p)

        )*/


        PositionSink positionSink = new PositionSink();
        logger.info("Started task");
        positionDataSource.addSink(positionSink).name("PositionSink");
       // priceTickDataSource.addSink(priceCache).name("PriceSink");
        JobExecutionResult jobResults = env.execute("Backpressure ");

    }
}
