package io.github.tylerhackett.caf.domain;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;


@RequestScoped
@Transactional
public class ImageDao implements IImageDao {

	private final EntityManager em;

	private final Logger logger;
	
	public ImageDao(EntityManager em, Logger logger) {
		this.em = em;
		this.logger = logger;
	}

	@Override
	public void addImage(Image image) {
		logger.info(String.format("Adding image \"%s\"", image.getCaption()));
		em.persist(image);
		em.flush();
	}

	@Override
	public void removeImage(UUID id) {
		logger.info(String.format("Removing image \"%s\"", id.toString()));
		Image image = em.find(Image.class, id);
		if (image == null) {
			throw new EntityNotFoundException(String.format("Image %s not found in the database.", id.toString()));
		}
		em.remove(image);
		em.flush();
	}

	@Override
	public void addComment(UUID imageId, Comment comment) {
		logger.info(String.format("Adding comment \"%s\" to image %s", comment.getText(), imageId.toString()));
		Image image = em.find(Image.class, imageId);
		if (image == null) {
			throw new EntityNotFoundException(String.format("Image %s not found in the database.", imageId.toString()));
		}
		comment.setImage(image);
		image.addComment(comment);
		em.persist(comment);
		em.flush();
	}

	@Override
	public void removeComment(UUID id) {
		logger.info(String.format("Removing comment %s", id.toString()));
		Comment comment = em.find(Comment.class, id);
		if (comment == null) {
			throw new EntityNotFoundException(String.format("Comment %s not found in the database.", id.toString()));
		}
		comment.getImage().removeComment(comment);
		em.remove(comment);
		em.flush();
	}

	@Override
	public Comment getComment(UUID id) {
		logger.info(String.format("Removing comment %s", id.toString()));
		Comment comment = em.find(Comment.class, id);
		if (comment == null) {
			throw new EntityNotFoundException(String.format("Comment %s not found in the database.", id.toString()));
		}
		return comment;
	}

	@Override
	public Image getImage(UUID id) {
		Image image = em.find(Image.class, id);
		if (image == null) {
			throw new EntityNotFoundException(String.format("Image %s not found in the database.", id.toString()));
		}
		return image;
	}

	@Override
	public List<Image> getImages() {
		TypedQuery<Image> query = em.createNamedQuery("AllImages", Image.class);
		return query.getResultList();
	}

	@Override
	public List<Image> getImages(String username) {
		TypedQuery<Image> query = em.createNamedQuery("ImagesByUser", Image.class)
				.setParameter("username", username);
		return query.getResultList();
	}

	@Override
	public List<Image> getImagesForApproval() {
		TypedQuery<Image> query = em.createNamedQuery("ImagesForApproval", Image.class);
		return query.getResultList();
	}

	@Override
	public void updateApproval(UUID id, boolean approved) {
		logger.infof("Updating approval of image \"%s\"", id.toString());
		Image image = em.find(Image.class, id);
		if (image == null) {
			throw new EntityNotFoundException(String.format("Image %s not found in the database.", id.toString()));
		}
		if (approved) {
			logger.info("...image approved!");
			image.setApproved(true);
		} else {
			logger.info("...image not approved!");
			em.remove(image);
			em.flush();
		}
	}

}
