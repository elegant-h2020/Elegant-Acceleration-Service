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

import org.json.simple.parser.JSONParser;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONObject;
import uk.ac.manchester.acceleration.service.elegant.controller.AccelerationService;
import uk.ac.manchester.acceleration.service.elegant.controller.CompilerRequest;
import uk.ac.manchester.acceleration.service.elegant.controller.DeviceInfo;
import uk.ac.manchester.acceleration.service.elegant.controller.FileInfo;
import uk.ac.manchester.acceleration.service.elegant.controller.MaxWorkItems;

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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

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

    private CompilerRequest parseJsonFileToCompilerRequest(String fileName) {
        System.out.println("---Parsing DeviceInfo file: " + fileName);
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(fileName));
            JSONObject jsonObject = (JSONObject) obj;
            final String[] functionName = new String[1];
            final String[] programmingLanguage = new String[1];
            final String[] deviceName = new String[1];
            final boolean[] doubleFPSupport = new boolean[1];
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
            final MaxWorkItems[] maxWorkItems = { new MaxWorkItems() };
            Map<Object, Object> deviceInfoMap = (Map<Object, Object>) (jsonObject.get("deviceInfo"));
            deviceInfoMap.forEach((key, value) -> {
                switch ((String) key) {
                    case "deviceName":
                        deviceName[0] = (String) value;
                        break;
                    case "doubleFPSupport":
                        doubleFPSupport[0] = (boolean) value;
                        break;
                    case "maxWorkItems":
                        Map<Object, Object> maxWorkItemsMap = (Map<Object, Object>) ((JSONObject) value);
                        maxWorkItemsMap.forEach((keyMaxWorkItems, valueMaxWorkItems) -> {
                            switch ((String) keyMaxWorkItems) {
                                case "dim1":
                                    maxWorkItems[0].setDim1(((Long) valueMaxWorkItems).intValue());
                                    break;
                                case "dim2":
                                    maxWorkItems[0].setDim2(((Long) valueMaxWorkItems).intValue());
                                    break;
                                case "dim3":
                                    maxWorkItems[0].setDim3(((Long) valueMaxWorkItems).intValue());
                                    break;
                                default:
                                    break;
                            }
                        });
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
            DeviceInfo deviceInfo = new DeviceInfo(deviceName[0], doubleFPSupport[0], maxWorkItems[0], deviceAddressBits[0], deviceType[0], deviceExtensions[0], availableProcessors[0]);

            // Compose CompilerRequest
            CompilerRequest compilerRequest = new CompilerRequest(fileInfo, deviceInfo);
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

    private Response iterateAndParseUploadFilesFromRequest(HttpServletRequest request) {
        CompilerRequest compilerRequest = null;
        int code = 200;
        String msg = "Files uploaded successfully.";
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload fileUpload = new ServletFileUpload(factory);
        try {
            if (request != null) {
                FileItemIterator iter = fileUpload.getItemIterator(request);
                String fileName = null;
                boolean isJsonFileloaded = false;
                boolean isCodeFileLoaded = false;
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

                        try (InputStream stream = item.openStream()) {
                            writeInputStreamToFile(stream, file);
                        }
                        if (itemName.contains(".json")) {
                            compilerRequest = parseJsonFileToCompilerRequest(file.getAbsolutePath());
                            isJsonFileloaded = true;
                        } else if (itemName.contains(".java") || itemName.contains(".cpp") || itemName.contains(".c")) {
                            fileName = FILE_UPLOAD_PATH + File.separator + itemName;
                            isCodeFileLoaded = true;
                        }
                    }
                }
                if (isJsonFileloaded && isCodeFileLoaded) {
                    accelerationService.addRequest(compilerRequest);
                    accelerationService.addOrUpdateUploadedFileName(compilerRequest.getId(), fileName);
                    msg += " Request id: " + compilerRequest.getId();
                } else {
                    msg = "Files are not loaded correctly.";
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
        return Response.status(code).entity(msg).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/submit")
    public Response uploadFile(@Context HttpServletRequest request) {
        Response response = null;
        if (ServletFileUpload.isMultipartContent(request)) {
            response = iterateAndParseUploadFilesFromRequest(request);
        }
        return response;
    }

    @PUT
    @Path("/{requestId}/resubmit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest updateAndCompile(@PathParam("requestId") long requestId, CompilerRequest request) {
        request.setId(requestId);
        accelerationService.addOrUpdateUploadedFileName(request.getId(), null); // TODO Add file in PUT requests.
        return accelerationService.updateRequest(request);
    }

    @DELETE
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest delete(@PathParam("requestId") long requestId) {
        accelerationService.removeUploadedFileName(requestId);
        return accelerationService.removeRequest(requestId);
    }
}