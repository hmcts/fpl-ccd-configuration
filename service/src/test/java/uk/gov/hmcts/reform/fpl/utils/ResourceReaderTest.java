package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceReaderTest {

    @Test
    public void shouldReturnBytesIfResourceExists() {
        byte[] bytes = ResourceReader.readBytes("sample-resource.txt");
        assertThat(bytes).isEqualTo("Sample content\n".getBytes());
    }

    @Test
    public void shouldThrowExceptionIfResourceDoesNotExist() throws Exception {
        Assertions.assertThrows(Exception.class, () -> {
            ResourceReader.readBytes("non-existing-resource.txt");
        });
    }
}
