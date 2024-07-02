import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YamlValidation {

    public static void main(String[] args) throws IOException, ProcessingException {
        // Example YAML content
        String yamlContent = "name: John Doe\nage: 30\nemail: john.doe@example.com";

        // Load YAML content into a JSON object
        ObjectMapper objectMapper = new ObjectMapper();
        Yaml yaml = new Yaml();
        Object yamlObject = yaml.load(yamlContent);
        JsonNode yamlJson = objectMapper.valueToTree(yamlObject);

        // Load JSON schema from URL (adjust URL accordingly)
        String schemaUrl = "https://example.com/schema.json";
        JsonNode schemaJson = loadJsonNodeFromUrl(schemaUrl);

        // Validate YAML against JSON schema and print validation issues with line numbers
        List<String> validationIssues = validateJsonSchema(schemaJson, yamlJson, yamlContent);
        if (validationIssues.isEmpty()) {
            System.out.println("YAML data is valid");
        } else {
            System.out.println("YAML data is invalid:");
            for (String issue : validationIssues) {
                System.out.println(issue);
            }
        }
    }

    private static JsonNode loadJsonNodeFromUrl(String url) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = new URL(url).openStream()) {
            return objectMapper.readTree(inputStream);
        }
    }

    private static List<String> validateJsonSchema(JsonNode schemaJson, JsonNode yamlJson, String yamlContent) throws ProcessingException {
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonSchema schema = factory.getJsonSchema(schemaJson);

        ProcessingReport report = schema.validate(yamlJson);

        // Collect validation issues with line numbers
        List<String> validationIssues = new ArrayList<>();
        for (ProcessingMessage message : report) {
            int lineNumber = getLineNumberFromPointer(message.asJson().get("instance").get("pointer").asText(), yamlContent);
            validationIssues.add("Line " + lineNumber + ": " + message.getMessage());
        }

        return validationIssues;
    }

    private static int getLineNumberFromPointer(String pointer, String yamlContent) {
        String[] lines = yamlContent.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (pointer.contains("/" + i)) {
                return i + 1; // Line numbers start from 1
            }
        }
        return -1; // Not found
    }
}
