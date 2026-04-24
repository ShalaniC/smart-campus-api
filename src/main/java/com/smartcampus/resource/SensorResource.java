package com.smartcampus.resource;
import com.smartcampus.application.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    private final DataStore store = DataStore.INSTANCE;
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> result = store.getSensors().values().stream()
                .filter(s -> type == null || type.isBlank() || type.equalsIgnoreCase(s.getType()))
                .collect(Collectors.toList());
        return Response.ok(result).build();
    }
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank())
            return Response.status(400).entity(Map.of("error", "Sensor 'id' is required.")).build();
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank())
            return Response.status(400).entity(Map.of("error", "Sensor 'roomId' is required.")).build();
        if (store.getSensor(sensor.getId()) != null)
            return Response.status(409).entity(Map.of("error", "Sensor already exists.")).build();
        if (store.getRoom(sensor.getRoomId()) == null)
            throw new LinkedResourceNotFoundException(
                    "Room '" + sensor.getRoomId() + "' does not exist. Create the room first.");
        store.putSensor(sensor);
        store.getRoom(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        URI location = UriBuilder.fromResource(SensorResource.class).path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }
    @GET @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null)
            return Response.status(404).entity(Map.of("error", "Sensor '" + sensorId + "' not found.")).build();
        return Response.ok(sensor).build();
    }
    @DELETE @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null)
            return Response.status(404).entity(Map.of("error", "Sensor '" + sensorId + "' not found.")).build();
        if (sensor.getRoomId() != null && store.getRoom(sensor.getRoomId()) != null)
            store.getRoom(sensor.getRoomId()).getSensorIds().remove(sensorId);
        store.deleteSensor(sensorId);
        return Response.noContent().build();
    }
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        if (store.getSensor(sensorId) == null)
            throw new NotFoundException("Sensor '" + sensorId + "' not found.");
        return new SensorReadingResource(sensorId);
    }
}
