package com.example.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class PostGenProcessor {

    private static final String CONFIG_FILE = "config.yaml";

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        Config config = objectMapper.readValue(Paths.get(CONFIG_FILE).toFile(), Config.class);

        for (PathConfig pathConfig : config.getPaths()) {
            Path path = Paths.get(pathConfig.getPath());
            if (pathConfig.getType().equalsIgnoreCase("directory")) {
                processDirectory(path, config.getAnnotations());
            } else if (pathConfig.getType().equalsIgnoreCase("file")) {
                processFile(path, config.getAnnotations());
            }
        }

        System.out.println("Annotations processed successfully.");
    }

    private static void processDirectory(Path directoryPath, Annotations annotations) throws IOException {
        Files.walk(directoryPath)
             .filter(path -> path.toString().endsWith(".java"))
             .forEach(path -> {
                 try {
                     processFile(path, annotations);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             });
    }

    private static void processFile(Path filePath, Annotations annotations) throws IOException {
        addClassAnnotations(filePath, annotations.getAddClassAnnotations());
        removeClassAnnotations(filePath, annotations.getRemoveClassAnnotations());
        addFieldAnnotations(filePath, annotations.getAddFieldAnnotations());
        removeFieldAnnotations(filePath, annotations.getRemoveFieldAnnotations());
        removeToStringMethod(filePath);
    }

    private static void addClassAnnotations(Path filePath, List<String> classAnnotations) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<String> modifiedLines = new ArrayList<>(classAnnotations);
        modifiedLines.addAll(lines);
        Files.write(filePath, modifiedLines);
    }

    private static void removeClassAnnotations(Path filePath, List<String> classAnnotations) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<String> modifiedLines = new ArrayList<>();

        for (String line : lines) {
            if (!classAnnotations.contains(line.trim())) {
                modifiedLines.add(line);
            }
        }

        Files.write(filePath, modifiedLines);
    }

    private static void addFieldAnnotations(Path filePath, Map<String, List<String>> fieldAnnotations) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<String> modifiedLines = new ArrayList<>();
        boolean fieldFound = false;

        for (String line : lines) {
            for (Map.Entry<String, List<String>> entry : fieldAnnotations.entrySet()) {
                if (line.contains(entry.getKey())) {
                    fieldFound = true;
                    modifiedLines.addAll(entry.getValue());
                }
            }
            modifiedLines.add(line);
        }

        Files.write(filePath, modifiedLines);
    }

    private static void removeFieldAnnotations(Path filePath, Map<String, List<String>> fieldAnnotations) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<String> modifiedLines = new ArrayList<>();
        boolean fieldFound = false;

        for (String line : lines) {
            boolean annotationRemoved = false;
            for (Map.Entry<String, List<String>> entry : fieldAnnotations.entrySet()) {
                if (line.contains(entry.getKey())) {
                    fieldFound = true;
                    for (String annotation : entry.getValue()) {
                        if (line.contains(annotation)) {
                            annotationRemoved = true;
                        }
                    }
                }
            }
            if (!annotationRemoved) {
                modifiedLines.add(line);
            }
        }

        Files.write(filePath, modifiedLines);
    }

    private static void removeToStringMethod(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<String> modifiedLines = new ArrayList<>();
        boolean inToStringMethod = false;

        for (String line : lines) {
            if (line.trim().startsWith("@Override") && lines.contains("public String toString()")) {
                inToStringMethod = true;
            }
            if (inToStringMethod) {
                if (line.trim().equals("}")) {
                    inToStringMethod = false;
                }
                continue;
            }
            modifiedLines.add(line);
        }

        Files.write(filePath, modifiedLines);
    }

    private static class Config {
        private List<PathConfig> paths;
        private Annotations annotations;

        public List<PathConfig> getPaths() {
            return paths;
        }

        public void setPaths(List<PathConfig> paths) {
            this.paths = paths;
        }

        public Annotations getAnnotations() {
            return annotations;
        }

        public void setAnnotations(Annotations annotations) {
            this.annotations = annotations;
        }
    }

    private static class PathConfig {
        private String type;
        private String path;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    private static class Annotations {
        private Map<String, List<String>> addFieldAnnotations;
        private Map<String, List<String>> removeFieldAnnotations;
        private List<String> addClassAnnotations;
        private List<String> removeClassAnnotations;

        public Map<String, List<String>> getAddFieldAnnotations() {
            return addFieldAnnotations;
        }

        public void setAddFieldAnnotations(Map<String, List<String>> addFieldAnnotations) {
            this.addFieldAnnotations = addFieldAnnotations;
        }

        public Map<String, List<String>> getRemoveFieldAnnotations() {
            return removeFieldAnnotations;
        }

        public void setRemoveFieldAnnotations(Map<String, List<String>> removeFieldAnnotations) {
            this.removeFieldAnnotations = removeFieldAnnotations;
        }

        public List<String> getAddClassAnnotations() {
            return addClassAnnotations;
        }

        public void setAddClassAnnotations(List<String> addClassAnnotations) {
            this.addClassAnnotations = addClassAnnotations;
        }

        public List<String> getRemoveClassAnnotations() {
            return removeClassAnnotations;
        }

        public void setRemoveClassAnnotations(List<String> removeClassAnnotations) {
            this.removeClassAnnotations = removeClassAnnotations;
        }
    }
}



// paths:
//   - type: directory
//     path: src/main/java/com/example/generated
//   - type: file
//     path: src/main/java/com/example/generated/SpecificFile.java

// annotations:
//   addFieldAnnotations:
//     yourFieldName:
//       - "@YourFirstAnnotation"
//       - "@YourSecondAnnotation"
//   removeFieldAnnotations:
//     yourFieldName:
//       - "@RemoveThisAnnotation"
//   addClassAnnotations:
//     - "@YourClassAnnotation"
//   removeClassAnnotations:
//     - "@RemoveThisClassAnnotation"


// <build>
//     <plugins>
//         <!-- Code generation plugin configuration goes here -->

//         <plugin>
//             <groupId>org.codehaus.mojo</groupId>
//             <artifactId>exec-maven-plugin</artifactId>
//             <version>3.0.0</version>
//             <executions>
//                 <execution>
//                     <id>process-java-files</id>
//                     <phase>generate-sources</phase>
//                     <goals>
//                         <goal>java</goal>
//                     </goals>
//                     <configuration>
//                         <mainClass>com.example.util.PostGenProcessor</mainClass>
//                         <arguments>
//                             <argument>${project.basedir}/config.yaml</argument>
//                         </arguments>
//                         <classpathScope>compile</classpathScope>
//                     </configuration>
//                 </execution>
//             </executions>
//         </plugin>
//     </plugins>
// </build>

