package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ResourceReader {

    public static byte[] readBytes(String resourcePath) {
        try (InputStream inputStream = ResourceReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource does not exist");
            }
            return IOUtils.toByteArray(inputStream);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public String read(String input) {
        try {
            URL resource = getClass().getClassLoader().getResource(input);
            URI url = resource.toURI();
            return new String(Files.readAllBytes(Paths.get(url)), UTF_8);
        } catch (NoSuchFileException e) {
            throw new RuntimeException("no file found with the link '" + input + "'", e);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("failed to read from file '" + input + "'", e);
        }
    }
}
