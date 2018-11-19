package uk.gov.hmcts.reform.fpl.service;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.domain.Document;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.service.CaseRepository.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.service.CaseRepository.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;

@ExtendWith(SpringExtension.class)
class CaseRepositoryTest {

    private static final String USER_ID = "1";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    private CaseRepository caseRepository;

    @Captor
    private ArgumentCaptor<CaseDataContent> caseDataContentArgumentCaptor;

    @Test
    @SuppressWarnings({"unchecked", "LineLength"})
    void shouldSetSubmittedFormPDF() throws IOException {
        String caseId = "12345";
        String event = "attachSubmittedFormPDF";
        Document document = document();

        given(authTokenGenerator.generate())
            .willReturn(SERVICE_AUTH_TOKEN);
        given(coreCaseDataApi.startEventForCaseWorker(AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, caseId, event))
            .willReturn(StartEventResponse.builder().eventId(event).token("event-token:0").build());

        caseRepository.setSubmittedFormPDF(AUTH_TOKEN, USER_ID, caseId, document);

        verify(coreCaseDataApi).startEventForCaseWorker(AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, caseId, event);
        verify(coreCaseDataApi).submitEventForCaseWorker(eq(AUTH_TOKEN), eq(SERVICE_AUTH_TOKEN), eq(USER_ID), eq(JURISDICTION), eq(CASE_TYPE), eq(caseId), eq(true), caseDataContentArgumentCaptor.capture());

        CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
        assertThat(caseDataContent.getEvent().getId()).isEqualTo(event);
        assertThat(caseDataContent.getEventToken()).isEqualTo("event-token:0");
        assertThat((Map<String, Map<String, String>>) caseDataContent.getData())
            .containsKey("submittedForm")
            .hasValueSatisfying(new Condition<Map<String, String>>() {
                public boolean matches(Map<String, String> map) {
                    return Objects.equals(map.get("document_url"), document.links.self.href)
                        && Objects.equals(map.get("document_binary_url"), document.links.binary.href)
                        && Objects.equals(map.get("document_filename"), document.originalDocumentName);
                }
            });
    }

}
