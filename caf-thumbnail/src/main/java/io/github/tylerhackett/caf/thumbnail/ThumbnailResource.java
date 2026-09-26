package io.github.tylerhackett.caf.thumbnail;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@RequestScoped
@Path("/api/thumbnail")
// Require user role
@RolesAllowed("user")
public class ThumbnailResource {

    @Inject
    ThumbnailService thumbnailService;

    @Inject
    Logger logger;

    @Inject
    SecurityIdentity caller;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getThumbnail(@QueryParam("bucket") String bucketName,
                               @QueryParam("object") String objectKey,
                               @QueryParam("width") String width,
                               @QueryParam("height") String height) {

        // Log these claims from the bearer token:
        // - principal
        // - roles of the principal
        logger.info("Principal: " + caller.getPrincipal().getName());
        logger.info("Roles: " + caller.getRoles());

        return thumbnailService.createThumbnail(bucketName, objectKey, Integer.parseInt(width), Integer.parseInt(height));
    }

}
