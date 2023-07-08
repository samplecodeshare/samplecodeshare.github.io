import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.sql.*;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

public class KafkaAvroToHDFSParquet {

    public static void main(String[] args) throws InterruptedException {
        // Kafka configurations
        String kafkaBootstrapServers = "localhost:9092";
        String kafkaTopic = "your_topic_name";
        String kafkaConsumerGroupId = "your_consumer_group_id";

        // HDFS output directory
        String hdfsOutputDirectory = "hdfs://localhost:9000/path/to/output";

        // Create Spark configuration
        SparkConf conf = new SparkConf()
                .setAppName("KafkaAvroToHDFSParquet")
                .setMaster("local[*]"); // You can change the master URL based on your deployment

        // Create Spark Session
        SparkSession sparkSession = SparkSession.builder()
                .config(conf)
                .getOrCreate();

        // Create Spark Streaming context
        JavaStreamingContext streamingContext = new JavaStreamingContext(sparkSession.sparkContext(), new Duration(2000));

        // Create Kafka consumer configuration
        Map<String, Object> kafkaConsumerParams = new HashMap<>();
        kafkaConsumerParams.put("bootstrap.servers", kafkaBootstrapServers);
        kafkaConsumerParams.put("key.deserializer", StringDeserializer.class);
        kafkaConsumerParams.put("value.deserializer", KafkaAvroDeserializer.class);
        kafkaConsumerParams.put("group.id", kafkaConsumerGroupId);
        kafkaConsumerParams.put("auto.offset.reset", "earliest");
        kafkaConsumerParams.put("specific.avro.reader", true);

        // Create Kafka consumer DStream
        Collection<String> topics = Arrays.asList(kafkaTopic);
        JavaInputDStream<ConsumerRecord<String, GenericRecord>> kafkaStream =
                KafkaUtils.createDirectStream(
                        streamingContext,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(topics, kafkaConsumerParams)
                );

        // Extract Avro records from Kafka stream
        kafkaStream.foreachRDD((VoidFunction2<JavaRDD<ConsumerRecord<String, GenericRecord>>, Time>) (rdd, time) -> {
            // Check if the RDD has any data
            if (!rdd.isEmpty()) {
                // Convert RDD of ConsumerRecords to RDD of GenericRecords
                JavaRDD<GenericRecord> genericRecordRDD = rdd.map(ConsumerRecord::value);

                // Convert GenericRecords to DataFrame
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
}
