// <dependencies>
//     <!-- SnakeYAML for YAML parsing -->
//     <dependency>
//         <groupId>org.yaml</groupId>
//         <artifactId>snakeyaml</artifactId>
//         <version>1.29</version>
//     </dependency>
//     <!-- JSON-P (javax.json) for JSON processing -->
//     <dependency>
//         <groupId>javax.json</groupId>
//         <artifactId>javax.json-api</artifactId>
//         <version>1.1.4</version>
//     </dependency>
//     <!-- Reference Implementation of JSON-P (needed for implementation classes) -->
//     <dependency>
//         <groupId>org.glassfish</groupId>
//         <artifactId>javax.json</artifactId>
//         <version>1.1.4</version>
//     </dependency>
// </dependencies>

import org.yaml.snakeyaml.Yaml;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class YamlSchemaValidator {

    public static void main(String[] args) {
        try {
            // Paths to the YAML and JSON Schema files
            String yamlFilePath = "path/to/your/file.yaml";
            String schemaFilePath = "path/to/your/schema.json";

            // Load JSON Schema
            JsonObject jsonSchema = loadJsonSchemaFromFile(schemaFilePath);

            // Load YAML content from file
            Object yamlObject = loadYamlFromFile(yamlFilePath);

            // Validate YAML against JSON Schema
            validateJsonSchema(jsonSchema, yamlObject);
            System.out.println("YAML data is valid.");
        } catch (IOException | JsonValidationException e) {
            e.printStackTrace();
        }
    }

    private static JsonObject loadJsonSchemaFromFile(String schemaFilePath) throws IOException {
        try (JsonReader reader = Json.createReader(new FileReader(schemaFilePath))) {
            return reader.readObject();
        }
    }

    private static Object loadYamlFromFile(String yamlFilePath) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = YamlSchemaValidator.class.getResourceAsStream(yamlFilePath)) {
            return yaml.load(inputStream);
        }
    }

    private static void validateJsonSchema(JsonObject jsonSchema, Object yamlObject) throws JsonValidationException {
        JsonStructure jsonStructure = Json.createValue(jsonSchema.toString());
        JsonReader reader = Json.createReaderFactory(null).createReader(Json.createValue(yamlObject.toString()));
        JsonStructure actualData = reader.read();
        if (!jsonStructure.equals(actualData)) {
            throw new JsonValidationException("Invalid JSON.");
        }
    }
}
