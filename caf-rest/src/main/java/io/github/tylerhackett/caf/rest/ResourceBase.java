package io.github.tylerhackett.caf.rest;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import io.github.tylerhackett.caf.domain.Comment;
import io.github.tylerhackett.caf.domain.IImageFactory;
import io.github.tylerhackett.caf.domain.Image;
import io.github.tylerhackett.caf.domain.ImageFactory;
import io.github.tylerhackett.caf.service.dto.CommentDto;
import io.github.tylerhackett.caf.service.dto.ImageDto;
import io.github.tylerhackett.caf.service.dto.ImageDtoFactory;

import java.util.ArrayList;
import java.util.List;

public class ResourceBase {

    protected static final IImageFactory IMAGE_FACTORY = new ImageFactory();

    protected static final ImageDtoFactory IMAGE_DTO_FACTORY = new ImageDtoFactory();

    protected final TimeBasedEpochGenerator uuidGenerator = Generators.timeBasedEpochGenerator();

    protected ImageDto imageToImageDto(Image image, boolean includeComments) {
        ImageDto imageDto = IMAGE_DTO_FACTORY.createImageDto();
        imageDto.setId(image.getId());
        imageDto.setUrl(image.getUrl());
        imageDto.setCaption(image.getCaption());
        imageDto.setThumbnail(image.getThumbnail());
        imageDto.setApproved(image.isApproved());
        imageDto.setTimestamp(image.getTimestamp());
        imageDto.setLoader(image.getLoader());
        if (includeComments) {
            imageDto.setComments(commentsToCommentDtos(image.getComments()));
        }
        return imageDto;
    }

    protected ImageDto imageToImageDto(Image image) {
        return imageToImageDto(image, false);
    }

    protected List<ImageDto> imagesToImageDtos(List<Image> images) {
        List<ImageDto> imageDtos = new ArrayList<>();
        for (Image image : images) {
            imageDtos.add(imageToImageDto(image));
        }
        return imageDtos;
    }

    protected List<CommentDto> commentsToCommentDtos(List<Comment> comments) {
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

}
