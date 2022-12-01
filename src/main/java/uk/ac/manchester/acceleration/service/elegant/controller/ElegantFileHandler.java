/*
 * This file is part of the ELEGANT Acceleration Service.
 * URL: https://github.com/elegant-h2020/Elegant-Acceleration-Service.git
 *
 * Copyright (c) 2022, APT Group, Department of Computer Science,
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
package uk.ac.manchester.acceleration.service.elegant.controller;

import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class ElegantFileHandler {

    /**
     * The default buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private static final String FILE_UPLOAD_PATH = "/home/thanos/repositories/Elegant-Acceleration-Service/examples/uploaded";

    private static CompilationRequest parseJsonFileToCompilationRequest(String fileName) {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(fileName));
            JSONObject jsonObject = (JSONObject) obj;
            final String[] functionName = new String[1];
            final String[] programmingLanguage = new String[1];
            final String[] deviceName = new String[1];
            final boolean[] doubleFPSupport = new boolean[1];
            final long[] maxWorkItemSizes = new long[3];
            final int[] deviceAddressBits = new int[1];
            final String[] deviceType = new String[1];
            final String[] deviceExtensions = new String[1];
            final int[] availableProcessors = new int[1];

            // Reconstruct FileInfo
            Map<Object, Object> fileInfoMap = (Map<Object, Object>) (jsonObject.get("fileInfo"));
            fileInfoMap.forEach((key, value) -> {
                switch ((String) key) {
                    case "functionName":
                        functionName[0] = (String) value;
                        break;
                    case "programmingLanguage":
                        programmingLanguage[0] = (String) value;
                        break;
                    default:
                        break;
                }
            });
            FileInfo fileInfo = new FileInfo(functionName[0], programmingLanguage[0]);

            // Reconstruct DeviceInfo
            Map<Object, Object> deviceInfoMap = (Map<Object, Object>) (jsonObject.get("deviceInfo"));
            deviceInfoMap.forEach((key, value) -> {
                switch ((String) key) {
                    case "deviceName":
                        deviceName[0] = (String) value;
                        break;
                    case "doubleFPSupport":
                        doubleFPSupport[0] = (boolean) value;
                        break;
                    case "maxWorkItemSizes":
                        for (int i = 0; i < maxWorkItemSizes.length; i++) {
                            maxWorkItemSizes[i] = (long) ((JSONArray) value).get(i);
                        }
                        break;
                    case "deviceAddressBits":
                        deviceAddressBits[0] = ((Long) value).intValue();
                        break;
                    case "deviceType":
                        deviceType[0] = (String) value;
                        break;
                    case "deviceExtensions":
                        deviceExtensions[0] = (String) value;
                        break;
                    case "availableProcessors":
                        availableProcessors[0] = ((Long) value).intValue();
                        break;
                    default:
                        break;
                }
            });
            DeviceInfo deviceInfo = new DeviceInfo(deviceName[0], doubleFPSupport[0], maxWorkItemSizes, deviceAddressBits[0], deviceType[0], deviceExtensions[0], availableProcessors[0]);

            // Reconstruct ParameterInfo
            Map<Object, Object> parameterInfoMap = (Map<Object, Object>) (jsonObject.get("parameterSizes"));
            final String[] keys = new String[parameterInfoMap.size()];
            final int[] values = new int[parameterInfoMap.size()];
            final int[] itemIndex = { 0 };

            parameterInfoMap.forEach((key, value) -> {
                keys[itemIndex[0]] = (String) key;
                values[itemIndex[0]] = ((Long) value).intValue();
                itemIndex[0]++;
            });
            ParameterInfo parameterInfo = new ParameterInfo(keys, values);

            // Compose CompilerRequest
            CompilationRequest compilerRequest = new CompilationRequest(fileInfo, deviceInfo, parameterInfo);
            return compilerRequest;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }

    private static String resolveUploadedDirectory(long id) {
        File idDirectory = new File(FILE_UPLOAD_PATH + File.separator + id);
        if (!idDirectory.exists()) {
            idDirectory.mkdirs();
        }
        return idDirectory.getAbsolutePath();
    }

    private static void writeStringToFile(String string, String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateInternalJsonFiles(TransactionMetaData transactionMetaData) {
        String uploadedDirectory = resolveUploadedDirectory(transactionMetaData.getCompilationRequest().getId());

        String deviceJson = JsonGenerator.createJsonContents(transactionMetaData.getCompilationRequest().getDeviceInfo());
        String deviceFileName = uploadedDirectory + "/deviceInfo.json";

        String fileInfoJson = JsonGenerator.createJsonContents(transactionMetaData.getCompilationRequest().getFileInfo());
        String fileInfoFileName = uploadedDirectory + "/fileInfo.json";

        String parameterInfoJson = JsonGenerator.createJsonContents(transactionMetaData.getCompilationRequest().getParameterInfo());
        String parameterInfoFileName = uploadedDirectory + "/parameterInfo.json";

        writeStringToFile(deviceJson, deviceFileName);
        writeStringToFile(fileInfoJson, fileInfoFileName);
        writeStringToFile(parameterInfoJson, parameterInfoFileName);

        cleanupInputFiles(transactionMetaData);
        transactionMetaData.setJsonFileName(deviceFileName);
        transactionMetaData.setParameterSizeFileName(parameterInfoFileName);
        transactionMetaData.setFileInfoName(fileInfoFileName);
    }

    public static void cleanupInputFiles(TransactionMetaData transactionMetaData) {
        String sourceJsonFileName = transactionMetaData.getJsonFileName();
        String sourceFunctionFileName = transactionMetaData.getFunctionFileName();

        try {
            Files.deleteIfExists(Paths.get(sourceJsonFileName));
            transferFunctionFileToUploadedDirectory(sourceFunctionFileName, transactionMetaData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void transferFunctionFileToUploadedDirectory(String sourceFile, TransactionMetaData transactionMetaData) {
        String targetFunctionFileName = null;
        String targetDirectory = resolveUploadedDirectory(transactionMetaData.getCompilationRequest().getId());

        int functionSubstringLength = sourceFile.split("\\/").length;
        targetFunctionFileName = targetDirectory + File.separator + sourceFile.split("\\/")[functionSubstringLength - 1];
        Path sourceFunctionPath = Paths.get(sourceFile);
        Path targetFunctionPath = Paths.get(targetFunctionFileName);

        try {
            Files.move(sourceFunctionPath, targetFunctionPath, StandardCopyOption.REPLACE_EXISTING);
            transactionMetaData.setFunctionFileName(targetFunctionFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TransactionMetaData iterateAndParseUploadFilesFromRequest(HttpServletRequest request) {
        CompilationRequest compilationRequest = null;
        int code = 200;
        String msg = "Files uploaded.";
        String functionFileName = null;
        String jsonFileName = null;
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload fileUpload = new ServletFileUpload(factory);
        try {
            if (request != null) {
                FileItemIterator iter = fileUpload.getItemIterator(request);
                while (iter.hasNext()) {
                    final FileItemStream item = iter.next();
                    final String itemName = item.getName();
                    if (!item.isFormField()) {
                        final File file = new File(FILE_UPLOAD_PATH + File.separator + itemName);
                        File dir = file.getParentFile();
                        if (!dir.exists()) {
                            dir.mkdir();
                        }

                        // TODO: Append date in the name of new files
                        if (file.exists()) {
                            file.delete();
                            file.createNewFile();
                        }

                        try (InputStream stream = item.openStream()) {
                            writeInputStreamToFile(stream, file);
                        }
                        if (itemName.contains(".json")) {
                            compilationRequest = parseJsonFileToCompilationRequest(file.getAbsolutePath());
                            jsonFileName = FILE_UPLOAD_PATH + File.separator + itemName;
                        } else if (itemName.contains(".java") || itemName.contains(".cpp") || itemName.contains(".c")) {
                            functionFileName = FILE_UPLOAD_PATH + File.separator + itemName;
                        }
                    }
                }
            }
        } catch (FileUploadException e) {
            code = 404;
            msg = e.getMessage();
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            code = 404;
            msg = e.getMessage();
        }
        TransactionMetaData transactionMetaData = new TransactionMetaData(compilationRequest, functionFileName, jsonFileName, null, Response.status(code).entity(msg).build());
        return transactionMetaData;
    }

    public static void removeFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void removeParentDirectoryOfFile(String fileName) {
        File file = new File(fileName).getParentFile();
        if (file.exists()) {
            file.delete();
        }
    }
}
