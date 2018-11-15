package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceReaderTest {

    @Test
    public void shouldReturnStringIfResourceExists() {
        String content = ResourceReader.readString("sample-resource.txt");
        assertThat(content).contains("Sample content");
    }

    @Test
    public void shouldThrowExceptionWhileReadingStringIfResourceDoesNotExist() {
        Assertions.assertThrows(Exception.class, () -> {
            ResourceReader.readString("non-existing-resource.txt");
        });
    }

    @Test
    public void shouldReturnBytesIfResourceExists() {
        byte[] content = ResourceReader.readBytes("sample-resource.txt");
        assertThat(content).contains("Sample content".getBytes());
    }

    @Test
    public void shouldThrowExceptionWhileReadingBytesIfResourceDoesNotExist() {
        Assertions.assertThrows(Exception.class, () -> {
            ResourceReader.readBytes("non-existing-resource.txt");
        });
    }
}
