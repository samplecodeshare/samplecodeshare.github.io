import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroToJsonConverter {

    public static void main(String[] args) {
        // Avro data as byte array
        byte[] avroData = getAvroData();

        // Avro schema as string
        String avroSchema = getAvroSchema();

        try {
            // Convert Avro to JSON
            String jsonData = convertAvroToJson(avroData, avroSchema);

            System.out.println("Converted JSON data:\n" + jsonData);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private static byte[] getAvroData() {
        // Return your Avro data as byte array
        // Example:
        // byte[] avroData = ...;
        // return avroData;
    }

    private static String getAvroSchema() {
        // Return your Avro schema as a string
        // Example:
        // String avroSchema = "...";
        // return avroSchema;
    }

    private static String convertAvroToJson(byte[] avroData, String avroSchema) throws IOException {
        // Create Avro schema
        Schema schema = new Schema.Parser().parse(avroSchema);

        // Create Avro reader and decoder
        DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(avroData, null);

        // Read Avro record from the binary data
        GenericRecord avroRecord = reader.read(null, decoder);

        // Convert Avro record to JSON
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, outputStream);
        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
        writer.write(avroRecord, encoder);
        encoder.flush();

        return outputStream.toString();
    }
}
