package io.github.tylerhackett.caf.service.micro;
import io.github.tylerhackett.caf.service.dto.ImageDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;

@RegisterRestClient(configKey="caf-images-api")
@Path("/users")
@AccessToken
public interface IUserResource {

    @GET
    @Path("{username}")
    @Produces(MediaType.APPLICATION_JSON)
    List<ImageDto> getImages(@HeaderParam("Authorization") String auth,
                             @PathParam("username") String username);

}
