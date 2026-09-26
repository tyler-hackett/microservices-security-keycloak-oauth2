package io.github.tylerhackett.caf.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@RegisterRestClient(configKey="caf-thumbnail-api")
@Path("/api/thumbnail")
public interface IThumbnailResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getThumbnail(@HeaderParam ("Authorization") String auth,
                        @QueryParam("bucket") String bucketName,
                        @QueryParam("object") String objectKey,
                        @QueryParam("width") String width,
                        @QueryParam("height") String height);
}
