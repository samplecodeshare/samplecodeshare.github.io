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
                processDirectory(path, pathConfig.getModifications());
            } else if (pathConfig.getType().equalsIgnoreCase("file")) {
                processFile(path, pathConfig.getModifications());
            }
        }

        System.out.println("Modifications processed successfully.");
    }

    private static void processDirectory(Path directoryPath, Modifications modifications) throws IOException {
        Files.walk(directoryPath)
             .filter(path -> path.toString().endsWith(".java"))
             .forEach(path -> {
                 try {
                     processFile(path, modifications);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             });
    }

    private static void processFile(Path filePath, Modifications modifications) throws IOException {
        addClassAnnotations(filePath, modifications.getAddClassAnnotations());
        removeClassAnnotations(filePath, modifications.getRemoveClassAnnotations());
        addFieldAnnotations(filePath, modifications.getAddFieldAnnotations());
        removeFieldAnnotations(filePath, modifications.getRemoveFieldAnnotations());
        if (modifications.isRemoveToString()) {
            removeToStringMethod(filePath);
        }
    }

    private static void addClassAnnotations(Path filePath, List<String> classAnnotations) throws IOException {
        if (classAnnotations != null && !classAnnotations.isEmpty()) {
            List<String> lines = Files.readAllLines(filePath);
            List<String> modifiedLines = new ArrayList<>(classAnnotations);
            modifiedLines.addAll(lines);
            Files.write(filePath, modifiedLines);
        }
    }

    private static void removeClassAnnotations(Path filePath, List<String> classAnnotations) throws IOException {
        if (classAnnotations != null && !classAnnotations.isEmpty()) {
            List<String> lines = Files.readAllLines(filePath);
            List<String> modifiedLines = new ArrayList<>();

            for (String line : lines) {
                if (!classAnnotations.contains(line.trim())) {
                    modifiedLines.add(line);
                }
            }

            Files.write(filePath, modifiedLines);
        }
    }

    private static void addFieldAnnotations(Path filePath, Map<String, List<String>> fieldAnnotations) throws IOException {
        if (fieldAnnotations != null && !fieldAnnotations.isEmpty()) {
            List<String> lines = Files.readAllLines(filePath);
            List<String> modifiedLines = new ArrayList<>();

            for (String line : lines) {
                boolean fieldFound = false;
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
    }

    private static void removeFieldAnnotations(Path filePath, Map<String, List<String>> fieldAnnotations) throws IOException {
        if (fieldAnnotations != null && !fieldAnnotations.isEmpty()) {
            List<String> lines = Files.readAllLines(filePath);
            List<String> modifiedLines = new ArrayList<>();

            for (String line : lines) {
                boolean annotationRemoved = false;
                for (Map.Entry<String, List<String>> entry : fieldAnnotations.entrySet()) {
                    if (line.contains(entry.getKey())) {
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

        public List<PathConfig> getPaths() {
            return paths;
        }

        public void setPaths(List<PathConfig> paths) {
            this.paths = paths;
        }
    }

    private static class PathConfig {
        private String type;
        private String path;
        private Modifications modifications;

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

        public Modifications getModifications() {
            return modifications;
        }

        public void setModifications(Modifications modifications) {
            this.modifications = modifications;
        }
    }

    private static class Modifications {
        private Map<String, List<String>> addFieldAnnotations;
        private Map<String, List<String>> removeFieldAnnotations;
        private List<String> addClassAnnotations;
        private List<String> removeClassAnnotations;
        private boolean removeToString;

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

        public boolean isRemoveToString() {
            return removeToString;
        }

        public void setRemoveToString(boolean removeToString) {
            this.removeToString = removeToString;
        }
    }
}

/*
paths:
  - type: directory
    path: src/main/java/com/example/generated
    modifications:
      addFieldAnnotations:
        yourFieldName:
          - "@YourFirstAnnotation"
          - "@YourSecondAnnotation"
      removeFieldAnnotations:
        yourFieldName:
          - "@RemoveThisAnnotation"
      addClassAnnotations:
        - "@YourClassAnnotation"
      removeClassAnnotations:
        - "@RemoveThisClassAnnotation"
      removeToString: true

  - type: file
    path: src/main/java/com/example/generated/SpecificFile.java
    modifications:
      addFieldAnnotations:
        anotherFieldName:
          - "@AnotherFieldAnnotation"
      removeFieldAnnotations:
        anotherFieldName:
          - "@RemoveAnotherFieldAnnotation"
      addClassAnnotations:
        - "@AnotherClassAnnotation"
      removeClassAnnotations:
        - "@RemoveAnotherClassAnnotation"
      removeToString: true
*/

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

