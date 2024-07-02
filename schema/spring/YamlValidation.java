import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.erosb.jsonsKema.JsonSchema;
import com.github.erosb.jsonsKema.ValidationException;
import com.github.erosb.jsonsKema.ValidationFailure;
import com.github.erosb.jsonsKema.JsonLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class YamlSchemaValidator {

    public static void main(String[] args) {
        try {
            // Paths to the YAML and JSON Schema files
            String yamlFilePath = "path/to/your/file.yaml";
            String schemaFilePath = "path/to/your/schema.json";

            // Load JSON Schema from file
            JsonNode jsonSchema = loadJsonSchemaFromFile(schemaFilePath);

            // Load YAML content from file and convert to JSON
            JsonNode yamlJson = convertYamlFileToJson(yamlFilePath);

            // Validate YAML against JSON Schema
            validateJsonSchema(jsonSchema, yamlJson);
            System.out.println("YAML data is valid.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonNode loadJsonSchemaFromFile(String schemaFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = new FileInputStream(new File(schemaFilePath))) {
            return objectMapper.readTree(inputStream);
        }
    }

    private static JsonNode convertYamlFileToJson(String yamlFilePath) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(new File(yamlFilePath))) {
            Object yamlObject = yaml.load(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.valueToTree(yamlObject);
        }
    }

    private static void validateJsonSchema(JsonNode jsonSchema, JsonNode yamlJson) {
        JsonSchema schema = JsonLoader.load(jsonSchema);
        try {
            schema.validate(yamlJson);
        } catch (ValidationException e) {
            List<ValidationFailure> failures = e.getFailures();
            System.out.println("YAML data is invalid:");
            for (ValidationFailure failure : failures) {
                System.out.println(failure);
            }
        }
    }
}
