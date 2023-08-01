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
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class OperatorParser {

    private static boolean shouldParseObject = false;
    private static OperatorObject operatorObject = null;

    public static void parseMethod(String methodFileName) {
        OperatorInfo operatorInfo = new OperatorInfo();

        try (Stream<String> stream = Files.lines(Paths.get(methodFileName), StandardCharsets.UTF_8)) {
            // Read the content with Stream
            stream.forEach(s -> {
                try {
                    parseLine(s, operatorInfo, null);
                } catch (IOException e) {
                    System.err.println("Runtime error while parsing lines of input files: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean isLineEmpty(String string) {
        return string.equals("\t") || string.equals(" ");
    }

    private static boolean lineStartsAComment(String line) {
        return line.startsWith("//");
    }

    private static int getNumberOfInputs(String line) {
        int stringTokenizer = new StringTokenizer(line, ",").countTokens();
        return stringTokenizer;
    }

    private static void checkMathFunctionInOperator(String token, OperatorInfo operatorInfo) {
        if (token.contains("Math.")) {
            operatorInfo.methodUsesMathPackage = true;
        }
    }

    private static void parseLine(String line, OperatorInfo operatorInfo, String functionName) throws IOException {
        if (isLineEmpty(line) || lineStartsAComment(line)) {
            return;
        }

        if (line.contains("class")) {
            shouldParseObject = true;
            operatorObject = new OperatorObject();
        }

        if (shouldParseObject) {
            parseObjectClass(line, operatorObject, operatorInfo);
        } else {
            parseOperator(line, operatorInfo, functionName);
        }
    }

    private static void parseObjectClass(String line, OperatorObject operatorObject, OperatorInfo operatorInfo) {
        if (line.equals("}")) {
            shouldParseObject = false;
            return;
        }
        if (isLineEmpty(line) || lineStartsAComment(line)) {
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(line, " ");
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();

            switch (token) {
                case "public":
                    operatorObject.isObjectPublic = true;
                    break;
                case "static":
                    operatorObject.isObjectStatic = true;
                    break;
                case "class":
                    operatorObject.className = tokenizer.nextToken(" ");
                    operatorInfo.hashMapOfNameAndOperatorObjects.put(operatorObject.className, operatorObject);
                    operatorInfo.listOfOperatorObjectNames.add(operatorObject.className);
                    tokenizer.nextToken(" "); // this should be the bracket {
                    break;
                default:
                    String nextToken = tokenizer.nextToken(" ;");
                    operatorObject.addEntryInMapTypeToVariableName(token, nextToken);
                    operatorObject.addEntryInFieldName(nextToken);
                    operatorObject.addEntryInFieldType(token);
                    break;
            }
        }
    }

    private static String trimFirstSpaceFromString(String string) {
        return string.replaceFirst("\\s+", "");
    }

    private static void setVariableNamesOfUdfPojos(String line, OperatorInfo operatorInfo) {
        String[] operatorNames = OperatorParser.getUniqueOperatorName(operatorInfo);
        for (int i = 0; i < operatorNames.length; i++) {
            if (line.contains(operatorNames[i]) && line.contains("new")) { // Declaration of new objects;
                line = trimFirstSpaceFromString(line);
                String[] strings = line.split("=");
                String[] subStrings = strings[0].split(" ");
                if (subStrings.length > 1) {
                    operatorInfo.variableNameList.add(subStrings[1]);
                    OperatorObject pojo = operatorInfo.hashMapOfNameAndOperatorObjects.get(subStrings[0]);
                    operatorInfo.variableNameToTypeMap.put(subStrings[1], pojo);
                }
            }
        }
    }

    private static void parseOperator(String line, OperatorInfo operatorInfo, String functionName) {
        if (line.equals("}") || isLineEmpty(line) || lineStartsAComment(line)) {
            // shouldParseObject = false;
            return;
        }

        operatorInfo.udfName = functionName;

        // Add declared variables
        setVariableNamesOfUdfPojos(line, operatorInfo);

        StringTokenizer tokenizer = new StringTokenizer(line, " (");

        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            switch (token) {
                case "public":
                    operatorInfo.isMethodPublic = true;
                    String nextToken = tokenizer.nextToken();
                    if (nextToken.equals("static")) {
                        operatorInfo.isMethodStatic = true;
                        nextToken = tokenizer.nextToken();
                    }
                    if (nextToken.equals("void")) {
                        break;
                    } else {
                        // 1. Check that object name exists in the added parsed list
                        if (operatorInfo.listOfOperatorObjectNames.contains(nextToken)) {
                            OperatorObject pojo = operatorInfo.hashMapOfNameAndOperatorObjects.get(nextToken);
                            // 2. Add the operator Object in the outputList
                            operatorInfo.outputList.add(pojo);
                        } else {
                            System.err.println("The object (" + nextToken + ") is not recognized.");
                        }
                    }

                    break;
                case "map":
                    // operatorInfo.udfName = token;
                    int numberOfInputs = getNumberOfInputs(line);
                    for (int i = 0; i < numberOfInputs; i++) {
                        String tokenAfterOperatorName = tokenizer.nextToken();
                        if (tokenAfterOperatorName.equals("final")) {
                            tokenAfterOperatorName = tokenizer.nextToken();
                        }
                        OperatorObject pojo = operatorInfo.hashMapOfNameAndOperatorObjects.get(tokenAfterOperatorName);
                        String argumentName = tokenizer.nextToken(") ");
                        operatorInfo.inputList.add(pojo);
                        operatorInfo.argumentNameList.add(argumentName);
                        operatorInfo.inputVariableNameToTypeMap.put(argumentName, pojo);
                    }
                    break;
                default:
                    checkMathFunctionInOperator(token, operatorInfo);
                    break;
            }
        }
    }

    public static OperatorInfo parse(String methodFileName, String functionName) {
        FileReader fileReader;
        BufferedReader bufferedReader;
        try {
            fileReader = new FileReader(methodFileName);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            OperatorInfo operatorInfo = new OperatorInfo();
            while ((line = bufferedReader.readLine()) != null) {
                parseLine(line, operatorInfo, functionName);
            }
            return operatorInfo;
        } catch (IOException e) {
            System.err.println("Wrong uploaded file or format. Please ensure that the UDF file is configured properly!");
        }
        return null;
    }

    private static boolean checkHomogeneityOfFields(ArrayList typesOfFields) {
        AtomicBoolean isHomogeneous = new AtomicBoolean(true);
        final String[] firstType = { null };
        typesOfFields.forEach((t) -> {
            if (firstType[0] == null) {
                firstType[0] = (String) t;
            } else if (!firstType[0].equals(t)) {
                isHomogeneous.set(false);
            }
        });

        return isHomogeneous.get();
    }

    private static String tornadifyType(ArrayList typesOfFields) {
        int numOfFields = typesOfFields.size();
        String typeName = (String) typesOfFields.get(0);
        switch (typeName) {
            case "double":
                if (numOfFields == 1) {
                    return typeName;
                } else if (numOfFields == 2) {
                    return "Double2";
                } else if (numOfFields == 4) {
                    return "Double4";
                } else if (numOfFields == 8) {
                    return "Double8";
                } else if (numOfFields == 16) {
                    return "Double16";
                }
                break;
            case "float":
                if (numOfFields == 1) {
                    return typeName;
                } else if (numOfFields == 2) {
                    return "Float2";
                } else if (numOfFields == 4) {
                    return "Float4";
                } else if (numOfFields == 8) {
                    return "Float8";
                } else if (numOfFields == 16) {
                    return "Float16";
                }
                break;
            case "long":
                if (numOfFields == 1) {
                    return typeName;
                } else if (numOfFields == 2) {
                    return "Long2";
                } else if (numOfFields == 4) {
                    return "Long4";
                } else if (numOfFields == 8) {
                    return "Long8";
                } else if (numOfFields == 16) {
                    return "Long16";
                }
                break;
            case "int":
                if (numOfFields == 1) {
                    return typeName;
                } else if (numOfFields == 2) {
                    return "Int2";
                } else if (numOfFields == 4) {
                    return "Int4";
                } else if (numOfFields == 8) {
                    return "Int8";
                } else if (numOfFields == 16) {
                    return "Int16";
                }
                break;
            default:
                System.err.println("Not valid type in TornadoVM for operator type [" + typeName + "].");
                break;
        }
        return null;
    }

    private static String getTornadoVMTypeForPojoNameIfHomogeneous(String pojoName, ArrayList list) {
        for (int i = 0; i < list.size(); i++) {
            OperatorObject pojo = (OperatorObject) list.get(i);
            if (pojo.className.equals(pojoName)) {
                ArrayList typesOfFields = pojo.getListOfTypes();
                // a. check that types are homogeneous
                if (checkHomogeneityOfFields(typesOfFields)) {
                    // b. Find the replacable TornadoVM Type
                    String tornadoTypeName = tornadifyType(typesOfFields);
                    return tornadoTypeName;
                } else {
                    System.err.println("The types in operator [" + pojoName + "] should be homogeneous.");
                }
            }
        }
        return null;
    }

    static void tornadifyIO(OperatorInfo operatorInfo) {
        final ListIterator<String> stringListIterator = operatorInfo.listOfOperatorObjectNames.listIterator();
        while (stringListIterator.hasNext()) {
            String pojoName = stringListIterator.next(); // CartesianCoordinate
            OperatorObject tornadoInputPojo = null;
            OperatorObject tornadoOutputPojo = null;

            // Check if operator is used as input
            String tornadoInputTypeName = getTornadoVMTypeForPojoNameIfHomogeneous(pojoName, operatorInfo.inputList);
            if (tornadoInputTypeName != null) {
                for (int i = 0; i < operatorInfo.inputList.size(); i++) {
                    OperatorObject pojo = operatorInfo.inputList.get(i);
                    if (pojo.className.equals(pojoName)) {
                        try {
                            tornadoInputPojo = (OperatorObject) pojo.clone();
                        } catch (CloneNotSupportedException e) {
                            throw new RuntimeException(e);
                        }
                        tornadoInputPojo.className = tornadoInputTypeName;
                        operatorInfo.inputVariableNameToTornadoTypeMap.put(operatorInfo.argumentNameList.get(i), tornadoInputPojo);
                        operatorInfo.tornadifiedInputList.add(tornadoInputPojo);
                    }
                }
            }

            // Check if operator is used as output
            String tornadoOutputTypeName = getTornadoVMTypeForPojoNameIfHomogeneous(pojoName, operatorInfo.outputList);
            if (tornadoOutputTypeName != null) {

                for (int i = 0; i < operatorInfo.outputList.size(); i++) {
                    OperatorObject pojo = operatorInfo.outputList.get(i);
                    if (pojo.className.equals(pojoName)) {
                        try {
                            tornadoOutputPojo = (OperatorObject) pojo.clone();
                        } catch (CloneNotSupportedException e) {
                            throw new RuntimeException(e);
                        }
                        tornadoOutputPojo.className = tornadoOutputTypeName;
                        operatorInfo.tornadifiedOutputList.add(tornadoOutputPojo);
                    }
                }
            }

            // Replace the object in listOfOperatorObjectNames with the TornadoVM type
            if (tornadoInputPojo != null && tornadoOutputPojo != null) {
                assert (tornadoInputPojo.className.equals(tornadoOutputPojo.className));
                operatorInfo.listOfOperatorObjectNames.set((stringListIterator.nextIndex() - 1), tornadoInputPojo.className);
            } else if (tornadoInputPojo != null) {
                operatorInfo.listOfOperatorObjectNames.set((stringListIterator.nextIndex() - 1), tornadoInputPojo.className);
            } else if (tornadoOutputPojo != null) {
                operatorInfo.listOfOperatorObjectNames.set((stringListIterator.nextIndex() - 1), tornadoOutputPojo.className);
            }
        }
    }

    static String[] getUniqueOperatorName(OperatorInfo operatorInfo) {
        String[] distinctOperatorNames = Arrays.stream(operatorInfo.listOfOperatorObjectNames.toArray()).distinct().toArray(String[]::new);
        return distinctOperatorNames;
    }
}
