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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ElegantRequestHandler {
    private Map<Long, CompilerRequest> requests = RequestDatabase.getRequests();

    private static ConcurrentHashMap<Long, String> mapOfUploadedFunctionFileNames = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, String> mapOfUploadedJsonFileNames = new ConcurrentHashMap<>();

    private static long uid = 0;

    public long getUid() {
        return ++uid;
    }

    public List<CompilerRequest> getAllRequests() {
        return new ArrayList<CompilerRequest>(requests.values());
    }

    public CompilerRequest getRequest(long id) {
        return requests.get(id);
    }

    public String getUploadedFunctionFileName(long id) {
        return mapOfUploadedFunctionFileNames.get(id);
    }

    public String getUploadedJsonFileName(long id) {
        return mapOfUploadedJsonFileNames.get(id);
    }

    public CompilerRequest addRequest(CompilerRequest request) {
        request.setState(CompilerRequest.CompilationState.INITIAL);
        requests.put(request.getId(), request);

        return request;
    }

    public CompilerRequest updateRequest(CompilerRequest request) {
        if (request.getId() <= 0) {
            return null;
        }
        requests.put(request.getId(), request);
        return request;
    }

    public void addOrUpdateUploadedFunctionFileName(CompilerRequest request, String functionFileName) {
        System.out.println("Add " + functionFileName + " - for id: " + request.getId());
        mapOfUploadedFunctionFileNames.put(request.getId(), functionFileName);
    }

    public void addOrUpdateUploadedJsonFileName(CompilerRequest request, String jsonFileName) {
        System.out.println("Add Json " + jsonFileName + " - for id: " + request.getId());
        mapOfUploadedJsonFileNames.put(request.getId(), jsonFileName);
    }

    public CompilerRequest removeRequest(long id) {
        return requests.remove(id);
    }

    public void removeUploadedFunctionFileName(long id) {
        mapOfUploadedFunctionFileNames.remove(id);
    }

    public void removeUploadedJsonFileName(long id) {
        mapOfUploadedJsonFileNames.remove(id);
    }
}
