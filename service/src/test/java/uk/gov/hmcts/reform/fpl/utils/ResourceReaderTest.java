package uk.gov.hmcts.reform.fpl.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceReaderTest {

    @Test
    public void shouldReturnStringIfResourceExists() {
        String content = ResourceReader.readString("sample-resource.txt");
        assertThat(content).contains("Sample content");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhileReadingStringIfResourceDoesNotExist() {
        ResourceReader.readString("non-existing-resource.txt");
    }

    @Test
    public void shouldReturnBytesIfResourceExists() {
        byte[] content = ResourceReader.readBytes("sample-resource.txt");
        assertThat(content).contains("Sample content".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhileReadingBytesIfResourceDoesNotExist() {
        ResourceReader.readBytes("non-existing-resource.txt");
    }
}
