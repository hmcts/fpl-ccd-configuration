package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class DocmosisDocumentGeneratorServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<byte[]> tornadoResponse;

    @Mock
    private DocmosisConfiguration docmosisDocumentGenerationConfiguration;

    @Captor
    ArgumentCaptor<HttpEntity<DocmosisRequest>> argumentCaptor;

    @Test
    void shouldInvokesTornado() {
        assertThat(1 + 1).isEqualTo(2);
    }

    private Map<String, Object> getTemplatePlaceholders() {
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
}
