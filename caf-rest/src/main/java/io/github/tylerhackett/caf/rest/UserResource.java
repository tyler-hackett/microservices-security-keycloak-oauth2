package io.github.tylerhackett.caf.rest;

import io.github.tylerhackett.caf.domain.IImageDao;
import io.github.tylerhackett.caf.service.dto.ImageDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.util.List;

@RequestScoped
@Transactional
@Path("/users")
// Restrict access
@RolesAllowed("user")
public class UserResource extends ResourceBase {

    private final Logger logger;

    private final IImageDao imageDao;

    public UserResource(Logger logger, IImageDao imageDao) {
        this.logger = logger;
        this.imageDao = imageDao;
    }

    @GET
    @Path("{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ImageDto> getImages(@PathParam("username") @DefaultValue("") String username) {
        if (username == null || username.isBlank()) {
            return imagesToImageDtos(imageDao.getImages());
        } else {
            return imagesToImageDtos(imageDao.getImages(username));
        }
    }
}
