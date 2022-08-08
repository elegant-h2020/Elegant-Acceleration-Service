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

import javax.ws.rs.core.Response;

public class TransactionMetaData {
    private CompilerRequest compilerRequest;
    private String functionFileName;
    private String jsonFileName;

    public Response response;

    public TransactionMetaData(CompilerRequest compilerRequest, String functionFileName, String jsonFileName, Response response) {
        this.compilerRequest = compilerRequest;

        this.functionFileName = functionFileName;
        this.jsonFileName = jsonFileName;
        this.response = response;
    }

    public CompilerRequest getCompilerRequest() {
        return compilerRequest;
    }

    public void setCompilerRequest(CompilerRequest compilerRequest) {
        this.compilerRequest = compilerRequest;
    }

    public String getFunctionFileName() {
        return functionFileName;
    }

    public void setFunctionFileName(String functionFileName) {
        this.functionFileName = functionFileName;
    }

    public String getJsonFileName() {
        return jsonFileName;
    }

    public void setJsonFileName(String jsonFileName) {
        this.jsonFileName = jsonFileName;
    }
}
