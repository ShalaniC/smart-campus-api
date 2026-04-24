package com.smartcampus.resource;
import com.smartcampus.application.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.*;
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    private final DataStore store = DataStore.INSTANCE;
    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(store.getRooms().values())).build();
    }
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank())
            return Response.status(400).entity(Map.of("error", "Room 'id' is required.")).build();
        if (store.getRoom(room.getId()) != null)
            return Response.status(409).entity(Map.of("error", "Room already exists.")).build();
        store.putRoom(room);
        URI location = UriBuilder.fromResource(RoomResource.class).path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }
    @GET @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null)
            return Response.status(404).entity(Map.of("error", "Room '" + roomId + "' not found.")).build();
        return Response.ok(room).build();
    }
    @DELETE @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null)
            return Response.status(404).entity(Map.of("error", "Room '" + roomId + "' not found.")).build();
        if (!room.getSensorIds().isEmpty())
            throw new RoomNotEmptyException("Room '" + roomId + "' cannot be deleted. It still has "
                    + room.getSensorIds().size() + " sensor(s) assigned to it.");
        store.deleteRoom(roomId);
        return Response.noContent().build();
    }
}
