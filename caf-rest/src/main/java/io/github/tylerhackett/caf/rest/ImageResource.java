package io.github.tylerhackett.caf.rest;

import io.github.tylerhackett.caf.domain.Comment;
import io.github.tylerhackett.caf.domain.IImageDao;
import io.github.tylerhackett.caf.domain.Image;
import io.github.tylerhackett.caf.service.dto.CommentDto;
import io.github.tylerhackett.caf.service.dto.ImageDto;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequestScoped
@Transactional
@Path("/images")
@NoCache
// Restrict access
@RolesAllowed("user")
public class ImageResource extends ResourceBase {

	public static final String IMAGE_BUCKET_CONFIG_KEY = "caf.image.bucket";

	public static final String MAX_WIDTH_CONFIG_KEY = "caf.thumbnail.maxwidth";

	public static final String MAX_HEIGHT_CONFIG_KEY = "caf.thumbnail.maxheight";

	public static final String ISSUER_CONFIG_KEY = "caf.jwt.issuer";

	public static final String AUDIENCE_CONFIG_KEY = "caf.jwt.audience";

	@Inject
	@ConfigProperty(name=IMAGE_BUCKET_CONFIG_KEY)
	String imageBucketName;

	@Inject
	@ConfigProperty(name=MAX_WIDTH_CONFIG_KEY)
	String maxwidth;

	@Inject
	@ConfigProperty(name=MAX_HEIGHT_CONFIG_KEY)
	String maxheight;

	@Inject
	SecurityIdentity caller;

	@Inject
	@ConfigProperty(name=ISSUER_CONFIG_KEY)
	String issuer;

	@Inject
	@ConfigProperty(name=AUDIENCE_CONFIG_KEY)
	String audience;

	@RestClient
	IThumbnailResource thumbnailResource;

	private final Logger logger;

	private final IImageDao imageDao;

	public ImageResource(Logger logger, IImageDao imageDao) {
		this.logger = logger;
		this.imageDao = imageDao;
	}

	public static String imageObjectName(UUID imageId) {
		return imageId.toString();
	}


	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void addImage(ImageDto imageDto) {
		// Generate the bearer token for the Authorization header
		String token = Jwt.subject(caller.getPrincipal().getName())
				.issuer(issuer)
				.audience(audience)
				.groups("user")
				.sign().toString();

		// Get the thumbnail by calling the thumbnailResource
		String thumbnail = thumbnailResource.getThumbnail(
				"Bearer " + token,
				imageBucketName,
				imageObjectName(imageDto.getId()),
				maxwidth,
				maxheight
		);

		Image image = IMAGE_FACTORY.createImage();
		image.setId(imageDto.getId());
		image.setCaption(imageDto.getCaption());
		image.setThumbnail("data:image/jpq;base64," + thumbnail);
		image.setLoader(imageDto.getLoader());
		image.setApproved(false);
		image.setTimestamp(Instant.now());
		image.setUrl(imageDto.getUrl());
		imageDao.addImage(image);
	}

	@DELETE
	@Path("{id}")
	public void removeImage(@PathParam("id") UUID imageId) {
		imageDao.removeImage(imageId);
	}

	@POST
	@Path("/comment")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addComment(CommentDto commentDto) {
		Comment comment = IMAGE_FACTORY.createComment();
		comment.setId(uuidGenerator.generate());
		comment.setText(commentDto.getText());
		comment.setTimestamp(Instant.now());
		comment.setAuthor(commentDto.getAuthor());
		imageDao.addComment(commentDto.getImageId(), comment);
	}

	@GET
	@Path("/comment/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public CommentDto getComment(@PathParam("id") UUID id) {
		Comment comment = imageDao.getComment(id);
		CommentDto commentDto = IMAGE_DTO_FACTORY.createCommentDto();
		commentDto.setId(comment.getId());
		commentDto.setText(comment.getText());
		commentDto.setImageId(comment.getImage().getId());
		commentDto.setTimestamp(comment.getTimestamp());
		commentDto.setAuthor(comment.getAuthor());
		return commentDto;
	}

	@DELETE
	@Path("/comment/{id}")
	public void removeComment(@PathParam("id") UUID id) throws SecurityException {
		imageDao.removeComment(id);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImageDto> getImages() {
		return imagesToImageDtos(imageDao.getImages());
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ImageDto getImage(@PathParam("id") UUID imageId) {
		return imageToImageDto(imageDao.getImage(imageId), true);
	}

}