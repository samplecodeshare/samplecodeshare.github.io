import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.util.Map;

public class NoDateConversion {

    public static void main(String[] args) {
        String yamlStr = "key1: yes\nkey2: no\nkey3: 2024-07-19\nkey4: 1234";

        Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new CustomResolver());

        Map<String, Object> data = yaml.load(yamlStr);
        System.out.println(data);
    }

    public static class CustomResolver extends Resolver {
        @Override
        public void addImplicitResolvers() {
            // Disable the implicit resolver for dates
            addImplicitResolver(Tag.STR, Resolver.FLOAT, "-+0123456789.");
            addImplicitResolver(Tag.STR, Resolver.INT, "-+0123456789");
            addImplicitResolver(Tag.STR, Resolver.BOOL, "yYnNtTfFoO");
            // Comment out or remove the date resolver to prevent date conversion
            // addImplicitResolver(Tag.TIMESTAMP, Resolver.TIMESTAMP, "0123456789");
            addImplicitResolver(Tag.STR, Resolver.MERGE, "<<");
            addImplicitResolver(Tag.STR, Resolver.NULL, "~nN\0");
            addImplicitResolver(Tag.STR, Resolver.NULL, "nullNULLNull");
        }

        @Override
        public Tag resolve(NodeId kind, String value, boolean implicit) {
            // Always resolve as a string
            return Tag.STR;
        }
    }
}
