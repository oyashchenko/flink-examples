package com.oyashchenko.flink.workflow;

import com.oyashchenko.flink.model.BackpressureMetric;
import com.oyashchenko.flink.model.Position;
import com.oyashchenko.flink.model.PositionDeleteEvent;
import com.oyashchenko.flink.operstions.*;
import com.oyashchenko.flink.sink.PriceSink;
import com.oyashchenko.flink.model.PriceTick;
import com.oyashchenko.flink.sink.PositionSink;
import com.oyashchenko.flink.source.*;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.*;
import org.apache.flink.configuration.Configuration;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PnlCalculation {
    public static void main(String[] args) throws Exception {
        Logger logger = LoggerFactory.getLogger(PnlCalculation.class.getName());
        // set up the execution environment
        Map<String, String> localSettings = new HashMap();
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
        DataStreamSource<BackpressureMetric> backpressureMetricsSource = env.fromSource(metricsSource(), WatermarkStrategy.noWatermarks(), "Metrics");
        DataStreamSource<PositionDeleteEvent> positionDeleteSource = env.addSource(new PositionDeleteEventSource(), "Delete Position");

        MapStateDescriptor<String, BackpressureMetric> stateOfBackPressure = BackPressureMetricsStoreFunction.getMetricsState();
        BroadcastStream<BackpressureMetric> backPressureStreamMetrics = backpressureMetricsSource
            //.keyBy(backpressureMetricAvro -> backpressureMetricAvro.getTaskName()).
            .broadcast(stateOfBackPressure);

        //Sinks
        PriceSink priceCache = new PriceSink();
        PositionSink positionSink = new PositionSink();

        SingleOutputStreamOperator<PriceTick> priceCountEventsStream  = priceTickDataSource.
        keyBy(priceTick -> priceTick.getSecId())
            .flatMap(new CalculatePriceVolumeFlatMap()).name("Price logic").setParallelism(3);


        SingleOutputStreamOperator<PriceTick> priceThrottle = priceCountEventsStream.connect(backPressureStreamMetrics)
                .process(new BroadcastPriceBackPressureMetricsProcessFunction()).name("Price Throttle").setParallelism(3);


        priceThrottle.addSink(priceCache).name("PriceSink").setParallelism(4);

        SingleOutputStreamOperator<Position> positionsWithDeleteEventJoin = positionDataSource
            .keyBy(position -> position.getLegalEntityId())
            .connect(positionDeleteSource)
                .keyBy(position -> position.getLegalEntityId(), positionDeleteEvent -> positionDeleteEvent.getLegalEntityId())
                .process(new PositionDeleteProcessFunction()).name("PositionDeleteUpdateFlag");


        SingleOutputStreamOperator<Position> processPositionPriceJoin =
                priceThrottle.keyBy(priceTick -> priceTick.getSecId())
                .connect(positionsWithDeleteEventJoin)
                        .keyBy(sec -> sec.getSecId(), position -> position.getSecId())
                        .process(new PositionPriceCoProcessFunction()).name("PositionPriceJoin");


        logger.info("Started task");
        processPositionPriceJoin.addSink(positionSink).name("PositionSink");
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
