package uk.gov.hmcts.reform.fpl.service;

import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
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

@RunWith(SpringRunner.class)
public class CaseRepositoryTest {

    private static final String USER_ID = "1";
    private static final String AUTHORIZATION_TOKEN = "Bearer token";
    private static final String SERVICE_AUTHORIZATION_TOKEN = "Bearer service token";

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
    public void shouldSetSubmittedFormPDF() throws IOException {
        String caseId = "12345";
        String event = "attachSubmittedFormPDF";
        Document document = document();

        given(authTokenGenerator.generate())
            .willReturn(SERVICE_AUTHORIZATION_TOKEN);
        given(coreCaseDataApi.startEventForCaseWorker(AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, caseId, event))
            .willReturn(StartEventResponse.builder().eventId(event).token("event-token:0").build());

        caseRepository.setSubmittedFormPDF(AUTHORIZATION_TOKEN, USER_ID, caseId, document);

        verify(coreCaseDataApi).startEventForCaseWorker(AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION_TOKEN, USER_ID, JURISDICTION, CASE_TYPE, caseId, event);
        verify(coreCaseDataApi).submitEventForCaseWorker(eq(AUTHORIZATION_TOKEN), eq(SERVICE_AUTHORIZATION_TOKEN), eq(USER_ID), eq(JURISDICTION), eq(CASE_TYPE), eq(caseId), eq(true), caseDataContentArgumentCaptor.capture());

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
