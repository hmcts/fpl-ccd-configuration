package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ResourceReader {

    private ResourceReader() {
        // NO-OP
    }

    public static String readString(String resourcePath) {
        return new String(ResourceReader.readBytes(resourcePath));
    }

    public static String readString(String resourcePath, Map<String, Object> placeholders) {
        return StringSubstitutor.replace(readString(resourcePath), placeholders);
    }

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
}
