package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class ManageDocumentsControllerAboutToStartTest extends AbstractControllerTest {

    private static final DocumentReference DOCUMENT = DocumentReference.builder().build();

    ManageDocumentsControllerAboutToStartTest() {
        super("manage-docs");
    }

    @Test
    void shouldSetPageShowToYesWhenOtherCourtAdminDocumentsPresent() {

        CaseData caseData = CaseData.builder().otherCourtAdminDocuments(
            List.of(element(new CourtAdminDocument("Doc 1", DOCUMENT)))).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);
        assertThat(response.getData().get("pageShow")).isEqualTo("Yes");
    }

    @Test
    void shouldSetPageShowToNoWhenNoOtherCourtAdminDocuments() {
        CaseData caseData = CaseData.builder().otherCourtAdminDocuments(List.of()).build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);
        assertThat(response.getData().get("pageShow")).isEqualTo("No");
    }

}
