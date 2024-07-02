import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.parser.ParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

public class EveritYamlSchemaValidator {

    public static void main(String[] args) {
        try {
            // Example YAML content
            String yamlContent = "name: John Doe\nage: 30\nemail: john.doe@example.com";

            // Load JSON Schema from URL
            String schemaUrl = "https://example.com/schema.json";
            JSONObject jsonSchema = loadJsonSchemaFromUrl(schemaUrl);

            // Load YAML content and convert to JSON
            JSONObject yamlJson = convertYamlToJson(yamlContent);

            // Validate YAML against JSON Schema
            validateJsonSchema(jsonSchema, yamlJson);
            System.out.println("YAML data is valid.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject loadJsonSchemaFromUrl(String schemaUrl) throws IOException {
        try (InputStream inputStream = new URL(schemaUrl).openStream()) {
            return new JSONObject(new JSONTokener(inputStream));
        }
    }

    private static JSONObject convertYamlToJson(String yamlContent) throws IOException {
        Yaml yaml = new Yaml();
        Object yamlObject = yaml.load(yamlContent);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(yamlObject);
        return new JSONObject(objectMapper.writeValueAsString(jsonNode));
    }

    private static void validateJsonSchema(JSONObject jsonSchema, JSONObject yamlJson) {
        Schema schema = SchemaLoader.load(jsonSchema);
        try {
            schema.validate(yamlJson); // throws a ValidationException if this object is invalid
        } catch (org.everit.json.schema.ValidationException e) {
            System.out.println("YAML data is invalid:");
            for (String error : e.getAllMessages()) {
                System.out.println(error);
            }
        }
    }
}
