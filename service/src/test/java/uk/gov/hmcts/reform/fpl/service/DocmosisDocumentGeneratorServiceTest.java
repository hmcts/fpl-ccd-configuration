package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;

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
        Map<String, String> placeholders = getTemplatePlaceholders();

        when(restTemplate.exchange(eq(tornadoUrl + "/rs/render"), eq(HttpMethod.POST), any(), eq(byte[].class)))
            .thenReturn(tornadoResponse);

        byte[] expectedResponse = {1, 2, 3};
        when(tornadoResponse.getBody()).thenReturn(expectedResponse);

        DocmosisDocument docmosisDocument = createServiceInstance().generateDocmosisDocument(placeholders, C6);
        assertThat(docmosisDocument.getBytes()).isEqualTo(expectedResponse);
    }

    @Test
    void shouldThrowNullPointerIfAnInValidDocmosisTemplateEnumIsNotProvided() {
        Map<String, String> placeholder = getTemplatePlaceholders();

        when(restTemplate.exchange(eq(tornadoUrl + "/rs/render"), eq(HttpMethod.POST), any(), eq(byte[].class)))
            .thenReturn(tornadoResponse);

        byte[] expectedResponse = {1, 2, 3};
        when(tornadoResponse.getBody()).thenReturn(expectedResponse);

        try {
            createServiceInstance().generateDocmosisDocument(placeholder, null);
        } catch (Exception ex) {
            assertThat(ex instanceof NullPointerException);
        }
    }

    private Map<String, String> getTemplatePlaceholders() {
        return Map.of(
            "jurisdiction", "PUBLICLAW",
            "familyManCaseNumber", "123",
            "todaysDate", "1 Jan 2019",
            "applicantName", "Bran Stark, Sansa Stark",
            "orderTypes", "EPO",
            "childrenNames", "Robb Stark, Jon Snow",
            "hearingDate", "2 Jan 2019",
            "hearingVenue", "Aldgate Tower floor 3",
            "preHearingAttendance", "",
            "hearingTime", "09.00pm"
        );
    }

    private DocmosisDocumentGeneratorService createServiceInstance() {
        return new DocmosisDocumentGeneratorService(
            restTemplate,
            tornadoUrl
        );
    }
}
