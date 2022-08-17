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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

//TODO Make methods static
public class ElegantRequestHandler {
    private static final String FILE_GENERATED_PATH = "/home/thanos/repositories/Elegant-Acceleration-Service/examples/generated";
    private static Map<Long, CompilationRequest> requests = RequestDatabase.getRequests(); // TODO Connect with a database Spring MySQL, or other database, SQLLite

    // TODO Remove hashmaps and update the database functionality
    private static ConcurrentHashMap<Long, String> mapOfUploadedFunctionFileNames = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, String> mapOfUploadedJsonFileNames = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, String> mapOfGeneratedKernelNames = new ConcurrentHashMap<>();

    private static AtomicLong uid = new AtomicLong(0);

    public static long getUid() { // TODO GenerateID
        return uid.incrementAndGet();
    }

    public static List<CompilationRequest> getAllRequests() {
        return new ArrayList<CompilationRequest>(requests.values());
    }

    public static CompilationRequest getRequest(long id) {
        return requests.get(id);
    }

    public static String getFileNameOfAccelerationCode(long id) {
        CompilationRequest compilerRequest = requests.get(id);
        String functionName = compilerRequest.getFileInfo().getFunctionName() + "-" + id;
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

    public static String getUploadedJsonFileName(long id) {
        return mapOfUploadedJsonFileNames.get(id);
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
        System.out.println("Add " + functionFileName + " - for id: " + request.getId());
        mapOfUploadedFunctionFileNames.put(request.getId(), functionFileName);
    }

    public static void addOrUpdateUploadedJsonFileName(CompilationRequest request, String jsonFileName) {
        System.out.println("Add Json " + jsonFileName + " - for id: " + request.getId());
        mapOfUploadedJsonFileNames.put(request.getId(), jsonFileName);
    }

    public static CompilationRequest removeRequest(long id) {
        return requests.remove(id);
    }

    public static void removeUploadedFunctionFileName(long id) {
        mapOfUploadedFunctionFileNames.remove(id);
    }

    public static void removeUploadedJsonFileName(long id) {
        mapOfUploadedJsonFileNames.remove(id);
    }

    // TODO: Update with invocation to the integrated compilers
    public static void compile(TransactionMetaData transactionMetaData) {
        CompilationRequest compilerRequest = transactionMetaData.getCompilationRequest();
        mapOfGeneratedKernelNames.put(compilerRequest.getId(), FILE_GENERATED_PATH + File.separator + getFileNameOfAccelerationCode(transactionMetaData.getCompilationRequest().getId()));
        transactionMetaData.getCompilationRequest().setState(CompilationRequest.State.SUBMITTED);
    }
}
