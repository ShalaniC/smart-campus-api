package com.smartcampus.exception;
import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;


@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {
    public Response toResponse(LinkedResourceNotFoundException ex) {
        return Response.status(422).type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(422, "Unprocessable Entity", ex.getMessage())).build();
    }
}
