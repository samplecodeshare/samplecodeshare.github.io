import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class YamlParser {

    public static void main(String[] args) {
        YamlParser parser = new YamlParser();
        parser.parseYaml();
    }

    private void parseYaml() {
        try {
            Yaml yaml = new Yaml(new Constructor(Map.class));

            File initialFile = new File("C:\\muddusri\\dev\\openapi_test\\data.yaml");
            InputStream targetStream = new FileInputStream(initialFile);

            Map<String, Object> yamlData = yaml.load(targetStream);

            Map<String, Map<String, Object>> models = (Map<String, Map<String, Object>>) yamlData.get("models");
            Map<String, Map<String, Object>> definitions = (Map<String, Map<String, Object>>) yamlData.get("definitions");

            for (Map.Entry<String, Map<String, Object>> modelEntry : models.entrySet()) {
                String modelName = modelEntry.getKey();
                Map<String, Object> modelDetails = modelEntry.getValue();

                System.out.println("Model: " + modelName);
                System.out.println("Description: " + modelDetails.get("description"));

                if (modelDetails.get("type").equals("object")) {
                    if (modelDetails.containsKey("properties")) {
                        Map<String, Map<String, Object>> fields = (Map<String, Map<String, Object>>) modelDetails.get("properties");
                        for (Map.Entry<String, Map<String, Object>> fieldEntry : fields.entrySet()) {
                            String fieldName = fieldEntry.getKey();
                            Map<String, Object> fieldDetails = fieldEntry.getValue();
                            String path = modelName + "/" + fieldName;
                            Map<String, Object> fieldDetailsInner = getFieldType(fieldDetails, definitions, models);

                            System.out.println("  Field: " + fieldName + "(" + path + ")");
                            System.out.println("    Type: " + fieldDetailsInner.get("type"));
                            System.out.println("    Description: " + fieldDetailsInner.get("description"));
                            System.out.println("    Description: " + fieldDetails.get("description"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> getFieldType(Map<String, Object> fieldDetails, Map<String, Map<String, Object>> definitions, Map<String, Map<String, Object>> models) {
        if (fieldDetails.containsKey("$ref")) {
            String ref = (String) fieldDetails.get("$ref");
            if (ref.contains("/definitions")) {
                String refKey = ref.replace("#/definitions/", "");
                System.out.println(" Searching for ref =" + refKey + " Ref=" + ref);
                Map<String, Object> refDetails = definitions.get(refKey);
                return refDetails;
            }
            if (ref.contains("/models")) {
                String refKey = ref.replace("#/models/", "");
                System.out.println(" Searching for ref =" + refKey + " Ref=" + ref);
                Map<String, Object> refDetails = definitions.get(refKey);
                return refDetails;
            }
        }
        return fieldDetails;
    }
}
