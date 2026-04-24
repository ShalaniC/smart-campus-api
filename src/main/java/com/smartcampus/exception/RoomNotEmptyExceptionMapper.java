package com.smartcampus.exception;
import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;


@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    public Response toResponse(RoomNotEmptyException ex) {
        return Response.status(409).type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(409, "Conflict", ex.getMessage())).build();
    }
}
