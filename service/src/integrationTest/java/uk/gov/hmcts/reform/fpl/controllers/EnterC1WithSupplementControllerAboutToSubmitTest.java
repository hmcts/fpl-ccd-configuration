package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ManageDocumentService.class)
@OverrideAutoConfiguration(enabled = true)
class EnterC1WithSupplementControllerAboutToSubmitTest extends AbstractCallbackTest {

    @MockBean
    private ManageDocumentService manageDocumentService;

    EnterC1WithSupplementControllerAboutToSubmitTest() {
        super("enter-c1-with-supplement");
    }

    @Test
    void shouldRemoveSubmittedC1WithSupplementWhenClearFlagIsYes() {
        Map<String, Object> caseDetails = postAboutToSubmitEvent(CaseData.builder()
            .id(10L)
            .state(State.OPEN)
            .caseName("Updated CaseName")
            .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                .clearSubmittedC1WithSupplement("yes")
                .build())
            .build()).getData();

        assertThat(caseDetails.get("submittedC1WithSupplement")).isNull();
    }

    @Test
    void shouldPopulateUploaderInfos() {
        when(manageDocumentService.getUploaderType(any())).thenReturn(DESIGNATED_LOCAL_AUTHORITY);
        when(manageDocumentService.getUploaderCaseRoles(any())).thenReturn(List.of(CaseRole.LASOLICITOR));

        CaseData caseData = extractCaseData(postAboutToSubmitEvent(CaseData.builder()
            .id(10L)
            .state(State.OPEN)
            .caseName("Updated CaseName")
            .submittedC1WithSupplement(SubmittedC1WithSupplementBundle.builder()
                .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle.builder()
                    .document(DocumentReference.builder().filename("ABC.docx").build())
                    .build())))
                .build())
            .build()));

        SubmittedC1WithSupplementBundle submittedC1WithSupplementBundle = caseData.getSubmittedC1WithSupplement();
        assertThat(submittedC1WithSupplementBundle).isNotNull();
        submittedC1WithSupplementBundle.getSupportingEvidenceBundle().forEach(seb -> {
            assertThat(seb.getValue().getUploaderType()).isEqualTo(DESIGNATED_LOCAL_AUTHORITY);
            assertThat(seb.getValue().getUploaderCaseRoles()).isEqualTo(List.of(CaseRole.LASOLICITOR));
        });
    }
}
