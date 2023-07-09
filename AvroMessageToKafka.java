import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AvroMessageToKafka {

    public static void main(String[] args) {
        // Kafka configurations
        String kafkaBootstrapServers = "localhost:9092";
        String kafkaTopic = "your_topic_name";

        // Create Spark configuration
        SparkConf conf = new SparkConf()
                .setAppName("AvroMessageToKafka")
                .setMaster("local[*]"); // You can change the master URL based on your deployment

        // Create Spark context
        JavaSparkContext sparkContext = new JavaSparkContext(conf);

        // Generate Avro messages
        // Assuming you have an Avro schema named "your_schema.avsc"
        String avroSchemaFilePath = "/path/to/your_schema.avsc";
        Schema avroSchema = loadAvroSchema(avroSchemaFilePath);

        for (int i = 0; i < 10; i++) {
            GenericRecord avroRecord = generateAvroRecord(avroSchema);
            sendAvroMessageToKafka(avroRecord, kafkaBootstrapServers, kafkaTopic);
        }

        sparkContext.stop();
    }

    private static Schema loadAvroSchema(String avroSchemaFilePath) {
        // Load Avro schema from a file
        // Replace with your own logic to load the Avro schema
        // Example:
        // Schema.Parser parser = new Schema.Parser();
        // return parser.parse(new File(avroSchemaFilePath));
    }

    private static GenericRecord generateAvroRecord(Schema avroSchema) {
        // Generate Avro record
        // Replace with your own logic to generate Avro records based on the schema
        // Example:
        // GenericRecord avroRecord = new GenericData.Record(avroSchema);
        // avroRecord.put("field1", value1);
        // avroRecord.put("field2", value2);
        // return avroRecord;
    }

    private static void sendAvroMessageToKafka(GenericRecord avroRecord, String bootstrapServers, String topic) {
        // Create Avro message
        byte[] avroMessage = serializeAvroRecord(avroRecord);

        // Kafka producer configurations
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        // Create Kafka producer
        KafkaProducer<String, byte[]> kafkaProducer = new KafkaProducer<>(producerProps);

        // Send Avro message to Kafka
        ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(topic, avroMessage);
        kafkaProducer.send(producerRecord, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Message sent successfully. Offset: " + metadata.offset());
            } else {
                System.err.println("Failed to send message: " + exception.getMessage());
            }
        });

        // Close Kafka producer
        kafkaProducer.close();
    }

    private static byte[] serializeAvroRecord(GenericRecord avroRecord) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DatumWriter<GenericRecord> datumWriter = new SpecificDatumWriter<>(avroRecord.getSchema());

        try {
            Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            datumWriter.write(avroRecord, encoder);
            encoder.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }
}
