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
    // springBoot restTemplate use this func to pre-compute size of file,
    // set HTTP header content length and stream the file in multipart data
    // wehave to override it tell spring to just stream it dont calculate
    // if remove this it will cut off prematurely when upload to fastApi
    @Override
    public long contentLength() {
        return -1;
    }
}
