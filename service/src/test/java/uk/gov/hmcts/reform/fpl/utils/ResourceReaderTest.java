package uk.gov.hmcts.reform.fpl.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceReaderTest {

    @Test
    public void shouldReturnBytesIfResourceExists() {
        byte[] bytes = ResourceReader.readBytes("responses/success.json");
        assertThat(bytes).contains("documentVersions\n".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfResourceDoesNotExist() {
        ResourceReader.readBytes("non-existing-resource.txt");
    }
}
