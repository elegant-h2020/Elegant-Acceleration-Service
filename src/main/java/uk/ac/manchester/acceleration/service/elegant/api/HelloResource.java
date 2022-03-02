package uk.ac.manchester.acceleration.service.elegant.api;

import uk.ac.manchester.acceleration.service.elegant.controller.AccelerationService;
import uk.ac.manchester.acceleration.service.elegant.controller.CompilerRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/acceleration/requests")
public class HelloResource {

    AccelerationService accelerationService = new AccelerationService();

    public HelloResource() {

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
    @Path("/count")
    @Produces("text/plain")
    public String getCount() {
        return String.valueOf(accelerationService.getAllRequests().size()); //kernelMap.size()
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest compile(CompilerRequest request)
    {
        return accelerationService.addRequest(request);
    }

    @PUT
    @Path("/{requestId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest updateAndCompile(@PathParam("requestId") long requestId, CompilerRequest request)
    {
        request.setId(requestId);
        return accelerationService.updateRequest(request);
    }

    @DELETE
    @Path("/{requestId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompilerRequest delete(@PathParam("requestId") long requestId)
    {
        return accelerationService.removeRequest(requestId);
    }
}