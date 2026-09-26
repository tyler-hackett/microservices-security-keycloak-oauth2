package io.github.tylerhackett.caf.service;

import io.github.tylerhackett.caf.service.dto.CommentDto;
import io.github.tylerhackett.caf.service.dto.ImageDto;

import java.util.List;
import java.util.UUID;

public interface IImageService {

	public void addImage (ImageDto imageDto);

	public void removeImage (UUID id);

	public void addComment (CommentDto commentDto);

	public CommentDto getComment(UUID id);

	public void removeComment (UUID id);

	public List<ImageDto> getImages();

	public List<ImageDto> getImages(String username);

	public ImageDto getImage(UUID imageId);

	public List<ImageDto> getImagesForApproval();

	public void updateApproval(UUID id, boolean approved);

}
