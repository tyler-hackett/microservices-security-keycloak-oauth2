package io.github.tylerhackett.caf.webapp;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import io.github.tylerhackett.caf.service.IImageService;
import io.github.tylerhackett.caf.service.dto.CommentDto;
import io.github.tylerhackett.caf.service.dto.ImageDto;
import io.github.tylerhackett.caf.service.dto.ImageDtoFactory;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

/*
 * Restrict operations to the user role
 */

@RequestScoped
@Transactional
@Path("/web/images")
@RolesAllowed("user")
public class ImagesController {

    public static final String IMAGE_BUCKET_CONFIG_KEY = "caf.image.bucket";

    public static final String IMAGE_TYPE_CONFIG_KEY = "caf.image.type";

    public static final String IMAGE_EXT_CONFIG_KEY = "caf.image.ext";

    public static final String IMAGE_URL_CONFIG_KEY = "caf.image.url";

    private static final ImageDtoFactory IMAGE_DTO_FACTORY = new ImageDtoFactory();

    private final TimeBasedEpochGenerator uuidGenerator = Generators.timeBasedEpochGenerator();

    private final IImageService imageService;

    private final Logger logger;

    @Inject
    SecurityIdentity identity;

    @Inject
    S3Client storageClient;

    @Inject
    @ConfigProperty(name=IMAGE_BUCKET_CONFIG_KEY)
    String imageBucketName;

    @Inject
    @ConfigProperty(name=IMAGE_URL_CONFIG_KEY)
    String imageUrlTemplate;

    @Inject
    @ConfigProperty(name=IMAGE_TYPE_CONFIG_KEY)
    String imageType;

    public ImagesController(IImageService imageService, Logger logger) {
        this.imageService = imageService;
        this.logger = logger;
    }

    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance images(String username, List<ImageDto> images);

        public static native TemplateInstance image(String imageContent, ImageDto image);

    }


    public static String imageObjectName(UUID imageId) {
        return imageId.toString();
    }

    private String imageStorageUrl(UUID imageId) {
        return String.format(imageUrlTemplate, imageBucketName, imageObjectName(imageId));
    }

    @GET
    public TemplateInstance listImages(@QueryParam("username") String username) {
        logger.info(String.format("Getting list of images for user %s.", username));
        return Templates.images(username, imageService.getImages(username));
    }

    @GET
    @Path("/{id}")
    public TemplateInstance getImage(@PathParam("id") String id) {
        logger.info(String.format("Getting image with id %s.", id));
        UUID imageId = UUID.fromString(id);
        return Templates.image(getImageContent(imageId),
                               imageService.getImage(imageId));
    }


    private String getImageContent(UUID id) {
        String objectKey = imageObjectName(id);
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(imageBucketName)
                .key(objectKey)
                .build();
        ResponseBytes<GetObjectResponse> objectBytes = storageClient.getObjectAsBytes(request);
        StringBuilder sb = new StringBuilder();
        sb.append("data:image/jpq;base64,");
        sb.append(Base64.getEncoder().encodeToString(objectBytes.asByteArray()));
        return sb.toString();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public TemplateInstance uploadImage(@RestForm("image") FileUpload imageFile,
                                        @RestForm("caption") String caption) {
        logger.info("Uploading image \""+caption+"\" to S3");
        // Display images for this user when done
        String username = identity.getPrincipal().getName();
        UUID id = uuidGenerator.generate();
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(imageBucketName)
                .key(imageObjectName(id))
                .contentType(imageType)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();
        try {
            PutObjectResponse response = storageClient.putObject(request, RequestBody.fromFile(imageFile.uploadedFile()));
            if (response == null || !response.sdkHttpResponse().isSuccessful()) {
                logger.errorf("Invalid response from S3: %d", response==null ? 500 : response.sdkHttpResponse().statusCode());
                throw new ServerErrorException("Invalid response from attempt to upload image to S3 storage!", 500);
            }
        } catch (Exception e) {
            logger.errorf(e, "Exception thrown when attempting to upload image to S3 storage!");
            throw new ServerErrorException("Failure to upload image to S3 storage!", 500, e);
        }

        ImageDto imageDto = IMAGE_DTO_FACTORY.createImageDto();
        imageDto.setId(id);
        imageDto.setCaption(caption);
        imageDto.setUrl(imageStorageUrl(id));
        imageDto.setLoader(username);
        imageService.addImage(imageDto);
        return Templates.images(null, imageService.getImages());
    }

    @POST
    @Path("{image-id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance postComment(@FormParam("comment") String text,
                                        @PathParam("image-id") String imageId) {
        logger.info(String.format("Posting new comment \"%s\" for image %s: ", text, imageId));
        UUID id = UUID.fromString(imageId);
        CommentDto commentDto = IMAGE_DTO_FACTORY.createCommentDto();
        commentDto.setImageId(id);
        commentDto.setAuthor(identity.getPrincipal().getName());
        commentDto.setText(text);
        imageService.addComment(commentDto);
        return Templates.image(getImageContent(id), imageService.getImage(id));
    }

    @POST
    @Path("{image-id}/delete")
    public TemplateInstance deleteImage(@PathParam("image-id") String imageId) {
        logger.info(String.format("Deleting image %s: ", imageId));
        ImageDto imageDto = imageService.getImage(UUID.fromString(imageId));
        // Delete the image, or return 403 if the remover does not own it
        if (!imageDto.getLoader().equals(identity.getPrincipal().getName())) {
            throw new ForbiddenException("You are not the owner of this image");
        }
        imageService.removeImage(UUID.fromString(imageId));
        return Templates.images(null, imageService.getImages());
    }

    @POST
    @Path("{image-id}/delete/{comment-id}")
    public TemplateInstance deleteComment(@PathParam("image-id") String imageId,
                                          @PathParam("comment-id") String commentId) {
        logger.info(String.format("Deleting comment \"%s\" for image %s: ", commentId, imageId));
        UUID id = UUID.fromString(imageId);
        UUID cid = UUID.fromString(commentId);
        ImageDto imageDto = imageService.getImage(id);
        CommentDto commentDto = imageService.getComment(cid);
        // Delete the comment, or return 403 if the remover does not own the image or the comment is not for the image
        // Note that commenter themselves does not have permission to delete their own comment!
        // Display image with its comments when done.
        if (!imageDto.getLoader().equals(identity.getPrincipal().getName())) {
            throw new ForbiddenException("You are not the owner of this image");
        }
        if (!commentDto.getImageId().equals(id)) {
            throw new BadRequestException("Comment does not belong to this image");
        }
        imageService.removeComment(cid);

        return Templates.image(getImageContent(id), imageService.getImage(id));
    }

}