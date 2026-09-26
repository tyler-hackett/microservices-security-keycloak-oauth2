package io.github.tylerhackett.caf.service;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import io.github.tylerhackett.caf.domain.*;
import io.github.tylerhackett.caf.service.dto.CommentDto;
import io.github.tylerhackett.caf.service.dto.ImageDto;
import io.github.tylerhackett.caf.service.dto.ImageDtoFactory;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequestScoped
@Transactional
public class ImageService implements IImageService {

	private static final IImageFactory IMAGE_FACTORY = new ImageFactory();
	
	private static final ImageDtoFactory IMAGE_DTO_FACTORY = new ImageDtoFactory();

	private final TimeBasedEpochGenerator uuidGenerator = Generators.timeBasedEpochGenerator();

	private final Logger logger;

	private final IImageDao imageDao;

	/**
	 * Default constructor.
	 */
	public ImageService(Logger logger, IImageDao imageDao) {
		this.logger = logger;
		this.imageDao = imageDao;
	}

	@Override
	public void addImage(ImageDto imageDto) {
		Image image = IMAGE_FACTORY.createImage();
		image.setId(imageDto.getId());
		image.setCaption(imageDto.getCaption());
		image.setLoader(imageDto.getLoader());
		image.setApproved(false);
		image.setTimestamp(Instant.now());
		image.setUrl(imageDto.getUrl());
		imageDao.addImage(image);
	}

	@Override
	public void removeImage(UUID imageId) {
		imageDao.removeImage(imageId);
	}

	@Override
	public void addComment(CommentDto commentDto) {
		Comment comment = IMAGE_FACTORY.createComment();
		comment.setId(uuidGenerator.generate());
		comment.setText(commentDto.getText());
		comment.setTimestamp(Instant.now());
		comment.setAuthor(commentDto.getAuthor());
		imageDao.addComment(commentDto.getImageId(), comment);
	}

	@Override
	public CommentDto getComment(UUID id) {
		Comment comment = imageDao.getComment(id);
		CommentDto commentDto = IMAGE_DTO_FACTORY.createCommentDto();
		commentDto.setId(comment.getId());
		commentDto.setText(comment.getText());
		commentDto.setImageId(comment.getImage().getId());
		commentDto.setTimestamp(comment.getTimestamp());
		commentDto.setAuthor(comment.getAuthor());
		return commentDto;
	}

	@Override
	public void removeComment(UUID id) throws SecurityException {
		imageDao.removeComment(id);
	}

	private ImageDto imageToImageDto(Image image, boolean includeComments) {
		ImageDto imageDto = IMAGE_DTO_FACTORY.createImageDto();
		imageDto.setId(image.getId());
		imageDto.setUrl(image.getUrl());
		imageDto.setCaption(image.getCaption());
		imageDto.setApproved(image.isApproved());
		imageDto.setTimestamp(image.getTimestamp());
		imageDto.setLoader(image.getLoader());
		if (includeComments) {
			imageDto.setComments(commentsToCommentDtos(image.getComments()));
		}
		return imageDto;
	}

	private ImageDto imageToImageDto(Image image) {
		return imageToImageDto(image, false);
	}

	private List<ImageDto> imagesToImageDtos(List<Image> images) {
		List<ImageDto> imageDtos = new ArrayList<>();
		for (Image image : images) {
			imageDtos.add(imageToImageDto(image));
		}
		return imageDtos;
	}

	private List<CommentDto> commentsToCommentDtos(List<Comment> comments) {
		List<CommentDto> commentDtos = new ArrayList<>();
		for (Comment comment : comments) {
			CommentDto commentDto = IMAGE_DTO_FACTORY.createCommentDto();
			commentDto.setId(comment.getId());
			commentDto.setText(comment.getText());
			commentDto.setAuthor(comment.getAuthor());
			commentDto.setImageId(comment.getImage().getId());
			commentDto.setTimestamp(comment.getTimestamp());

			commentDtos.add(commentDto);
		}
		return commentDtos;
	}

	@Override
	public List<ImageDto> getImages(String username) {
		if (username == null || username.isBlank()) {
			return imagesToImageDtos(imageDao.getImages());
		} else {
			return imagesToImageDtos(imageDao.getImages(username));
		}
	}

	@Override
	public List<ImageDto> getImages() {
		return getImages(null);
	}

	@Override
	public ImageDto getImage(UUID imageId) {
		return imageToImageDto(imageDao.getImage(imageId), true);
	}

	@Override
	public List<ImageDto> getImagesForApproval() {
		return imagesToImageDtos(imageDao.getImagesForApproval());
	}

	@Override
	public void updateApproval(UUID id, boolean approved) {
		imageDao.updateApproval(id, approved);
	}

}