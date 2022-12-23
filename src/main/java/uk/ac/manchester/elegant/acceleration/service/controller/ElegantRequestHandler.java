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
package uk.ac.manchester.elegant.acceleration.service.controller;

import uk.ac.manchester.elegant.acceleration.service.tools.LinuxTornadoVM;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

//TODO Make methods static
public class ElegantRequestHandler {

    private static String fileGeneratedPath;
    private static Map<Long, CompilationRequest> requests = RequestDatabase.getRequests(); // TODO Connect with a database Spring MySQL, or other database, SQLLite

    // TODO Remove hashmaps and update the database functionality
    private static ConcurrentHashMap<Long, String> mapOfUploadedFunctionFileNames = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, String> mapOfUploadedDeviceJsonFileNames = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, String> mapOfUploadedFileInfoJsonFileNames = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, String> mapOfUploadedParameterSizeFileNames = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, String> mapOfGeneratedKernelNames = new ConcurrentHashMap<>();

    private static AtomicLong uid = new AtomicLong(0);

    public static long incrementAndGetUid() {
        return uid.incrementAndGet();
    }

    public String getFileGeneratedPath() {
        return ElegantRequestHandler.fileGeneratedPath;
    }

    public static void setFileGeneratedPath(String fileGeneratedPath) {
        ElegantRequestHandler.fileGeneratedPath = fileGeneratedPath;
    }

    public static List<CompilationRequest> getAllRequests() {
        return new ArrayList<CompilationRequest>(requests.values());
    }

    public static CompilationRequest getRequest(long id) {
        return requests.get(id);
    }

    public static String getFileNameOfAccelerationCode(long id) {
        CompilationRequest compilerRequest = requests.get(id);
        String functionName = compilerRequest.getFileInfo().getFunctionName();
        String suffix = "cl";
        String fileName = functionName + "." + suffix;
        return fileName;
    }

    public static String getGeneratedKernelFileName(long id) {
        return mapOfGeneratedKernelNames.get(id);
    }

    public static void removeKernelFileNameFromMap(long id) {
        mapOfGeneratedKernelNames.remove(id);
    }

    public static String getUploadedFunctionFileName(long id) {
        return mapOfUploadedFunctionFileNames.get(id);
    }

    public static String getUploadedDeviceJsonFileName(long id) {
        return mapOfUploadedDeviceJsonFileNames.get(id);
    }

    public static String getUploadedFileInfoJsonFileName(long id) {
        return mapOfUploadedFileInfoJsonFileNames.get(id);
    }

    public static String getUploadedParameterSizeJsonFileName(long id) {
        return mapOfUploadedParameterSizeFileNames.get(id);
    }

    public static CompilationRequest addRequest(CompilationRequest request) {
        request.setState(CompilationRequest.State.INITIAL);
        requests.put(request.getId(), request);

        return request;
    }

    public static CompilationRequest updateRequest(CompilationRequest request) {
        if (request.getId() <= 0) {
            return null;
        }
        requests.put(request.getId(), request);
        return request;
    }

    public static void addOrUpdateUploadedFunctionFileName(CompilationRequest request, String functionFileName) {
        mapOfUploadedFunctionFileNames.put(request.getId(), functionFileName);
    }

    public static void addOrUpdateUploadedDeviceJsonFileName(CompilationRequest request, String jsonFileName) {
        mapOfUploadedDeviceJsonFileNames.put(request.getId(), jsonFileName);
    }

    public static void addOrUpdateUploadedFileInfoFileName(CompilationRequest request, String jsonFileName) {
        mapOfUploadedFileInfoJsonFileNames.put(request.getId(), jsonFileName);
    }

    public static void addOrUpdateUploadedParameterSizeJsonFileName(CompilationRequest request, String jsonFileName) {
        mapOfUploadedParameterSizeFileNames.put(request.getId(), jsonFileName);
    }

    public static CompilationRequest removeRequest(long id) {
        return requests.remove(id);
    }

    public static void removeUploadedFileNames(long id) {
        mapOfUploadedFunctionFileNames.remove(id);
        mapOfUploadedDeviceJsonFileNames.remove(id);
        mapOfUploadedParameterSizeFileNames.remove(id);
        mapOfUploadedFileInfoJsonFileNames.remove(id);
    }

    // TODO: Update with invocation to the integrated compilers
    public static void compile(LinuxTornadoVM tornadoVM, TransactionMetaData transactionMetaData) throws IOException, InterruptedException {
        CompilationRequest compilerRequest = transactionMetaData.getCompilationRequest();
        File idDirectory = new File(fileGeneratedPath + File.separator + compilerRequest.getId());
        if (!idDirectory.exists()) {
            idDirectory.mkdirs();
        }
        mapOfGeneratedKernelNames.put(compilerRequest.getId(),
                fileGeneratedPath + File.separator + compilerRequest.getId() + File.separator + getFileNameOfAccelerationCode(transactionMetaData.getCompilationRequest().getId()));
        String methodFileName = mapOfUploadedFunctionFileNames.get(compilerRequest.getId());
        String deviceDescriptionJsonFileName = mapOfUploadedDeviceJsonFileNames.get(compilerRequest.getId());
        String parameterSizeJsonFileName = mapOfUploadedParameterSizeFileNames.get(compilerRequest.getId());
        String generatedKernelFileName = mapOfGeneratedKernelNames.get(compilerRequest.getId());
        tornadoVM.compileToBytecode(compilerRequest.getId(), methodFileName);

        tornadoVM.compileBytecodeToOpenCL(compilerRequest.getId(), methodFileName, deviceDescriptionJsonFileName, parameterSizeJsonFileName, generatedKernelFileName);

        if (tornadoVM.getExitCode() == 0) {
            transactionMetaData.getCompilationRequest().setState(CompilationRequest.State.COMPLETED);
        } else {
            transactionMetaData.getCompilationRequest().setState(CompilationRequest.State.FAILED);
        }
        transactionMetaData.response = Response//
                .status(Response.Status.ACCEPTED)//
                .type(MediaType.TEXT_PLAIN_TYPE)//
                .entity("New code acceleration request has been registered (#" + transactionMetaData.getCompilationRequest().getId() + ")\n")//
                .build();
    }
}