package io.github.tylerhackett.caf.service;

import io.github.tylerhackett.caf.service.dto.CommentDto;
import io.github.tylerhackett.caf.service.dto.ImageDto;
import io.github.tylerhackett.caf.service.micro.IApprovalResource;
import io.github.tylerhackett.caf.service.micro.IImageResource;
import io.github.tylerhackett.caf.service.micro.IUserResource;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

@RequestScoped
public class ImageService implements IImageService {

    @Inject
    Logger logger;

    @Inject
    SecurityIdentity caller;

    @ConfigProperty(name = "caf.jwt.issuer")
    String issuer;

    @ConfigProperty(name = "caf.jwt.audience")
    String audience;

    @RestClient
    IApprovalResource approvalResource;

    @RestClient
    IImageResource imageResource;

    @RestClient
    IUserResource userResource;

    private String generateAuthToken() {
        return "Bearer " + Jwt.subject(caller.getPrincipal().getName())
                .issuer(issuer)
                .audience(audience)
                .groups("user")
                .sign().toString();
    }

    @Override
    public void addImage(ImageDto imageDto) {
        imageResource.addImage(generateAuthToken(), imageDto);
    }

    @Override
    public void removeImage(UUID id) {
        imageResource.removeImage(generateAuthToken(), id);
    }

    @Override
    public void addComment(CommentDto commentDto) {
        imageResource.addComment(generateAuthToken(), commentDto);
    }

    @Override
    public CommentDto getComment(UUID id) {
        return imageResource.getComment(generateAuthToken(), id);
    }

    @Override
    public void removeComment(UUID id) {
        imageResource.removeComment(generateAuthToken(), id);
    }

    @Override
    public List<ImageDto> getImages() {
        return imageResource.getImages(generateAuthToken());
    }

    @Override
    public ImageDto getImage(UUID imageId) {
        return imageResource.getImage(generateAuthToken(), imageId);
    }

    @Override
    public List<ImageDto> getImages(String username) {
        if (username == null || username.isBlank()) {
            return imageResource.getImages(generateAuthToken());
        } else {
            return userResource.getImages(generateAuthToken(), username);
        }
    }

    @Override
    public List<ImageDto> getImagesForApproval() {
        return approvalResource.getImagesForApproval(generateAuthToken());
    }

    @Override
    public void updateApproval(UUID id, boolean approved) {
        approvalResource.updateApproval(generateAuthToken(), id, approved);
    }

}
