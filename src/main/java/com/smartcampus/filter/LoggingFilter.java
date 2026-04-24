package com.smartcampus.filter;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;


@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(String.format(
                ">>> REQUEST  | Method: %-7s | URI: %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri().toString()
        ));
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info(String.format(
                "<<< RESPONSE | Status: %d | URI: %s",
                responseContext.getStatus(),
                requestContext.getUriInfo().getRequestUri().toString()
        ));
    }
}
