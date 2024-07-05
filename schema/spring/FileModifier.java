import javax.tools.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import com.sun.source.tree.*;
import com.sun.source.util.*;

public class FileModifier {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java MethodRemover <path-to-java-file> <method-name>");
            System.exit(1);
        }

        String filePath = args[0];
        String methodName = args[1];
        removeMethod(filePath, methodName);
    }

    public static void removeMethod(String filePath, String methodName) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.err.println("File not found: " + filePath);
            System.exit(1);
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("System Java compiler not available");
            System.exit(1);
        }

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(path.toFile());

        JavacTask task = (JavacTask) compiler.getTask(null, fileManager, null, null, null, compilationUnits);
        Iterable<? extends CompilationUnitTree> asts = task.parse();

        for (CompilationUnitTree ast : asts) {
            new TreeScanner<Void, Void>() {
                @Override
                public Void visitMethod(MethodTree methodTree, Void aVoid) {
                    if (methodTree.getName().toString().equals(methodName)) {
                        long startLine = getLineNumber(ast, methodTree);
                        long endLine = getEndLineNumber(ast, methodTree);

                        try {
                            List<String> lines = Files.readAllLines(path);
                            List<String> updatedLines = new ArrayList<>();

                            for (int i = 0; i < lines.size(); i++) {
                                long lineNumber = i + 1;
                                if (lineNumber < startLine || lineNumber > endLine) {
                                    updatedLines.add(lines.get(i));
                                }
                            }

                            Files.write(path, updatedLines);
                            System.out.println("Method " + methodName + " removed successfully.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return super.visitMethod(methodTree, aVoid);
                }

                private long getLineNumber(CompilationUnitTree ast, Tree tree) {
                    LineMap lineMap = ast.getLineMap();
                    return lineMap.getLineNumber(((JCTree) tree).getStartPosition());
                }

                private long getEndLineNumber(CompilationUnitTree ast, Tree tree) {
                    LineMap lineMap = ast.getLineMap();
                    return lineMap.getLineNumber(((JCTree) tree).getEndPosition());
                }
            }.scan(ast, null);
        }

        fileManager.close();
    }
}
