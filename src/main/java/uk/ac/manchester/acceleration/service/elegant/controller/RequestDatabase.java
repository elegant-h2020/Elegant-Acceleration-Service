package uk.ac.manchester.acceleration.service.elegant.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestDatabase {
    //TODO: investigate concurrency issues
    private static ConcurrentHashMap<Long, CompilerRequest> requests = new ConcurrentHashMap<>();

    public static Map<Long, CompilerRequest> getRequests() {
        return requests;
    }
}
