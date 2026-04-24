package com.smartcampus.exception;
import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;


@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {
    public Response toResponse(SensorUnavailableException ex) {
        return Response.status(403).type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(403, "Forbidden", ex.getMessage())).build();
    }
}
