// <dependencies>
//     <!-- SnakeYAML for YAML parsing -->
//     <dependency>
//         <groupId>org.yaml</groupId>
//         <artifactId>snakeyaml</artifactId>
//         <version>1.29</version>
//     </dependency>
//     <!-- Networknt JSON Schema Validator -->
//     <dependency>
//         <groupId>com.networknt</groupId>
//         <artifactId>json-schema-validator</artifactId>
//         <version>1.0.74</version>
//     </dependency>
//     <!-- Jackson for JSON processing -->
//     <dependency>
//         <groupId>com.fasterxml.jackson.core</groupId>
//         <artifactId>jackson-databind</artifactId>
//         <version>2.13.0</version>
//     </dependency>
// </dependencies>


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.parser.ParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class YamlSchemaValidator {

    public static void main(String[] args) {
        try {
            // Paths to the YAML and JSON Schema files
            String yamlFilePath = "path/to/your/file.yaml";
            String schemaFilePath = "path/to/your/schema.json";

            // Load JSON Schema from file
            JsonSchema jsonSchema = loadJsonSchemaFromFile(schemaFilePath);

            // Load YAML content from file and convert to JSON
            JsonNode yamlJson = convertYamlFileToJson(yamlFilePath);

            // Validate YAML against JSON Schema
            validateJsonSchema(jsonSchema, yamlJson, yamlFilePath);
            System.out.println("YAML data is valid.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonSchema loadJsonSchemaFromFile(String schemaFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = new FileInputStream(new File(schemaFilePath))) {
            JsonNode schemaNode = objectMapper.readTree(inputStream);
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
            return factory.getSchema(schemaNode);
        }
    }

    private static JsonNode convertYamlFileToJson(String yamlFilePath) throws IOException {
        Yaml yaml = new Yaml(new SafeConstructor());
        try (InputStream inputStream = new FileInputStream(new File(yamlFilePath))) {
            Object yamlObject = yaml.load(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.valueToTree(yamlObject);
        }
    }

    private static void validateJsonSchema(JsonSchema jsonSchema, JsonNode yamlJson, String yamlFilePath) throws IOException {
        Set<ValidationMessage> validationMessages = jsonSchema.validate(yamlJson);
        if (validationMessages.isEmpty()) {
            System.out.println("YAML data is valid.");
        } else {
            System.out.println("YAML data is invalid:");
            for (ValidationMessage message : validationMessages) {
                System.out.println(message.getMessage());
                System.out.println("Line number: " + findLineNumber(message.getPath(), yamlFilePath));
            }
        }
    }

    private static int findLineNumber(String path, String yamlFilePath) throws IOException {
        Yaml yaml = new Yaml(new SafeConstructor(), new Representer(), new DumperOptions(), new CustomLoaderOptions(path));
        try (InputStream inputStream = new FileInputStream(new File(yamlFilePath))) {
            yaml.load(inputStream);
            return yaml.getLoaderOptions().getLineNumber();
        } catch (ParserException e) {
            return -1; // If an error occurs, return -1 or handle it accordingly
        }
    }

    // Custom loader options to track line numbers
    static class CustomLoaderOptions extends SafeConstructor.LoaderOptions {
        private final String path;
        private int lineNumber = -1;

        public CustomLoaderOptions(String path) {
            this.path = path;
        }

        @Override
        public void set(Node node) {
            super.set(node);
            if (node.getStartMark() != null && node.getEndMark() != null) {
                if (path.equals(node.getValue())) {
                    lineNumber = node.getStartMark().getLine() + 1; // YAML line numbers are 0-based
                }
            }
        }

        public int getLineNumber() {
            return lineNumber;
        }
    }
}
