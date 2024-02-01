package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(ManageDocumentService.class)
@OverrideAutoConfiguration(enabled = true)
class EnterC1WithSupplementControllerAboutToStartTest extends AbstractCallbackTest {

    @MockBean
    private ManageDocumentService manageDocumentService;

    EnterC1WithSupplementControllerAboutToStartTest() {
        super("enter-c1-with-supplement");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldPopulateIsDocumentUploadedAndClearSubmittedC1WithSupplement() {
        Map<String, Object> caseDetails =  postAboutToStartEvent(CaseData.builder()
            .id(10L)
            .state(State.OPEN)
            .caseName("CaseName")
            .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                .document(DocumentReference.builder().filename("ABC.docx").build())
                .build())
            .build()).getData();

        assertThat(caseDetails.get("submittedC1WithSupplement")).isNotNull();
        Map<String, Object> submittedC1WithSupplement = (Map<String, Object>) caseDetails
            .get("submittedC1WithSupplement");
        assertThat(submittedC1WithSupplement).extracting("isDocumentUploaded").isEqualTo("YES");
        assertThat(submittedC1WithSupplement).extracting("clearSubmittedC1WithSupplement")
            .isEqualTo("NO");
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldPopulateIsDocumentUploadedAndClearSubmittedC1WithSupplementWithoutDocument() {
        Map<String, Object> caseDetails =  postAboutToStartEvent(CaseData.builder()
            .id(10L)
            .state(State.OPEN)
            .caseName("CaseName")
            .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                .build())
            .build()).getData();

        assertThat(caseDetails.get("submittedC1WithSupplement")).isNotNull();
        Map<String, Object> submittedC1WithSupplement = (Map<String, Object>) caseDetails
            .get("submittedC1WithSupplement");
        assertThat(submittedC1WithSupplement).extracting("isDocumentUploaded").isEqualTo("NO");
        assertThat(submittedC1WithSupplement).extracting("clearSubmittedC1WithSupplement").isNull();
    }
}
