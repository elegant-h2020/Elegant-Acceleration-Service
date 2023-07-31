/*
 * This file is part of the ELEGANT Acceleration Service.
 * URL: https://github.com/elegant-h2020/Elegant-Acceleration-Service.git
 *
 * Copyright (c) 2023, APT Group, Department of Computer Science,
 * The University of Manchester. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.manchester.elegant.acceleration.service.tools;

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

    private static void emitPackagePrologue(StringBuilder sb, OperatorInfo operatorInfo) {
        sb.append("import uk.ac.manchester.tornado.api.annotations.Parallel;");
        sb.append("\n");
        if (operatorInfo.methodUsesMathPackage) {
            sb.append("import uk.ac.manchester.tornado.api.collections.math.TornadoMath;");
            sb.append("\n");
        }

        String[] operatorNames = OperatorParser.getUniqueOperatorName(operatorInfo);
        for (int i = 0; i < operatorNames.length; i++) {
            sb.append("import uk.ac.manchester.tornado.api.collections.types.");
            sb.append(operatorNames[i]);
            sb.append(";");
            sb.append("\n");
            sb.append("import uk.ac.manchester.tornado.api.collections.types.Vector");
            sb.append(operatorNames[i]);
            sb.append(";");
            sb.append("\n");
        }
        sb.append("\n");
    }

    private static void emitClassBegin(StringBuilder sb, String className) {
        sb.append("\n");
        sb.append("public class ");
        sb.append(className);
        sb.append(" {");
        sb.append("\n");
    }

    private static void emitUdfBody(StringBuilder sb, String method) {
        sb.append("\n");
        sb.append(method);
    }

    private static void emitClosingBrace(StringBuilder sb) {
        sb.append("}");
        sb.append("\n");
    }

    public static String extractUdfBodyFromFileToString(String path) {
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

    // TODO Deprecate
    private static String getMethodNameFromSignature(String signatureName) {
        String[] strings = signatureName.split("\\(");
        String[] subStrings = strings[0].split(" ");
        return subStrings[subStrings.length - 1];
    }

    // TODO Deprecate
    public static String getMethodNameFromFileName(String methodFileName) {
        String signatureName = getSignatureOfMethodFile(methodFileName);
        String[] strings = signatureName.split("\\(");
        String[] subStrings = strings[0].split(" ");
        return subStrings[subStrings.length - 1];
    }

    private static String extractSignature(String line) {
        return line.replaceFirst(" \\{|\\{", ";");
    }

    // TODO Deprecate
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

    public static String getVirtualClassName(String functionName) {
        String className = "Test" + functionName.substring(0, 1).toUpperCase() + functionName.substring(1);
        return className;
    }

    public static String getVirtualClassFileName(String functionName) {
        String className = getVirtualClassName(functionName);
        return className + SUFFIX;
    }

    public static String generateBoilerplateCode(String methodFileName, String functionName, OperatorInfo operatorInfo) {
        stringBuilder = new StringBuilder();
        emitPackagePrologue(stringBuilder, operatorInfo);
        String className = getVirtualClassName(functionName);
        emitClassBegin(stringBuilder, className);
        String udfBody = extractUdfBodyFromFileToString(methodFileName);
        emitUdfBody(stringBuilder, udfBody);
        emitClosingBrace(stringBuilder);
        return stringBuilder.toString();
    }

}
