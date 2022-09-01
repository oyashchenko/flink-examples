package com.oyashchenko.flink.workflow;

import com.oyashchenko.cache.model.*;
import com.oyashchenko.flink.model.BackpressureMetric;
import com.oyashchenko.flink.model.PositionDeleteEvent;
import com.oyashchenko.flink.operations.*;
import com.oyashchenko.flink.sink.SinkFactory;
import com.oyashchenko.flink.source.*;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.*;
import org.apache.flink.configuration.Configuration;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.evictors.Evictor;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.streaming.runtime.operators.windowing.TimestampedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PnlCalculation {
    public static void main(String[] args) throws Exception {
        Logger logger = LoggerFactory.getLogger(PnlCalculation.class.getName());
        // set up the execution environment
        Map<String, String> localSettings = new HashMap<String, String>();
      //  localSettings.put("metrics.reporter.myReporter.factory.class", "org.apache.flink.metrics.own.LocalStorageReporterFactory");
      //  localSettings.put("metrics.reporter.myReporter.interval","10 SECONDS");

        //localSettings.put("metrics.reporter.slf.factory.class", "org.apache.flink.metrics.slf4j.Slf4jReporterFactory");
        //localSettings.put("metrics.reporter.slf.interval","20 SECONDS");

        Configuration configuration = Configuration.fromMap(localSettings);

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(
                configuration
        );

        env.setParallelism(3);
        //Data sources
        DataStreamSource<Position> positionDataSource = env.addSource(new PositionSource(),"Position Source");
        DataStreamSource<PriceTick> priceTickDataSource = env.addSource(new PriceSource(),"Price ticks");
        DataStreamSource<BackpressureMetric> backpressureMetricsSource = env.fromSource(metricsSource(), WatermarkStrategy.noWatermarks(), "Metrics").setParallelism(1);
        DataStreamSource<PositionDeleteEvent> positionDeleteSource = env.addSource(new PositionDeleteEventSource(), "Delete Position");
        DataStreamSource<Portfolio> portfolioDataSource = env.fromCollection(
            Arrays.asList(new Portfolio(1), new Portfolio(2))
        );


        MapStateDescriptor<String, BackpressureMetric> stateOfBackPressure = BackPressureMetricsStoreFunction.getMetricsState();
        BroadcastStream<BackpressureMetric> backPressureStreamMetrics = backpressureMetricsSource
            //.keyBy(backpressureMetricAvro -> backpressureMetricAvro.getTaskName()).
            .broadcast(stateOfBackPressure);

        //Sinks
        RichSinkFunction<PriceTick> priceCache = SinkFactory.getSink(PriceTick.class);
        RichSinkFunction<Position> positionSink = SinkFactory.getSink(Position.class);
        RichSinkFunction<Portfolio> portfolioSink = SinkFactory.getSink(Portfolio.class);

        SingleOutputStreamOperator<PriceTick> priceCountEventsStream  = priceTickDataSource
            .keyBy(PriceTick::getSecId)
            .flatMap(new CalculatePriceVolumeFlatMap()).name("Price logic").setParallelism(3);

        SingleOutputStreamOperator<PriceTick> priceThrottle = priceCountEventsStream.connect(backPressureStreamMetrics)
                .process(new BroadcastPriceBackPressureMetricsProcessFunction()).name("Price Throttle").setParallelism(3);


        priceThrottle.keyBy(PriceTick::getSecId).addSink(priceCache).name("PriceSink").setParallelism(3);

        SingleOutputStreamOperator<Position> positionsWithDeleteEventJoin = positionDataSource
            .keyBy(Position::getLegalEntityId)
            .connect(positionDeleteSource)
                .keyBy(Position::getLegalEntityId, PositionDeleteEvent::getLegalEntityId)
                .process(new PositionDeleteProcessFunction()).name("PositionDeleteUpdateFlag");


        SingleOutputStreamOperator<Position> processPositionPriceJoin =
            priceThrottle.keyBy(PriceTick::getSecId)
                .connect(positionsWithDeleteEventJoin)
                        .keyBy(PriceTick::getSecId, Position::getSecId)
                        .process(new PositionPriceCoProcessFunction()).name("PositionPriceJoin");

        //join positions and portfolio
        SingleOutputStreamOperator<Portfolio> portfolioPositionJoin = portfolioDataSource
                .keyBy(Portfolio::getLegalEntityId).connect(processPositionPriceJoin).keyBy(
                        Portfolio::getLegalEntityId, Position::getLegalEntityId
        ).process(new PortfolioPositionsPriceJoin()).name("PortfolioPositionJoin").setParallelism(4);

        WindowedStream<Position, Integer, GlobalWindow> portfolioAllWindow = processPositionPriceJoin.keyBy(Position::getLegalEntityId)
                .window(GlobalWindows.create());

        /*portfolioAllWindow
                .trigger(
                        new Trigger<Position, GlobalWindow>() {
                            @Override
                            public TriggerResult onElement(Position position, long l, GlobalWindow globalWindow, TriggerContext triggerContext) throws Exception {
                                return TriggerResult.FIRE;
                            }

                            @Override
                            public TriggerResult onProcessingTime(long l, GlobalWindow globalWindow, TriggerContext triggerContext) throws Exception {
                                return TriggerResult.CONTINUE;
                            }

                            @Override
                            public TriggerResult onEventTime(long l, GlobalWindow globalWindow, TriggerContext triggerContext) throws Exception {
                                return TriggerResult.CONTINUE;
                            }

                            @Override
                            public void clear(GlobalWindow globalWindow, TriggerContext triggerContext) throws Exception {

                            }
                        }
                ).evictor(
                        new Evictor<Position, GlobalWindow>() {
                            @Override
                            public void evictBefore(Iterable<TimestampedValue<Position>> iterable, int i, GlobalWindow globalWindow, EvictorContext evictorContext) {
                                for (Iterator<TimestampedValue<Position>> iterator = iterable.iterator(); iterator.hasNext(); ) {
                                    TimestampedValue<Position> record = iterator.next();
                                    if (record.getValue().isDeleted()) {
                                    //    iterator.remove();

                                        System.out.println("REMOVED from Trigger" + record.getValue());
                                    }
                                }

                            }

                            @Override
                            public void evictAfter(Iterable<TimestampedValue<Position>> iterable, int i, GlobalWindow globalWindow, EvictorContext evictorContext) {

                            }
                        }
                )
                .aggregate( new PortfolioPositionAggregationWindowsFunction())
                .name("PortfolioAllWindow");*/
                //.addSink(portfolioSink).name("PortfolioSink");





        logger.info("Started task");
        processPositionPriceJoin.addSink(positionSink).name("PositionSink");
        portfolioPositionJoin.addSink(portfolioSink).name("PortfolioSink");
       // priceTickDataSource.addSink(priceCache).name("PriceSink");
        JobExecutionResult jobResults = env.execute("Backpressure ");

    }

    private static KafkaSource<BackpressureMetric> metricsSource() {
        return KafkaSource.<BackpressureMetric>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics("backPressureEventTopic")
                .setGroupId("my-group")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(
                        BackpressureMetricsDeserializer.class))
                .setStartingOffsets(OffsetsInitializer.latest())
                .build();
    }
}
