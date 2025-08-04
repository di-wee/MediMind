package nus.iss.backend.util;

import org.springframework.core.io.InputStreamResource;
import java.io.InputStream;

public class ImageToApi extends InputStreamResource {

    private final String filename;

    // Forwarding images from Spring Boot to FastAPI via RestTemplate
    public ImageToApi(InputStream inputStream, String filename) {
        super(inputStream);
        this.filename = filename;
    }

    // Ensures the filename is sent in multipart request (like a real file)
    @Override
    public String getFilename() {
        return this.filename;
    }

    // We don't know the size beforehand, so return -1
    @Override
    public long contentLength() {
        return -1;
    }
}
