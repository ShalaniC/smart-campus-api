package com.smartcampus.exception;

import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.util.logging.Logger;


@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        
        LOGGER.severe("Unhandled exception [" + ex.getClass().getName() + "]: " + ex.getMessage());

        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(
                        500,
                        "Internal Server Error",
                        "An unexpected error occurred. Please contact the API administrator."))
                .build();
    }
}
