package io.github.tylerhackett.caf.webapp;

import io.github.tylerhackett.caf.service.IImageService;
import io.github.tylerhackett.caf.service.Role;
import io.github.tylerhackett.caf.service.dto.ImageDto;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

/*
 * Restrict operations to the approver role
 */

@RequestScoped
@Transactional
@Path("/web/approval")
@RolesAllowed(Role.APPROVER)
public class ApprovalController {

    public static final String IMAGE_BUCKET_CONFIG_KEY = "caf.image.bucket";

    private final IImageService imageService;

    private final Logger logger;

    @Inject
    S3Client storageClient;

    @Inject
    @ConfigProperty(name=IMAGE_BUCKET_CONFIG_KEY)
    String imageBucketName;

    public ApprovalController(IImageService imageService, Logger logger) {
        this.imageService = imageService;
        this.logger = logger;
    }

    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance images(List<ImageDto> images);

        public static native TemplateInstance image(String imageContent, ImageDto image);

    }

    @GET
    public TemplateInstance listImages() {
        logger.info("Getting list of images for approval.");
        return Templates.images(imageService.getImagesForApproval());
    }

    @GET
    @Path("{id}")
    public TemplateInstance getImage(@PathParam("id") String id) {
        UUID imageId = UUID.fromString(id);
        return Templates.image(getImageContent(imageId),
                               imageService.getImage(imageId));
    }

    @POST
    @Path("{id}")
    public TemplateInstance approveImage(@PathParam("id") String id,
                                         @FormParam("approval") String approval) {
        logger.infof("Approving image, approval is %s", approval);
        boolean decision = Boolean.parseBoolean(approval);
        imageService.updateApproval(UUID.fromString(id), decision);
        return Templates.images(imageService.getImagesForApproval());
    }

    private String getImageContent(UUID id) {
        String objectKey = ImagesController.imageObjectName(id);
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(imageBucketName)
                .key(objectKey)
                .build();
        ResponseBytes<GetObjectResponse> objectBytes = storageClient.getObjectAsBytes(request);
        StringBuilder sb = new StringBuilder();
        sb.append("data:image/jpg;base64,");
        sb.append(Base64.getEncoder().encodeToString(objectBytes.asByteArray()));
        return sb.toString();
    }

}
