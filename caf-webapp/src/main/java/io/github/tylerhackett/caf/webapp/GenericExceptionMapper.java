package io.github.tylerhackett.caf.webapp;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.UUID;

@Provider
@ApplicationScoped
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Inject
    @Location("errors/generic.html")
    Template errorPage;

    @Inject
    Logger logger;

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {

        int statusCode;

        switch (exception) {
            case WebApplicationException we -> {
                statusCode = we.getResponse().getStatus();
            }
            default -> statusCode = 500;
        }

        UUID errorId = UUID.randomUUID();

        boolean devMode = LaunchMode.current().isDev();

        logger.errorf(exception, "Unhandled exception (ID %s) at %s", errorId, uriInfo.getPath());

        return Response.status(statusCode)
                .entity(errorPage
                        .data("statusCode", statusCode)
                        .data("reference", errorId.toString())
                        .data("exceptionType", exception.getClass().getName())
                        .data("exceptionMessage", exception.getMessage())
                        .data("path", uriInfo.getPath())
                        .data("devMode", devMode))
                .type(MediaType.TEXT_HTML)
                .build();
    }

}
