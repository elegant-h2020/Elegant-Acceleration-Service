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
import uk.ac.manchester.acceleration.service.elegant.controller.ElegantRequestHandler;
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
import java.io.File;
import java.util.List;

@Path("/acceleration/requests")
public class ElegantAccelerationService {

    /**
     * The default buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    ElegantRequestHandler elegantRequestHandler = new ElegantRequestHandler();
    ElegantFileHandler elegantFileHandler = new ElegantFileHandler();

    public ElegantAccelerationService() {

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CompilerRequest> retrieveRequests() {
        return elegantRequestHandler.getAllRequests();
    }

    @GET
    @Path("/{requestId}/info")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest retrieveRequest(@PathParam("requestId") long requestId) {
        System.out.println("The uploaded functionFileName for request [" + requestId + "] of code is: " + elegantRequestHandler.getUploadedFunctionFileName(requestId));
        System.out.println("The uploaded jsonFileName for request [" + requestId + "] of code is: " + elegantRequestHandler.getUploadedJsonFileName(requestId));
        return elegantRequestHandler.getRequest(requestId);
    }

    @GET
    @Path("/{requestId}/retrieve")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("requestId") long requestId) throws Exception {
        if (elegantRequestHandler.getUploadedFunctionFileName(requestId) != null) {
            File fileDownload = new File(elegantRequestHandler.getUploadedFunctionFileName(requestId));
            Response.ResponseBuilder response = Response.ok((Object) fileDownload);
            response.header("Content-Disposition", "attachment;filename=" + elegantRequestHandler.getFileNameOfAccelerationCode(requestId));
            return response.build();
        } else {
            return Response.status(404).entity("File not found.").build();
        }
    }

    @GET
    @Path("/{requestId}/state")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest.CompilationState retrieveRequestState(@PathParam("requestId") long requestId) {
        if (elegantRequestHandler.getRequest(requestId) != null) {
            return elegantRequestHandler.getRequest(requestId).getState();
        } else {
            return null;
        }
    }

    @GET
    @Path("/count")
    @Produces("text/plain")
    public String getCount() {
        return String.valueOf(elegantRequestHandler.getAllRequests().size()); // kernelMap.size()
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/submit")
    public Response uploadFile(@Context HttpServletRequest request) {
        TransactionMetaData transactionMetaData = null;
        if (ServletFileUpload.isMultipartContent(request)) {
            transactionMetaData = elegantFileHandler.iterateAndParseUploadFilesFromRequest(request);
            long uid = elegantRequestHandler.getUid();
            transactionMetaData.getCompilerRequest().setId(uid);
            elegantRequestHandler.addRequest(transactionMetaData.getCompilerRequest());
            elegantRequestHandler.addOrUpdateUploadedFunctionFileName(transactionMetaData.getCompilerRequest(), transactionMetaData.getFunctionFileName());
            elegantRequestHandler.addOrUpdateUploadedJsonFileName(transactionMetaData.getCompilerRequest(), transactionMetaData.getJsonFileName());
        }
        return transactionMetaData.response;
    }

    @POST
    @Path("/{requestId}/resubmit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateAndCompile(@PathParam("requestId") long requestId, @Context HttpServletRequest request) {
        TransactionMetaData transactionMetaData = null;
        if (ServletFileUpload.isMultipartContent(request)) {
            elegantFileHandler.removeFile(elegantRequestHandler.getUploadedFunctionFileName(requestId));
            elegantFileHandler.removeFile(elegantRequestHandler.getUploadedJsonFileName(requestId));
            transactionMetaData = elegantFileHandler.iterateAndParseUploadFilesFromRequest(request);
            transactionMetaData.getCompilerRequest().setId(requestId);
            elegantRequestHandler.updateRequest(transactionMetaData.getCompilerRequest());
            elegantRequestHandler.addOrUpdateUploadedFunctionFileName(transactionMetaData.getCompilerRequest(), transactionMetaData.getFunctionFileName());
            elegantRequestHandler.addOrUpdateUploadedJsonFileName(transactionMetaData.getCompilerRequest(), transactionMetaData.getJsonFileName());
        }
        return transactionMetaData.response;
    }

    @DELETE
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest delete(@PathParam("requestId") long requestId) {
        elegantFileHandler.removeFile(elegantRequestHandler.getUploadedFunctionFileName(requestId));
        elegantFileHandler.removeFile(elegantRequestHandler.getUploadedJsonFileName(requestId));
        elegantRequestHandler.removeUploadedFunctionFileName(requestId);
        elegantRequestHandler.removeUploadedJsonFileName(requestId);
        return elegantRequestHandler.removeRequest(requestId);
    }
}