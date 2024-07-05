package com.example.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class AddAnnotations {

    private static final String FIELD_NAME = "yourFieldName";  // Replace with the actual field name
    private static final String[] ANNOTATIONS = { 
        "@YourFirstAnnotation", 
        "@YourSecondAnnotation" 
        // Add more annotations as needed
    };

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java AddAnnotations <directory>");
            System.exit(1);
        }

        Path startPath = Paths.get(args[0]);
        Files.walk(startPath)
             .filter(path -> path.toString().endsWith(".java"))
             .forEach(path -> {
                 try {
                     addAnnotationsToField(path);
                     removeToStringMethod(path);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             });

        System.out.println("Annotations and toString methods processed successfully.");
    }

    private static void addAnnotationsToField(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<String> modifiedLines = new ArrayList<>();
        boolean fieldFound = false;

        for (String line : lines) {
            if (line.contains(FIELD_NAME)) {
                fieldFound = true;
                for (String annotation : ANNOTATIONS) {
                    modifiedLines.add(annotation);
                }
            }
            modifiedLines.add(line);
        }

        if (fieldFound) {
            Files.write(filePath, modifiedLines);
        }
    }

    private static void removeToStringMethod(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<String> modifiedLines = new ArrayList<>();
        boolean inToStringMethod = false;

        for (String line : lines) {
            if (line.trim().startsWith("@Override")) {
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
}


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
//                         <mainClass>com.example.util.AddAnnotations</mainClass>
//                         <arguments>
//                             <argument>${project.build.directory}/generated-sources</argument>
//                         </arguments>
//                         <classpathScope>compile</classpathScope>
//                     </configuration>
//                 </execution>
//             </executions>
//         </plugin>
//     </plugins>
// </build>

