package com.smartcampus.resource;
import com.smartcampus.application.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    private final String sensorId;
    private final DataStore store = DataStore.INSTANCE;
    public SensorReadingResource(String sensorId) { this.sensorId = sensorId; }
    @GET
    public Response getReadings() {
        return Response.ok(store.getReadings(sensorId)).build();
    }
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensor(sensorId);
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus()))
            throw new SensorUnavailableException("Sensor '" + sensorId
                    + "' is currently under MAINTENANCE and cannot accept new readings.");
        if (reading == null)
            return Response.status(400).entity(Map.of("error", "Reading body is required.")).build();
        SensorReading newReading = new SensorReading(reading.getValue());
        if (reading.getTimestamp() > 0) newReading.setTimestamp(reading.getTimestamp());
        store.addReading(sensorId, newReading);
        sensor.setCurrentValue(newReading.getValue());
        return Response.status(201).entity(newReading).build();
    }
    @GET @Path("/{readingId}")
    public Response getReading(@PathParam("readingId") String readingId) {
        return store.getReadings(sensorId).stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst()
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(404)
                        .entity(Map.of("error", "Reading '" + readingId + "' not found.")).build());
    }
}
