package io.github.tylerhackett.caf.thumbnail;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ServerErrorException;
import org.jboss.logging.Logger;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@RequestScoped
public class ThumbnailService {

    @Inject
    Logger logger;

    @Inject
    S3Client storageClient;

    public String createThumbnail(String bucketName, String objectKey, int maxWidth, int maxHeight) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            ResponseBytes<GetObjectResponse> objectBytes = storageClient.getObjectAsBytes(request);
            InputStream in = objectBytes.asInputStream();
            BufferedImage originalImage = ImageIO.read(in);
            if (originalImage == null) {
                throw new ClientErrorException(String.format("Could not read image: %s/%s", bucketName, objectKey), 404);
            }

            // Calculate scaled dimensions while preserving aspect ratio
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            double widthScale = (double) maxWidth / originalWidth;
            double heightScale = (double) maxHeight / originalHeight;
            double scaleFactor = Math.min(widthScale, heightScale); // Use smaller scale to fit

            // Prevent upscaling if original is smaller than target
            scaleFactor = Math.min(scaleFactor, 1.0);

            int scaledWidth = (int) (originalWidth * scaleFactor);
            int scaledHeight = (int) (originalHeight * scaleFactor);

            // Create a new BufferedImage for the thumbnail
            BufferedImage thumbnail = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);

            // Draw the scaled image using high-quality interpolation
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
            g2d.dispose();

            // Return the thumbnail Base64-encoded
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());

        } catch (IOException e) {

            logger.errorf(e, "Could not create thumbnail for %s/%s", bucketName, objectKey);
            throw new ServerErrorException("IO Exception while creating thumbnail", 500);

        }
    }

}
