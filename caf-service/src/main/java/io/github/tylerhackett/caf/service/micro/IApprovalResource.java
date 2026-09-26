package io.github.tylerhackett.caf.service.micro;
import io.github.tylerhackett.caf.service.dto.ImageDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;
import java.util.UUID;

@RegisterRestClient(configKey="caf-images-api")
@Path("/approval")
@AccessToken
public interface IApprovalResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ImageDto> getImagesForApproval(@HeaderParam("Authorization") String auth);

    @PUT
    @Path("{id}")
    void updateApproval(@HeaderParam("Authorization") String auth,
                        @PathParam("id") UUID id,
                        @QueryParam("approved") boolean approved);

}
