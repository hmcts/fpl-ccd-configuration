package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ResourceReaderTest {

    @Test
    public void shouldReturnStringIfResourceExists() {
        String content = ResourceReader.readString("sample-resource.txt");
        assertThat(content).contains("Sample content");
    }

    @Test
    public void shouldThrowExceptionWhileReadingStringIfResourceDoesNotExist() {
        assertThatThrownBy(() -> ResourceReader.readString("non-existing-resource.txt"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldReturnBytesIfResourceExists() {
        byte[] content = ResourceReader.readBytes("sample-resource.txt");
        assertThat(content).contains("Sample content".getBytes());
    }

    @Test
    public void shouldThrowExceptionWhileReadingBytesIfResourceDoesNotExist() {
        assertThatThrownBy(() -> ResourceReader.readBytes("non-existing-resource.txt"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
