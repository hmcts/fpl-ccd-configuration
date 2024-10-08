package uk.gov.hmcts.reform.fpl.service.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fpl.config.DocmosisConfiguration;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisRequest;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class DocmosisDocumentGeneratorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<byte[]> tornadoResponse;

    @Mock
    private DocmosisConfiguration configuration;

    @Captor
    ArgumentCaptor<HttpEntity<DocmosisRequest>> argumentCaptor;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldInvokesTornado() {
        Map<String, Object> placeholders = getTemplatePlaceholders();

        when(restTemplate.exchange(eq(configuration.getUrl() + "/api/render"),
            eq(HttpMethod.POST), argumentCaptor.capture(), eq(byte[].class))).thenReturn(tornadoResponse);

        byte[] expectedResponse = {1, 2, 3};
        when(tornadoResponse.getBody()).thenReturn(expectedResponse);

        DocmosisDocument docmosisDocument = createServiceInstance().generateDocmosisDocument(placeholders, C6);
        assertThat(docmosisDocument.getBytes()).isEqualTo(expectedResponse);

        assertThat(argumentCaptor.getValue().getBody().getTemplateName()).isEqualTo(C6.getTemplate());
        assertThat(argumentCaptor.getValue().getBody().getOutputFormat()).isEqualTo("pdf");
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

    private DocmosisDocumentGeneratorService createServiceInstance() {
        return new DocmosisDocumentGeneratorService(restTemplate, configuration, mapper);
    }
}

