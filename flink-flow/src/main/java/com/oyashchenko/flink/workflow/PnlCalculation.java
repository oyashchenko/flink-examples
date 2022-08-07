package com.oyashchenko.flink.workflow;

import com.oyashchenko.flink.Utils;
import com.oyashchenko.flink.model.BackpressureMetric;
import com.oyashchenko.flink.model.Position;
import com.oyashchenko.flink.operstions.CalculatePriceVolumeFlatMap;
import com.oyashchenko.flink.operstions.BroadcastPriceBackPressureMetricsProcessFunction;
import com.oyashchenko.flink.sink.PriceSink;
import com.oyashchenko.flink.model.PriceTick;
import com.oyashchenko.flink.sink.PositionSink;
import com.oyashchenko.flink.source.BackPressureMetricsStoreFunction;
import com.oyashchenko.flink.source.BackpressureMetricsDeserializer;
import com.oyashchenko.flink.source.PriceSource;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;
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
        DataStreamSource<Position> positionDataSource = env.fromCollection(Utils.generatePosition());
        DataStreamSource<PriceTick> priceTickDataSource = env.addSource(new PriceSource(),"Price ticks");
        DataStreamSource<BackpressureMetric> backpressureMetricsSource = env.fromSource(metricsSource(), WatermarkStrategy.noWatermarks(), "Metrics");

        MapStateDescriptor<String, BackpressureMetric> stateOfBackPressure = BackPressureMetricsStoreFunction.getMetricsState();

        BroadcastStream<BackpressureMetric> backPressureStreamMetrics = backpressureMetricsSource
                //.keyBy(backpressureMetricAvro -> backpressureMetricAvro.getTaskName()).
          .broadcast(stateOfBackPressure);



        //flatMap(new BackPressureMetricsStoreFunction()).print().name("Metrics Results");


        PriceSink priceCache = new PriceSink();
        positionDataSource.flatMap(
                new RichFlatMapFunction<Position, String>() {
                    @Override
                    public void open(Configuration parameters) throws Exception {

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

        SingleOutputStreamOperator<PriceTick> priceCountEventsStream  = priceTickDataSource.
        keyBy(priceTick -> priceTick.getSecId())
            .flatMap(new CalculatePriceVolumeFlatMap()).name("Price logic").setParallelism(2);


        SingleOutputStreamOperator<PriceTick> price_throttle = priceCountEventsStream.connect(backPressureStreamMetrics)
                .process(new BroadcastPriceBackPressureMetricsProcessFunction()).name("Price Throttle").setParallelism(3);


        price_throttle.addSink(priceCache).name("PriceSink").setParallelism(3);






        PositionSink positionSink = new PositionSink();
        logger.info("Started task");
        positionDataSource.addSink(positionSink).name("PositionSink");
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
