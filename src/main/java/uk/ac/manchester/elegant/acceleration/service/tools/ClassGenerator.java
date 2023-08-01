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
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    private static String changeMethodToStatic(String line, OperatorInfo operatorInfo) {
        StringBuilder sb = new StringBuilder();
        if (line.contains(operatorInfo.udfName) && !operatorInfo.isMethodStatic) {
            String[] subStrings = line.split(operatorInfo.udfName, 2);
            StringTokenizer tokenizer = new StringTokenizer(subStrings[0], " ");

            while (tokenizer.hasMoreElements()) {
                String token = tokenizer.nextToken();
                operatorInfo.outputList.forEach(output -> {
                    if (output.className.equals(token)) {
                        subStrings[0] = subStrings[0].replace(token, "static " + token);
                    }
                });
            }
            for (int i = 0; i < subStrings.length; i++) {
                sb.append(subStrings[i]);
                if (i == 0) {
                    String newOperatorName = "tornado" + operatorInfo.udfName.substring(0, 1).toUpperCase() + operatorInfo.udfName.substring(1);
                    operatorInfo.renamedUdfName = newOperatorName;
                    sb.append(operatorInfo.renamedUdfName);
                }
            }
            operatorInfo.isMethodStatic = true;
        } else {
            sb.append(line);
        }
        return sb.toString();
    }

    private static String transformUdfPojosInLine(String line, OperatorInfo operatorInfo) {
        String transformedSignature = line;

        for (int i = 0; i < operatorInfo.inputList.size(); i++) {
            if (line.contains(operatorInfo.inputList.get(i).className)) {
                OperatorObject deprecatedObject = operatorInfo.inputList.get(i);
                OperatorObject tornadoObject = operatorInfo.tornadifiedInputList.get(i);
                operatorInfo.tornadifiedInputList.forEach(input -> System.out.println(input.className));
                transformedSignature = line.replace(deprecatedObject.className, tornadoObject.className);
            }
        }

        for (int i = 0; i < operatorInfo.outputList.size(); i++) {
            if (line.contains(operatorInfo.outputList.get(i).className)) {
                OperatorObject deprecatedObject = operatorInfo.outputList.get(i);
                OperatorObject tornadoObject = operatorInfo.tornadifiedOutputList.get(i);
                operatorInfo.tornadifiedInputList.forEach(input -> System.out.println(input.className));
                transformedSignature = transformedSignature.replace(deprecatedObject.className, tornadoObject.className);
            }
        }
        return transformedSignature;
    }

    private static String transformMathToTornadoMath(String line) {
        String transformedMathLine = line;
        if (line.contains("Math.")) {
            transformedMathLine = transformedMathLine.replace("Math", "TornadoMath");
        }
        return transformedMathLine;
    }

    private static String getReadAccessOfTornadoFieldNameForIndex(ArrayList fields, int index) {
        String tornadoFieldName = null;
        switch (index) {
            case 0:
                tornadoFieldName = (fields.size() < 6) ? "getX()" : "getS0()";
                break;
            case 1:
                tornadoFieldName = (fields.size() < 6) ? "getY()" : "getS1()";
                break;
            case 2:
                tornadoFieldName = (fields.size() < 6) ? "getZ()" : "getS2()";
                break;
            case 3:
                tornadoFieldName = (fields.size() < 6) ? "getW()" : "getS3()";
                break;
            case 4:
                tornadoFieldName = "getS4()";
                break;
            case 5:
                tornadoFieldName = "getS5()";
                break;
            case 6:
                tornadoFieldName = "getS6()";
                break;
            case 7:
                tornadoFieldName = "getS7()";
                break;
        }
        return tornadoFieldName;
    }

    private static String getWriteOperationOfTornadoFieldNameForIndex(ArrayList fields, int index) {
        String tornadoFieldName = null;
        switch (index) {
            case 0:
                tornadoFieldName = (fields.size() < 6) ? "setX(" : "setS0(";
                break;
            case 1:
                tornadoFieldName = (fields.size() < 6) ? "setY(" : "setS1(";
                break;
            case 2:
                tornadoFieldName = (fields.size() < 6) ? "setZ(" : "setS2(";
                break;
            case 3:
                tornadoFieldName = (fields.size() < 6) ? "setW(" : "setS3(";
                break;
            case 4:
                tornadoFieldName = "setS4(";
                break;
            case 5:
                tornadoFieldName = "setS5(";
                break;
            case 6:
                tornadoFieldName = "setS6(";
                break;
            case 7:
                tornadoFieldName = "setS7(";
                break;
        }
        return tornadoFieldName;
    }

    private static String transformFieldsOfInputObjects(String line, OperatorInfo operatorInfo) {
        for (int i = 0; i < operatorInfo.argumentNameList.size(); i++) {
            if (line.contains(operatorInfo.argumentNameList.get(i) + ".")) {
                ArrayList fields = operatorInfo.inputVariableNameToTypeMap.get(operatorInfo.argumentNameList.get(i)).getListOfField();
                for (int j = 0; j < fields.size(); j++) {
                    ObjectField field = (ObjectField) fields.get(j);
                    String tornadoFieldName = getReadAccessOfTornadoFieldNameForIndex(fields, j);
                    if (line.contains(field.fieldName)) {
                        line = line.replace(field.fieldName, tornadoFieldName);
                    }
                }
            }
        }
        return line;
    }

    private static String transformVariablePojoFieldsWithPrimitiveTypes(String line, OperatorInfo operatorInfo) {
        for (int i = 0; i < operatorInfo.variableNameList.size(); i++) {
            String variableName = operatorInfo.variableNameList.get(i);
            if (line.contains(variableName + ".")) {
                OperatorObject pojo = operatorInfo.variableNameToTypeMap.get(variableName);

                ArrayList fields = pojo.getListOfField();
                for (int j = 0; j < fields.size(); j++) {
                    ObjectField field = (ObjectField) fields.get(j);
                    String pojoType = pojo.listOfTypes.get(j);
                    String pojoAccess = variableName + "." + field.fieldName;
                    if (line.contains(pojoAccess)) {
                        line = line.replaceAll(pojoAccess, field.fieldName);
                        if (!field.isDeclaredInRewrittenFunction) {
                            line = line.replaceFirst(field.fieldName, pojoType + " " + field.fieldName);
                            field.isDeclaredInRewrittenFunction = true;
                        }

                        // 5. Add primitive variables to Tornado types
                        String tornadoFieldName = getWriteOperationOfTornadoFieldNameForIndex(fields, j);
                        line = emitTornadoWriteAccessToLine(variableName, tornadoFieldName, field.fieldName, line);
                    }
                }
            }
        }
        return line;
    }

    /**
     * This method emits the write access operations for TornadoVM Types. E.g.
     * variableName.setX(fieldName);
     * 
     * @param variableName
     * @param tornadoFieldName
     * @param fieldName
     * @param line
     * @return
     */
    private static String emitTornadoWriteAccessToLine(String variableName, String tornadoFieldName, String fieldName, String line) {
        StringBuilder sb = new StringBuilder(line);
        sb.append("\n");
        sb.append("\t");
        sb.append(variableName);
        sb.append(".");
        sb.append(tornadoFieldName);
        sb.append(fieldName);
        sb.append(");");
        return sb.toString();
    }

    private static String getTornadoVMVectorTypeOfPojo(OperatorObject operatorObject) {
        return "Vector" + operatorObject.className;
    }

    /**
     * public static void map(VectorDouble2 in1, VectorDouble2 output) { for
     * (@Parallel int i = 0; i < in1.getLength(); i++) { output.set(i,
     * map(in1.get(i))); } }
     * 
     * @param operatorInfo
     * @return
     */
    public static String extractScalableSkeletonBody(OperatorInfo operatorInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("public ");
        if (operatorInfo.isMethodStatic) {
            sb.append("static ");
        }
        sb.append("void ");
        sb.append(operatorInfo.udfName);
        sb.append("(");
        for (int i = 0; i < operatorInfo.tornadifiedInputList.size(); i++) {
            OperatorObject pojo = operatorInfo.tornadifiedInputList.get(i);
            sb.append(getTornadoVMVectorTypeOfPojo(pojo));
            sb.append(" ");
            sb.append(operatorInfo.argumentNameList.get(i));
            sb.append(", ");
        }
        for (int i = 0; i < operatorInfo.tornadifiedOutputList.size(); i++) {
            OperatorObject pojo = operatorInfo.tornadifiedOutputList.get(i);
            sb.append(getTornadoVMVectorTypeOfPojo(pojo));
            sb.append(" ");
            sb.append("output");
            sb.append(")");
            sb.append("{");
            sb.append("\n");
        }
        sb.append("\t");
        sb.append("for (@Parallel int i = 0; i < output.getLength(); i++) {");
        sb.append("\n");
        sb.append("\t\t");
        sb.append("output.set(i, ");
        sb.append(operatorInfo.renamedUdfName);
        sb.append("(");
        for (int i = 0; i < operatorInfo.tornadifiedInputList.size(); i++) {
            sb.append(operatorInfo.argumentNameList.get(i));
            sb.append(".get(i)");
            if (i == operatorInfo.tornadifiedInputList.size() - 1) {
                sb.append("));");
                sb.append("\n");
            } else {
                sb.append(", ");
            }
        }
        sb.append("\t}");
        sb.append("}");
        sb.append("\n");
        return sb.toString();
    }

    public static String extractUdfBodyFromFileToString(String path, OperatorInfo operatorInfo) {
        StringBuilder contentBuilder = new StringBuilder();
        AtomicBoolean skipObject = new AtomicBoolean(false);
        AtomicInteger numberOfOpenCurlyBrackets = new AtomicInteger(0);

        try (Stream<String> stream = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {

            // if (operatorInfo.udfName.equals("map")) {

            // Read the content with Stream
            stream.forEach(s -> {
                if (!skipObject.get() && s.contains("class")) {
                    skipObject.set(true);
                }

                if (skipObject.get() && s.contains("{")) {
                    numberOfOpenCurlyBrackets.incrementAndGet();
                } else if (skipObject.get() && s.contains("}") && numberOfOpenCurlyBrackets.getAndDecrement() == 1) {
                    skipObject.set(false);
                } else if (!skipObject.get()) {
                    if (isLineEmpty(s)) {
                        contentBuilder.append(s).append("\n");
                    } else {
                        // 1. First change the NES UDF to be static
                        String transformedSignature = changeMethodToStatic(s, operatorInfo);

                        // 2. Replace objectTypes of input and output with TornadoTypes
                        transformedSignature = transformUdfPojosInLine(transformedSignature, operatorInfo);

                        // 3. Replace Math with TornadoMath
                        transformedSignature = transformMathToTornadoMath(transformedSignature);

                        // FIXME Apply also casting if type is double

                        // 4. Replace object fields with primitive variables of the same name
                        transformedSignature = transformVariablePojoFieldsWithPrimitiveTypes(transformedSignature, operatorInfo);

                        // 6. Replace accesses of fields of input objects with Tornado accesses
                        transformedSignature = transformFieldsOfInputObjects(transformedSignature, operatorInfo);

                        contentBuilder.append(transformedSignature).append("\n");
                    }
                }
            });
            // } else {
            // stream.forEach(s -> contentBuilder.append(s).append("\n"));
            // }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

    private static boolean isLineEmpty(String string) {
        return string.equals("\t") || string.equals(" ");
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
        String udfBody = extractUdfBodyFromFileToString(methodFileName, operatorInfo);
        emitUdfBody(stringBuilder, udfBody);
        String scalableSkeletonBody = extractScalableSkeletonBody(operatorInfo);
        emitUdfBody(stringBuilder, scalableSkeletonBody);
        emitClosingBrace(stringBuilder);
        return stringBuilder.toString();
    }

}
