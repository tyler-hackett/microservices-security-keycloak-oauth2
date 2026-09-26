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
import java.util.UUID;

@RequestScoped
@Transactional
@Path("/approval")
// Restrict access
@RolesAllowed("approver")
public class ApprovalResource extends ResourceBase {

    private final Logger logger;

    private final IImageDao imageDao;

    public ApprovalResource(Logger logger, IImageDao imageDao) {
        this.logger = logger;
        this.imageDao = imageDao;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ImageDto> getImagesForApproval() {
        return imagesToImageDtos(imageDao.getImagesForApproval());
    }

    @PUT
    @Path("{id}")
    public void updateApproval(@PathParam("id") UUID id,
                               @QueryParam("approved") boolean approved) {
        imageDao.updateApproval(id, approved);
    }

}
