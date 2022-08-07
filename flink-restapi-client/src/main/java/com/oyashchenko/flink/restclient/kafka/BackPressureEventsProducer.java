package com.oyashchenko.flink.restclient.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class BackPressureEventsProducer implements AutoCloseable {
    private KafkaProducer<String, BackpressureMetric> kafkaProducer;
    public BackPressureEventsProducer(Properties properties) {
        Properties kafkaProp = new Properties();
        kafkaProp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("kafka.servers"));
        kafkaProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BackPressureMetricsSerializer.class);
        kafkaProducer = new KafkaProducer(kafkaProp);
    }

    public void publish(String topic, BackpressureMetric metric) {
        kafkaProducer.send(new ProducerRecord<>(topic, metric));
        kafkaProducer.flush();
    }


    @Override
    public void close() throws Exception {
        if (kafkaProducer != null) {
            kafkaProducer.close();
        }
    }
}
