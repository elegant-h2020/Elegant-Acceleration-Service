package uk.ac.manchester.acceleration.service.elegant.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ClassGenerator {
    private static final String SUFFIX = ".java";
    private static StringBuilder stringBuilder;

    private static void emitPackagePrologue(StringBuilder sb) {
        sb.append("import uk.ac.manchester.tornado.api.annotations.Parallel;");
        sb.append("\n");
    }

    private static void emitClassBegin(StringBuilder sb, String className) {
        sb.append("\n");
        sb.append("public class ");
        sb.append(className);
        sb.append(" {");
        sb.append("\n");
    }

    private static void emitMethod(StringBuilder sb, String method) {
        sb.append("\n");
        sb.append(method);
    }

    private static void emitClosingBrace(StringBuilder sb) {
        sb.append("}");
        sb.append("\n");
    }

    public static String extractMethodFromFileToString(String path) {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
            // Read the content with Stream
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

    public static void writeClassToFile(String classBody, String fileName) {
        FileWriter fileWriter;
        BufferedWriter bufferedWriter;
        try {
            fileWriter = new FileWriter(fileName);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.append(classBody);
            bufferedWriter.close();
        } catch (IOException e) {
            System.err.println("Error in writing of the generated class to file [" + fileName + "]." + e.getMessage());
        }
    }

    private static String getMethodNameFromSignature(String signatureName) {
        String[] strings = signatureName.split("\\(");
        String[] subStrings = strings[0].split(" ");
        return subStrings[subStrings.length - 1];
    }

    public static String getMethodNameFromFileName(String methodFileName) {
        String signatureName = getSignatureOfMethodFile(methodFileName);
        String[] strings = signatureName.split("\\(");
        String[] subStrings = strings[0].split(" ");
        return subStrings[subStrings.length - 1];
    }

    private static String extractSignature(String line) {
        return line.replaceFirst(" \\{|\\{", ";");
    }

    private static String getSignatureOfMethodFile(String fileName) {
        FileReader fileReader;
        BufferedReader bufferedReader;
        String signatureOfMethod;
        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            line = bufferedReader.readLine();
            signatureOfMethod = extractSignature(line);
            return signatureOfMethod;
        } catch (IOException e) {
            System.err.println("Input fileName [" + fileName + "] failed to run." + e.getMessage());
            return null;
        }
    }

    public static String getVirtualClassName(String methodFileName, long id) {
        System.out.println("methodFileName: " + methodFileName);
        String methodName = getMethodNameFromSignature(getSignatureOfMethodFile(methodFileName));
        System.out.println("methodName: " + methodName);
        String className = "Test" + methodName.substring(0, 1).toUpperCase() + methodName.substring(1) + id;
        return className;
    }

    public static String getVirtualClassFileName(String methodFileName, long id) {
        String className = getVirtualClassName(methodFileName, id);
        return className + SUFFIX;
    }

    public static String generateBoilerplateCode(String methodFileName, long id) {
        stringBuilder = new StringBuilder();
        emitPackagePrologue(stringBuilder);
        String className = getVirtualClassName(methodFileName, id);
        emitClassBegin(stringBuilder, className);
        String methodBody = extractMethodFromFileToString(methodFileName);
        emitMethod(stringBuilder, methodBody);
        emitClosingBrace(stringBuilder);
        return stringBuilder.toString();
    }

}
