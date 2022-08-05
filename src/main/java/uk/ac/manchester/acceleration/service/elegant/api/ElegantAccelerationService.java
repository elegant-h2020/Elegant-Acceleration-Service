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

import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import uk.ac.manchester.acceleration.service.elegant.controller.AccelerationService;
import uk.ac.manchester.acceleration.service.elegant.controller.CompilerRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Path("/acceleration/requests")
public class ElegantAccelerationService {

    /**
     * The default buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    AccelerationService accelerationService = new AccelerationService();

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
        return accelerationService.getRequest(requestId);
    }

    @GET
    @Path("/{requestId}/state")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest.CompilationState retrieveRequestState(@PathParam("requestId") long requestId) {
        return accelerationService.getRequest(requestId).getState();
    }

    @GET
    @Path("/count")
    @Produces("text/plain")
    public String getCount() {
        return String.valueOf(accelerationService.getAllRequests().size()); // kernelMap.size()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest compile(CompilerRequest request) {
        return accelerationService.addRequest(request);
    }

    private static final String FILE_UPLOAD_PATH = "/home/thanos/repositories/Elegant-Acceleration-Service/examples/uploaded/";

    private static void writeInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/submit")
    public Response uploadFile(@Context HttpServletRequest request) {
        String name = null;
        int code = 200;
        String msg = "Files uploaded successfully";
        if (ServletFileUpload.isMultipartContent(request)) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload fileUpload = new ServletFileUpload(factory);
            try {
                if (request != null) {
                    FileItemIterator iter = fileUpload.getItemIterator(request);
                    while (iter.hasNext()) {
                        final FileItemStream item = iter.next();
                        final String itemName = item.getName();
                        final String fieldName = item.getFieldName();
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
                            System.out.println("itemName: " + itemName + " - fieldName: " + fieldName);
                            System.out.println("Saving the file: " + file.getName());
                            try (InputStream stream = item.openStream()) {
                                writeInputStreamToFile(stream, file);
                            }
                        } // else {
                          // name = fieldValue;
                          // name = fieldName;
                          // System.out.println("Field Name: " + fieldName);// + ", Field Value: " +
                          // fieldValue);
                          // System.out.println("Candidate Name: " + name);
                          // }
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
        }
        return Response.status(code).entity(msg).build();
    }

    @PUT
    @Path("/{requestId}/resubmit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest updateAndCompile(@PathParam("requestId") long requestId, CompilerRequest request) {
        request.setId(requestId);
        return accelerationService.updateRequest(request);
    }

    @DELETE
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest delete(@PathParam("requestId") long requestId) {
        return accelerationService.removeRequest(requestId);
    }
}