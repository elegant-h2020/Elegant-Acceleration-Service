package uk.ac.manchester.acceleration.service.elegant.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccelerationService {
    private Map<Long, CompilerRequest> requests = RequestDatabase.getRequests();

    public List<CompilerRequest> getAllRequests() {
        return new ArrayList<CompilerRequest>(requests.values());
    }

    public CompilerRequest getRequest(long id) {
        return requests.get(id);
    }

    public CompilerRequest addRequest(CompilerRequest request) {
        request.setId(requests.size() + 1);
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

    public CompilerRequest removeRequest(long id) {
        return requests.remove(id);
    }
}
