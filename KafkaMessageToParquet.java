import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.sql.*;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.*;

public class KafkaMessageToParquet {

    public static void main(String[] args) throws InterruptedException {
        // Kafka configurations
        Map<String, Object> kafkaConsumerParams = new HashMap<>();
        kafkaConsumerParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaConsumerParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaConsumerParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaConsumerParams.put(ConsumerConfig.GROUP_ID_CONFIG, "your_consumer_group_id");
        kafkaConsumerParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Kafka topic to consume from
        Collection<String> topics = Collections.singletonList("your_topic_name");

        // HDFS output directory
        String hdfsOutputDirectory = "hdfs://localhost:9000/path/to/output";

        // Create Spark configuration
        SparkConf conf = new SparkConf()
                .setAppName("KafkaMessageToParquet")
                .setMaster("local[*]"); // You can change the master URL based on your deployment

        // Create Spark Session
        SparkSession sparkSession = SparkSession.builder()
                .config(conf)
                .getOrCreate();

        // Create Spark Streaming context
        JavaStreamingContext streamingContext = new JavaStreamingContext(sparkSession.sparkContext(), new Duration(2000));

        // Create Kafka consumer DStream
        JavaInputDStream<ConsumerRecord<String, String>> kafkaStream =
                KafkaUtils.createDirectStream(
                        streamingContext,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(topics, kafkaConsumerParams)
                );

        // Extract relevant information from Kafka messages
        kafkaStream.foreachRDD((VoidFunction2<org.apache.spark.api.java.JavaRDD<ConsumerRecord<String, String>>, org.apache.spark.streaming.Time>) (rdd, time) -> {
            // Check if the RDD has any data
            if (!rdd.isEmpty()) {
                // Convert RDD of ConsumerRecords to RDD of GenericRecords
                org.apache.spark.api.java.JavaRDD<GenericRecord> genericRecordRDD = rdd.map(record -> {
                    // Create Avro record
                    Schema avroSchema = createAvroSchema();
                    GenericRecord avroRecord = new GenericData.Record(avroSchema);

                    // Extract relevant information from Kafka message
                    avroRecord.put("header", record.headers().toString());
                    avroRecord.put("key", record.key());
                    avroRecord.put("value", record.value());
                    avroRecord.put("offset", record.offset());

                    return avroRecord;
                });

                // Convert RDD to DataFrame
                Dataset<Row> dataFrame = sparkSession.createDataFrame(genericRecordRDD, GenericRecord.class);

                // Write DataFrame to HDFS in Parquet format
                dataFrame.write().parquet(hdfsOutputDirectory + "/" + time.milliseconds());
            }
        });

        // Start the streaming context
        streamingContext.start();

        // Wait for the streaming to finish
        streamingContext.awaitTermination();
    }

    private static Schema createAvroSchema() {
        // Define the Avro schema manually or load it from a file
        String avroSchemaJson = "{\"type\":\"record\",\"name\":\"Record\",\"fields\":[{\"name\":\"header\",\"type\":\"string\"},{\"name\":\"key\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"string\"},{\"name\":\"offset\",\"type\":\"long\"}]}";

        return new Schema.Parser().parse(avroSchemaJson);
    }
}
