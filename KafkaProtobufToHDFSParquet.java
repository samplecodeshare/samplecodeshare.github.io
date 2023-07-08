import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
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

public class KafkaProtobufToHDFSParquet {

    public static void main(String[] args) throws InterruptedException {
        // Kafka configurations
        String kafkaBootstrapServers = "localhost:9092";
        String kafkaTopic = "your_topic_name";
        String kafkaConsumerGroupId = "your_consumer_group_id";

        // HDFS output directory
        String hdfsOutputDirectory = "hdfs://localhost:9000/path/to/output";

        // Create Spark configuration
        SparkConf conf = new SparkConf()
                .setAppName("KafkaProtobufToHDFSParquet")
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
        kafkaConsumerParams.put("value.deserializer", ByteArrayDeserializer.class);
        kafkaConsumerParams.put("group.id", kafkaConsumerGroupId);
        kafkaConsumerParams.put("auto.offset.reset", "earliest");

        // Create Kafka consumer DStream
        Collection<String> topics = Arrays.asList(kafkaTopic);
        JavaInputDStream<ConsumerRecord<String, byte[]>> kafkaStream =
                KafkaUtils.createDirectStream(
                        streamingContext,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(topics, kafkaConsumerParams)
                );

        // Extract Protobuf messages from Kafka stream
        kafkaStream.foreachRDD((VoidFunction2<JavaRDD<ConsumerRecord<String, byte[]>>, Time>) (rdd, time) -> {
            // Check if the RDD has any data
            if (!rdd.isEmpty()) {
                // Convert RDD of ConsumerRecords to RDD of Protobuf messages
                JavaRDD<MessageLite> protobufRDD = rdd.map(record -> {
                    try {
                        MessageLite message = YourProtobufMessage.newBuilder()
                                .mergeFrom(record.value())
                                .build();
                        return message;
                    } catch (InvalidProtocolBufferException e) {
                        // Handle deserialization errors
                        return null;
                    }
                }).filter(Objects::nonNull);

                // Convert Protobuf RDD to DataFrame
                Dataset<Row> dataFrame = sparkSession.createDataFrame(protobufRDD, YourProtobufMessage.class);

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
