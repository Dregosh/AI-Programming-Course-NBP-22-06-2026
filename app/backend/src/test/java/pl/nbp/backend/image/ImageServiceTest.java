package pl.nbp.backend.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import static org.assertj.core.api.Assertions.*;

class ImageServiceTest {

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService();
    }

    // Helper: create a JPEG byte array
    private byte[] createJpeg(int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpeg", baos);
        return baos.toByteArray();
    }

    // Helper: create a PNG byte array
    private byte[] createPng(int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    @Test
    void validJpegReturnsDataUri() throws Exception {
        byte[] jpeg = createJpeg(100, 100);
        String result = imageService.validateAndCompress(jpeg, "image/jpeg");
        assertThat(result).startsWith("data:image/jpeg;base64,");
    }

    @Test
    void largeImageIsDownscaled() throws Exception {
        byte[] png = createPng(3000, 2000);
        String result = imageService.validateAndCompress(png, "image/png");
        // decode and check dimensions
        String b64 = result.substring("data:image/jpeg;base64,".length());
        byte[] decoded = Base64.getDecoder().decode(b64);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(decoded));
        assertThat(Math.max(img.getWidth(), img.getHeight())).isLessThanOrEqualTo(1568);
    }

    @Test
    void gifBytesThrowUnsupportedImageTypeException() {
        // GIF magic bytes: GIF89a
        byte[] gif = new byte[]{'G', 'I', 'F', '8', '9', 'a', 0, 0, 0, 0, 0, 0};
        assertThatThrownBy(() -> imageService.validateAndCompress(gif, "image/gif"))
            .isInstanceOf(UnsupportedImageTypeException.class);
    }

    @Test
    void tooLargeImageThrowsImageTooLargeException() {
        byte[] big = new byte[10 * 1024 * 1024 + 1]; // 10MB + 1 byte
        assertThatThrownBy(() -> imageService.validateAndCompress(big, "image/jpeg"))
            .isInstanceOf(ImageTooLargeException.class);
    }

    @Test
    void spoofedContentTypeUsedMagicNumberNotContentType() throws Exception {
        // PNG bytes but "image/gif" claimed - should reject based on magic number
        // Actually PNG has magic 89 50 4E 47 which passes, so test with actual GIF bytes + claimed png
        byte[] gif = new byte[]{'G', 'I', 'F', '8', '9', 'a', 0, 0, 0, 0, 0, 0};
        assertThatThrownBy(() -> imageService.validateAndCompress(gif, "image/png"))
            .isInstanceOf(UnsupportedImageTypeException.class);
    }
}
