import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.io.ParsingEncoder;
import org.apache.avro.io.ResolvingDecoder;
import org.apache.avro.io.ValidatingDecoder;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


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
        ByteArrayInputStream inputStream = new ByteArrayInputStream(avroData);
        SeekableByteArrayInput seekableInput = new SeekableByteArrayInput(inputStream);

        Schema schema = new Schema.Parser().parse(avroSchema);

        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(seekableInput, null);

        GenericRecord avroRecord = datumReader.read(null, createDecoderWithValidation(decoder, schema));
        ParsingEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(schema, System.out);

        JsonEncoder encoder = EncoderFactory.get().validatingEncoder(schema, jsonEncoder);

        encoder.configure(jsonEncoder);
        encoder.write(avroRecord);

        jsonEncoder.flush();

        return IOUtils.toString(System.out, StandardCharsets.UTF_8);
    }

    private static ResolvingDecoder createDecoderWithValidation(Decoder decoder, Schema writerSchema) {
        ValidatingDecoder.ValidatingEncoder validatingEncoder = new ValidatingDecoder.ValidatingEncoder(writerSchema);
        validatingEncoder.configure(decoder);

        return validatingEncoder;
    }
}
