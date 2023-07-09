import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

// <dependencies>
//     <!-- Apache Avro -->
//     <dependency>
//         <groupId>org.apache.avro</groupId>
//         <artifactId>avro</artifactId>
//         <version>1.10.2</version>
//     </dependency>

//     <!-- Apache Commons IO -->
//     <dependency>
//         <groupId>commons-io</groupId>
//         <artifactId>commons-io</artifactId>
//         <version>2.11.0</version>
//     </dependency>
// </dependencies>

public class JsonToAvroConverter {

    public static void main(String[] args) {
        // JSON data
        String jsonData = "{\"field1\":\"value1\", \"field2\":\"value2\"}";

        // Avro schema
        String avroSchemaJson = "{\"type\":\"record\",\"name\":\"Record\",\"fields\":[{\"name\":\"field1\",\"type\":\"string\"},{\"name\":\"field2\",\"type\":\"string\"}]}";

        try {
            // Convert JSON to Avro
            byte[] avroData = convertJsonToAvro(jsonData, avroSchemaJson);

            // Write Avro data to a file
            String outputFile = "/path/to/output.avro";
            writeAvroDataToFile(avroData, outputFile);

            System.out.println("Conversion completed successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private static byte[] convertJsonToAvro(String jsonData, String avroSchemaJson) throws IOException {
        // Load Avro schema
        Schema avroSchema = new Schema.Parser().parse(avroSchemaJson);

        // Create Avro record
        GenericRecord avroRecord = new GenericData.Record(avroSchema);

        // Convert JSON to Avro
        JSONObject jsonObject = new JSONObject(jsonData);
        for (Schema.Field field : avroSchema.getFields()) {
            String fieldName = field.name();
            if (jsonObject.has(fieldName)) {
                avroRecord.put(fieldName, jsonObject.get(fieldName));
            }
        }

        // Serialize Avro record to bytes
        DatumWriter<GenericRecord> datumWriter = new SpecificDatumWriter<>(avroSchema);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
        datumWriter.write(avroRecord, encoder);
        encoder.flush();
        outputStream.close();

        return outputStream.toByteArray();
    }

    private static void writeAvroDataToFile(byte[] avroData, String outputFile) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        fileOutputStream.write(avroData);
        fileOutputStream.close();
    }
}
