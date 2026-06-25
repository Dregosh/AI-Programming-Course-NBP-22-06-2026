package pl.nbp.backend.image;

import org.springframework.stereotype.Service;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Validates, downscales, and re-encodes uploaded images as JPEG data URIs.
 *
 * <p>Accepted formats: JPEG, PNG, WebP — detected by magic bytes, not by the
 * claimed {@code Content-Type} header. Images exceeding 10 MB or using an
 * unsupported format are rejected with a descriptive exception. Images whose
 * longest edge exceeds 1568 px are proportionally downscaled before
 * re-encoding.
 */
@Service
public class ImageService {

    private static final int MAX_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final int MAX_EDGE = 1568;
    private static final float JPEG_QUALITY = 0.80f;

    /**
     * Validates the supplied image bytes, downscales if necessary, re-encodes
     * as JPEG, and returns a base64 data URI.
     *
     * @param imageBytes  raw image bytes uploaded by the client
     * @param contentType the claimed MIME type (ignored for format detection)
     * @return {@code "data:image/jpeg;base64,<encoded>"} ready for embedding in JSON
     * @throws ImageTooLargeException         if {@code imageBytes.length} exceeds 10 MB
     * @throws UnsupportedImageTypeException  if the magic bytes do not match JPEG, PNG, or WebP
     */
    public String validateAndCompress(byte[] imageBytes, String contentType) {
        if (imageBytes.length > MAX_BYTES) {
            throw new ImageTooLargeException("Image exceeds 10 MB limit");
        }

        detectFormat(imageBytes); // throws UnsupportedImageTypeException if not jpeg/png/webp

        BufferedImage img = decodeImage(imageBytes);
        img = downscale(img);
        byte[] jpegBytes = encodeJpeg(img);
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(jpegBytes);
    }

    /**
     * Inspects the leading bytes of the image to determine its format.
     * Throws {@link UnsupportedImageTypeException} for any format other than
     * JPEG, PNG, or WebP.
     *
     * @param bytes raw image bytes (must have at least 4 bytes)
     * @throws UnsupportedImageTypeException if the format is not recognised
     */
    private void detectFormat(byte[] bytes) {
        if (bytes.length < 4) {
            throw new UnsupportedImageTypeException("Image too small to detect format");
        }
        // JPEG: FF D8 FF
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return;
        }
        // PNG: 89 50 4E 47
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return;
        }
        // WebP: RIFF at 0-3, WEBP at 8-11
        if (bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return;
        }
        throw new UnsupportedImageTypeException("Unsupported image format (only jpeg/png/webp allowed)");
    }

    /**
     * Decodes the raw bytes into a {@link BufferedImage}.
     *
     * @param bytes raw image bytes
     * @return decoded image
     * @throws UnsupportedImageTypeException if {@link ImageIO} cannot decode the bytes
     */
    private BufferedImage decodeImage(byte[] bytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) throw new UnsupportedImageTypeException("Cannot decode image");
            return img;
        } catch (IOException e) {
            throw new UnsupportedImageTypeException("Cannot decode image: " + e.getMessage());
        }
    }

    /**
     * Proportionally downscales the image so that its longest edge is at most
     * {@value #MAX_EDGE} px. Images already within bounds are returned unchanged.
     *
     * @param img source image
     * @return downscaled image, or the original if no downscaling was needed
     */
    private BufferedImage downscale(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (w <= MAX_EDGE && h <= MAX_EDGE) return img;
        double scale = (double) MAX_EDGE / Math.max(w, h);
        int newW = (int) Math.round(w * scale);
        int newH = (int) Math.round(h * scale);
        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, 0, 0, newW, newH, null);
        g2.dispose();
        return out;
    }

    /**
     * Re-encodes the image as JPEG at {@value #JPEG_QUALITY} quality.
     *
     * @param img source image
     * @return JPEG-encoded bytes
     * @throws RuntimeException if JPEG encoding fails unexpectedly
     */
    private byte[] encodeJpeg(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(JPEG_QUALITY);
            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(img, null, null), param);
            writer.dispose();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("JPEG encoding failed", e);
        }
    }
}
