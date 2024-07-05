import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FileModifier {

    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Usage: java PostGenProcessor <path-to-java-file> <method-name> <variable-name> <annotations-to-add> <annotations-to-remove>");
            System.exit(1);
        }

        String filePath = args[0];
        String methodName = args[1];
        String variableName = args[2];
        List<String> annotationsToAdd = Arrays.asList(args[3].split(","));
        List<String> annotationsToRemove = Arrays.asList(args[4].split(","));

        try {
            removeMethod(filePath, methodName);
            addAnnotationsToVariable(filePath, variableName, annotationsToAdd);
            removeAnnotationsFromClass(filePath, annotationsToRemove);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeMethod(String filePath, String methodName) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.err.println("File not found: " + filePath);
            System.exit(1);
        }

        // Parse the Java file
        CompilationUnit cu = StaticJavaParser.parse(path);

        // Create a visitor to find and remove the method
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration md, Void arg) {
                if (md.getNameAsString().equals(methodName)) {
                    System.out.println("Removing method: " + md.getNameAsString());
                    md.remove();
                }
                super.visit(md, arg);
            }
        }, null);

        // Write the updated content back to the file
        Files.write(path, cu.toString().getBytes());
        System.out.println("Method " + methodName + " removed successfully.");
    }

    public static void addAnnotationsToVariable(String filePath, String variableName, List<String> annotationNames) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.err.println("File not found: " + filePath);
            System.exit(1);
        }

        // Parse the Java file
        CompilationUnit cu = StaticJavaParser.parse(path);

        // Create a visitor to find the variable and add the annotations
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(FieldDeclaration fd, Void arg) {
                fd.getVariables().forEach(variable -> {
                    if (variable.getNameAsString().equals(variableName)) {
                        System.out.println("Adding annotations to variable: " + variable.getNameAsString());
                        annotationNames.forEach(annotationName -> {
                            MarkerAnnotationExpr annotation = new MarkerAnnotationExpr(annotationName);
                            fd.addAnnotation(annotation);
                        });
                    }
                });
                super.visit(fd, arg);
            }
        }, null);

        // Write the updated content back to the file
        Files.write(path, cu.toString().getBytes());
        System.out.println("Annotations " + annotationNames + " added to variable " + variableName + " successfully.");
    }

    public static void removeAnnotationsFromClass(String filePath, List<String> annotationNames) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.err.println("File not found: " + filePath);
            System.exit(1);
        }

        // Parse the Java file
        CompilationUnit cu = StaticJavaParser.parse(path);

        // Create a visitor to find the class and remove the annotations
        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration cid, Void arg) {
                annotationNames.forEach(annotationName -> {
                    cid.getAnnotations().removeIf(annotation -> annotation.getNameAsString().equals(annotationName));
                });
                super.visit(cid, arg);
            }
        }, null);

        // Write the updated content back to the file
        Files.write(path, cu.toString().getBytes());
        System.out.println("Annotations " + annotationNames + " removed from class successfully.");
    }
}
