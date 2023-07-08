import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvToAvroConverter {

    public static void main(String[] args) {
        // Path to the CSV file
        String csvFilePath = "/path/to/input.csv";

        // Path to the Avro file
        String avroFilePath = "/path/to/output.avro";

        try {
            // Parse CSV file
            CSVParser csvParser = new CSVParser(new FileReader(csvFilePath), CSVFormat.DEFAULT.withHeader());

            // Create Avro schema
            Schema avroSchema = createAvroSchema();

            // Create Avro records from CSV data
            List<GenericRecord> avroRecords = createAvroRecords(csvParser, avroSchema);

            // Write Avro records to Avro file
            writeAvroFile(avroRecords, avroSchema, avroFilePath);

            System.out.println("Conversion completed successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }

    private static Schema createAvroSchema() {
        // Define the Avro schema manually or load it from a file
        String avroSchemaJson = "{\"type\":\"record\",\"name\":\"Record\",\"fields\":[{\"name\":\"field1\",\"type\":\"string\"},{\"name\":\"field2\",\"type\":\"int\"}]}";

        return new Schema.Parser().parse(avroSchemaJson);
    }

    private static List<GenericRecord> createAvroRecords(CSVParser csvParser, Schema avroSchema) {
        List<GenericRecord> avroRecords = new ArrayList<>();

        for (CSVRecord csvRecord : csvParser) {
            // Create Avro record
            GenericRecord avroRecord = new GenericData.Record(avroSchema);
            avroRecord.put("field1", csvRecord.get("column1")); // Map CSV columns to Avro fields
            avroRecord.put("field2", Integer.parseInt(csvRecord.get("column2")));

            avroRecords.add(avroRecord);
        }

        return avroRecords;
    }

    private static void writeAvroFile(List<GenericRecord> avroRecords, Schema avroSchema, String avroFilePath) throws IOException {
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(avroSchema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);

        dataFileWriter.create(avroSchema, new File(avroFilePath));
        for (GenericRecord record : avroRecords) {
            dataFileWriter.append(record);
        }
        dataFileWriter.close();
    }
}
