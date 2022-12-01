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
import uk.ac.manchester.acceleration.service.elegant.controller.CompilationRequest;
import uk.ac.manchester.acceleration.service.elegant.controller.ElegantFileHandler;
import uk.ac.manchester.acceleration.service.elegant.controller.EnvironmentVariables;
import uk.ac.manchester.acceleration.service.elegant.controller.TransactionMetaData;
import uk.ac.manchester.acceleration.service.elegant.tools.LinuxTornadoVM;

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
import java.io.IOException;
import java.util.List;

@Path("/acceleration/requests")
public class ElegantAccelerationService {

    private static LinuxTornadoVM tornadoVM;

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public ElegantAccelerationService() throws IOException {
        newTornadoVMInstance();
        // tornadoVM.buildTornadoVM();
    }

    private void newTornadoVMInstance() {
        if (OS.contains("linux")) {
            tornadoVM = new LinuxTornadoVM();
            ElegantRequestHandler.setFileGeneratedPath(tornadoVM.getEnvironmentVariable(EnvironmentVariables.GENERATED_KERNELS_DIR));
        } else {
            throw new UnsupportedOperationException("The Acceleration Service is not supported for " + OS + ".");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<CompilationRequest> retrieveRequests() {
        return ElegantRequestHandler.getAllRequests();
    }

    @GET
    @Path("/{requestId}/info")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilationRequest retrieveRequest(@PathParam("requestId") long requestId) {
        System.out.println("The generated kernelFileName for request [" + requestId + "] of code is: " + ElegantRequestHandler.getGeneratedKernelFileName(requestId));
        System.out.println("The uploaded jsonFileName for request [" + requestId + "] of code is: " + ElegantRequestHandler.getUploadedJsonFileName(requestId));
        return ElegantRequestHandler.getRequest(requestId); // TODO Avoid creating an object. Use the class name and make methods static.
    }

    @GET
    @Path("/{requestId}/retrieve")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("requestId") long requestId) throws Exception {
        if (ElegantRequestHandler.getGeneratedKernelFileName(requestId) != null) {
            File fileDownload = new File(ElegantRequestHandler.getGeneratedKernelFileName(requestId));
            Response.ResponseBuilder response = Response.ok((Object) fileDownload);
            response.header("Content-Disposition", "attachment;filename=" + ElegantRequestHandler.getFileNameOfAccelerationCode(requestId));
            return response.build();
        } else {
            return Response.status(404).entity("File not found.").build();
        }
    }

    @GET
    @Path("/{requestId}/state")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilationRequest.State retrieveRequestState(@PathParam("requestId") long requestId) {
        if (ElegantRequestHandler.getRequest(requestId) != null) {
            return ElegantRequestHandler.getRequest(requestId).getState();
        } else {
            return null;
        }
    }

    @GET
    @Path("/count")
    @Produces("text/plain")
    public String getCount() {
        return String.valueOf(ElegantRequestHandler.getAllRequests().size()); // kernelMap.size()
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/submit")
    public Response uploadFile(@Context HttpServletRequest request) throws IOException, InterruptedException {
        TransactionMetaData transactionMetaData = null;
        if (ServletFileUpload.isMultipartContent(request)) {
            transactionMetaData = ElegantFileHandler.iterateAndParseUploadFilesFromRequest(request);
            if (transactionMetaData.getCompilationRequest() != null) {
                long uid = ElegantRequestHandler.incrementAndGetUid();
                transactionMetaData.getCompilationRequest().setId(uid);
                ElegantFileHandler.generateInternalJsonFiles(transactionMetaData);
                ElegantRequestHandler.addRequest(transactionMetaData.getCompilationRequest());
                ElegantRequestHandler.addOrUpdateUploadedFunctionFileName(transactionMetaData.getCompilationRequest(), transactionMetaData.getFunctionFileName());
                ElegantRequestHandler.addOrUpdateUploadedDeviceJsonFileName(transactionMetaData.getCompilationRequest(), transactionMetaData.getJsonFileName());
                ElegantRequestHandler.compile(tornadoVM, transactionMetaData);
            }
        }
        return transactionMetaData.response;
    }

    @POST
    @Path("/{requestId}/resubmit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateAndCompile(@PathParam("requestId") long requestId, @Context HttpServletRequest request) throws IOException, InterruptedException {
        TransactionMetaData transactionMetaData = null;
        if (ServletFileUpload.isMultipartContent(request)) {
            ElegantFileHandler.removeFile(ElegantRequestHandler.getUploadedFunctionFileName(requestId));
            ElegantRequestHandler.removeKernelFileNameFromMap(requestId);
            ElegantFileHandler.removeFile(ElegantRequestHandler.getUploadedJsonFileName(requestId));
            transactionMetaData = ElegantFileHandler.iterateAndParseUploadFilesFromRequest(request);
            transactionMetaData.getCompilationRequest().setId(requestId);
            ElegantRequestHandler.updateRequest(transactionMetaData.getCompilationRequest());
            ElegantRequestHandler.addOrUpdateUploadedFunctionFileName(transactionMetaData.getCompilationRequest(), transactionMetaData.getFunctionFileName());
            ElegantRequestHandler.addOrUpdateUploadedDeviceJsonFileName(transactionMetaData.getCompilationRequest(), transactionMetaData.getJsonFileName());
            ElegantRequestHandler.compile(tornadoVM, transactionMetaData);
        }
        return transactionMetaData.response;
    }

    @DELETE
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilationRequest delete(@PathParam("requestId") long requestId) {
        ElegantFileHandler.removeFile(ElegantRequestHandler.getUploadedFunctionFileName(requestId));
        ElegantRequestHandler.removeKernelFileNameFromMap(requestId);
        ElegantFileHandler.removeFile(ElegantRequestHandler.getUploadedJsonFileName(requestId));
        ElegantRequestHandler.removeUploadedFunctionFileName(requestId);
        ElegantRequestHandler.removeUploadedJsonFileName(requestId);
        return ElegantRequestHandler.removeRequest(requestId);
    }
}