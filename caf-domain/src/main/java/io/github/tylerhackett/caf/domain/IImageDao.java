package io.github.tylerhackett.caf.domain;

import java.util.List;
import java.util.UUID;


public interface IImageDao {

	public void addImage (Image image);

	public void removeImage (UUID id);

	public void addComment (UUID imageId, Comment comment);

	public Comment getComment(UUID id);

	public void removeComment (UUID id);

	public Image getImage(UUID id);

	public List<Image> getImages();

	public List<Image> getImages(String username);

	public List<Image> getImagesForApproval();

	public void updateApproval(UUID id, boolean approved);

}
