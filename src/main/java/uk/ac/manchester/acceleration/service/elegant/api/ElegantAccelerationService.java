/*
 * This file is part of the ELEGANT Acceleration Service.
 * URL: https://github.com/stratika/elegant-acceleration-service
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
package uk.ac.manchester.acceleration.service.elegant.api;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import uk.ac.manchester.acceleration.service.elegant.controller.AccelerationService;
import uk.ac.manchester.acceleration.service.elegant.controller.CompilerRequest;
import uk.ac.manchester.acceleration.service.elegant.controller.ElegantFileHandler;
import uk.ac.manchester.acceleration.service.elegant.controller.TransactionMetaData;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/acceleration/requests")
public class ElegantAccelerationService {

    /**
     * The default buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    AccelerationService accelerationService = new AccelerationService();
    ElegantFileHandler elegantFileHandler = new ElegantFileHandler();

    public ElegantAccelerationService() {

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CompilerRequest> retrieveRequests() {
        return accelerationService.getAllRequests();
    }

    @GET
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest retrieveRequest(@PathParam("requestId") long requestId) {
        System.out.println("The uploaded functionFileName for request [" + requestId + "] of code is: " + accelerationService.getUploadedFunctionFileName(requestId));
        System.out.println("The uploaded jsonFileName for request [" + requestId + "] of code is: " + accelerationService.getUploadedJsonFileName(requestId));
        return accelerationService.getRequest(requestId);
    }

    @GET
    @Path("/{requestId}/state")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest.CompilationState retrieveRequestState(@PathParam("requestId") long requestId) {
        if (accelerationService.getRequest(requestId) != null) {
            return accelerationService.getRequest(requestId).getState();
        } else {
            return null;
        }
    }

    @GET
    @Path("/count")
    @Produces("text/plain")
    public String getCount() {
        return String.valueOf(accelerationService.getAllRequests().size()); // kernelMap.size()
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/submit")
    public Response uploadFile(@Context HttpServletRequest request) {
        TransactionMetaData transactionMetaData = null;
        if (ServletFileUpload.isMultipartContent(request)) {
            transactionMetaData = elegantFileHandler.iterateAndParseUploadFilesFromRequest(request);
            long uid = accelerationService.getUid();
            transactionMetaData.getCompilerRequest().setId(uid);
            accelerationService.addRequest(transactionMetaData.getCompilerRequest());
            accelerationService.addOrUpdateUploadedFunctionFileName(transactionMetaData.getCompilerRequest(), transactionMetaData.getFunctionFileName());
            accelerationService.addOrUpdateUploadedJsonFileName(transactionMetaData.getCompilerRequest(), transactionMetaData.getJsonFileName());
        }
        return transactionMetaData.response;
    }

    @POST
    @Path("/{requestId}/resubmit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateAndCompile(@PathParam("requestId") long requestId, @Context HttpServletRequest request) {
        TransactionMetaData transactionMetaData = null;
        if (ServletFileUpload.isMultipartContent(request)) {
            elegantFileHandler.removeFile(accelerationService.getUploadedFunctionFileName(requestId));
            elegantFileHandler.removeFile(accelerationService.getUploadedJsonFileName(requestId));
            transactionMetaData = elegantFileHandler.iterateAndParseUploadFilesFromRequest(request);
            transactionMetaData.getCompilerRequest().setId(requestId);
            accelerationService.updateRequest(transactionMetaData.getCompilerRequest());
            accelerationService.addOrUpdateUploadedFunctionFileName(transactionMetaData.getCompilerRequest(), transactionMetaData.getFunctionFileName());
            accelerationService.addOrUpdateUploadedJsonFileName(transactionMetaData.getCompilerRequest(), transactionMetaData.getJsonFileName());
        }
        return transactionMetaData.response;
    }

    @DELETE
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest delete(@PathParam("requestId") long requestId) {
        elegantFileHandler.removeFile(accelerationService.getUploadedFunctionFileName(requestId));
        elegantFileHandler.removeFile(accelerationService.getUploadedJsonFileName(requestId));
        accelerationService.removeUploadedFunctionFileName(requestId);
        accelerationService.removeUploadedJsonFileName(requestId);
        return accelerationService.removeRequest(requestId);
    }
}