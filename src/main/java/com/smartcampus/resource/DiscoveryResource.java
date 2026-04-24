package com.smartcampus.resource;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    @GET
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("api", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0.0");
        response.put("contact", "admin@smartcampus.ac.uk");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        response.put("resources", resources);
        return Response.ok(response).build();
    }
}
