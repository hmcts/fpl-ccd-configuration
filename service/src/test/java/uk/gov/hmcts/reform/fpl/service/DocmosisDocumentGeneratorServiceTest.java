package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;

import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;

@ExtendWith(SpringExtension.class)
class DocmosisDocumentGeneratorServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<byte[]> tornadoResponse;

    private String tornadoUrl = "http://tornado:5433";

    @Test
    void shouldInvokesTornado() {
        Map<String, String> placeholders = Map.of("applicant", "John Smith");

        when(restTemplate.exchange(eq(tornadoUrl), eq(HttpMethod.POST), any(), eq(byte[].class)))
            .thenReturn(tornadoResponse);

        byte[] expectedResponse = {1, 2, 3};
        when(tornadoResponse.getBody()).thenReturn(expectedResponse);

        DocmosisDocument docmosisDocument = createServiceInstance().generateDocmosisDocument(placeholders, C6);
        assertThat(docmosisDocument.getBytes()).isEqualTo(expectedResponse);
    }

    private DocmosisDocumentGeneratorService createServiceInstance() {
        return createServiceInstance(Clock.systemDefaultZone());
    }

    private DocmosisDocumentGeneratorService createServiceInstance(Clock clock) {
        return new DocmosisDocumentGeneratorService(
            restTemplate,
            tornadoUrl
        );
    }
}
