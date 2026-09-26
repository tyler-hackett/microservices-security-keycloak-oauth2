package io.github.tylerhackett.caf.service.micro;
import io.github.tylerhackett.caf.service.dto.CommentDto;
import io.github.tylerhackett.caf.service.dto.ImageDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;
import java.util.UUID;

@RegisterRestClient(configKey="caf-images-api")
@Path("/images")
@AccessToken
public interface IImageResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void addImage(@HeaderParam("Authorization") String auth,
                  ImageDto imageDto);

    @DELETE
    @Path("{id}")
    void removeImage(@HeaderParam("Authorization") String auth,
                     @PathParam("id") UUID imageId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void addComment(@HeaderParam("Authorization") String auth,
                    CommentDto commentDto);

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    CommentDto getComment(@HeaderParam("Authorization") String auth,
                          @PathParam("id") UUID id);

    @DELETE
    @Path("{id}")
    void removeComment(@HeaderParam("Authorization") String auth,
                       @PathParam("id") UUID id) throws SecurityException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<ImageDto> getImages(@HeaderParam("Authorization") String auth);

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    ImageDto getImage(@HeaderParam("Authorization") String auth,
                      @PathParam("id") UUID imageId);

}
