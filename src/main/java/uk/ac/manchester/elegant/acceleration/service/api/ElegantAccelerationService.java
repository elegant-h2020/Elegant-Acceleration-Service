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
package uk.ac.manchester.elegant.acceleration.service.api;

import jakarta.ws.rs.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import uk.ac.manchester.elegant.acceleration.service.controller.ElegantRequestHandler;
import uk.ac.manchester.elegant.acceleration.service.controller.CompilationRequest;
import uk.ac.manchester.elegant.acceleration.service.controller.FileHandler;
import uk.ac.manchester.elegant.acceleration.service.controller.EnvironmentVariables;
import uk.ac.manchester.elegant.acceleration.service.controller.TransactionMetaData;
import uk.ac.manchester.elegant.acceleration.service.tools.LinuxTornadoVM;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Path("/acceleration")
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
            FileHandler.setFileUploadedPath(tornadoVM.getEnvironmentVariable(EnvironmentVariables.UPLOADED_DIR));
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
        System.out.println("The uploaded jsonFileName for request [" + requestId + "] of code is: " + ElegantRequestHandler.getUploadedDeviceJsonFileName(requestId));
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
            return Response.status(Response.Status.EXPECTATION_FAILED).entity("File not found.").build();
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
    public Response uploadFile(@FormDataParam("codeFile") InputStream codeFileInputStream,
                               @FormDataParam("codeFile") FormDataContentDisposition codeFileMetaData,
                               @FormDataParam("jsonFile") InputStream jsonFileInputStream,
                               @FormDataParam("jsonFile") FormDataContentDisposition jsonFileMetaData) throws IOException, InterruptedException {
        TransactionMetaData transactionMetaData = null;
        String msg = "Accepted request. Files uploaded.";

        String codeFileUploadedPath = FileHandler.uploadFile(codeFileInputStream, codeFileMetaData);
        String jsonFileUploadedPath = FileHandler.uploadFile(jsonFileInputStream, jsonFileMetaData);
        CompilationRequest compilationRequest = FileHandler.receiveRequest(jsonFileMetaData.getFileName());

        if (compilationRequest == null) {
            msg = "Problem with uploaded files. See server log for more detail.";
            transactionMetaData = new TransactionMetaData(compilationRequest, null, null, null, Response.status(Response.Status.BAD_REQUEST).entity(msg).build());
        } else {
            transactionMetaData = new TransactionMetaData(compilationRequest, codeFileUploadedPath, jsonFileUploadedPath, null, Response.status(Response.Status.ACCEPTED).entity(msg).build());
        }

        if (transactionMetaData.getCompilationRequest() != null) {
            long uid = ElegantRequestHandler.incrementAndGetUid();
            transactionMetaData.getCompilationRequest().setId(uid);
            FileHandler.generateInternalJsonFiles(transactionMetaData);
            ElegantRequestHandler.addRequest(transactionMetaData.getCompilationRequest());
            ElegantRequestHandler.addOrUpdateUploadedFunctionFileName(transactionMetaData.getCompilationRequest(), transactionMetaData.getFunctionFileName());
            ElegantRequestHandler.addOrUpdateUploadedDeviceJsonFileName(transactionMetaData.getCompilationRequest(), transactionMetaData.getJsonFileName());
            ElegantRequestHandler.addOrUpdateUploadedFileInfoFileName(transactionMetaData.getCompilationRequest(), transactionMetaData.getFileInfoName());
            ElegantRequestHandler.addOrUpdateUploadedParameterSizeJsonFileName(transactionMetaData.getCompilationRequest(), transactionMetaData.getParameterSizeFileName());
            ElegantRequestHandler.compile(tornadoVM, transactionMetaData);
        }

        return transactionMetaData.response;

    }

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{requestId}/resubmit")
    public Response updateAndCompile(@PathParam("requestId") long requestId,
                                     @FormDataParam("codeFile") InputStream codeFileInputStream,
                                     @FormDataParam("codeFile") FormDataContentDisposition codeFileMetaData,
                                     @FormDataParam("jsonFile") InputStream jsonFileInputStream,
                                     @FormDataParam("jsonFile") FormDataContentDisposition jsonFileMetaData) throws IOException, InterruptedException {
        TransactionMetaData transactionMetaData = null;
        String msg = "Accepted request. Files uploaded.";

        // Remove existing files related to requestId
        FileHandler.removeFile(ElegantRequestHandler.getUploadedFunctionFileName(requestId));
        FileHandler.removeFile(ElegantRequestHandler.getUploadedDeviceJsonFileName(requestId));
        FileHandler.removeFile(ElegantRequestHandler.getUploadedParameterSizeJsonFileName(requestId));
        FileHandler.removeFile(ElegantRequestHandler.getUploadedFileInfoJsonFileName(requestId));
        FileHandler.removeFile(ElegantRequestHandler.getGeneratedKernelFileName(requestId));
        ElegantRequestHandler.removeKernelFileNameFromMap(requestId);

        // Upload new files
        String codeFileUploadedPath = FileHandler.uploadFile(codeFileInputStream, codeFileMetaData);
        String jsonFileUploadedPath = FileHandler.uploadFile(jsonFileInputStream, jsonFileMetaData);
        CompilationRequest compilationRequest = FileHandler.receiveRequest(jsonFileMetaData.getFileName());

        if (compilationRequest == null) {
            msg = "Problem with uploaded files. See server log for more detail.";
            transactionMetaData = new TransactionMetaData(compilationRequest, null, null, null, Response.status(Response.Status.BAD_REQUEST).entity(msg).build());
        } else {
            transactionMetaData = new TransactionMetaData(compilationRequest, codeFileUploadedPath, jsonFileUploadedPath, null, Response.status(Response.Status.ACCEPTED).entity(msg).build());

            // Update and trigger execution of request
            transactionMetaData.getCompilationRequest().setId(requestId);
            FileHandler.generateInternalJsonFiles(transactionMetaData);
            ElegantRequestHandler.updateRequest(transactionMetaData.getCompilationRequest());
            ElegantRequestHandler.addOrUpdateUploadedFunctionFileName(transactionMetaData.getCompilationRequest(), transactionMetaData.getFunctionFileName());
            ElegantRequestHandler.addOrUpdateUploadedDeviceJsonFileName(transactionMetaData.getCompilationRequest(), transactionMetaData.getJsonFileName());
            ElegantRequestHandler.addOrUpdateUploadedParameterSizeJsonFileName(transactionMetaData.getCompilationRequest(), transactionMetaData.getParameterSizeFileName());
            ElegantRequestHandler.compile(tornadoVM, transactionMetaData);
        }

        return transactionMetaData.response;
    }

    @DELETE
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilationRequest delete(@PathParam("requestId") long requestId) {
        FileHandler.removeFile(ElegantRequestHandler.getUploadedFunctionFileName(requestId));
        FileHandler.removeFile(ElegantRequestHandler.getUploadedDeviceJsonFileName(requestId));
        FileHandler.removeFile(ElegantRequestHandler.getUploadedParameterSizeJsonFileName(requestId));
        FileHandler.removeFile(ElegantRequestHandler.getUploadedFileInfoJsonFileName(requestId));
        FileHandler.removeFile(ElegantRequestHandler.getGeneratedKernelFileName(requestId));
        FileHandler.removeParentDirectoryOfFile(ElegantRequestHandler.getUploadedParameterSizeJsonFileName(requestId));
        FileHandler.removeParentDirectoryOfFile(ElegantRequestHandler.getGeneratedKernelFileName(requestId));

        ElegantRequestHandler.removeKernelFileNameFromMap(requestId);
        ElegantRequestHandler.removeUploadedFileNames(requestId);
        return ElegantRequestHandler.removeRequest(requestId);
    }
}