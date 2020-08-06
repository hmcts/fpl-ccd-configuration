package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtAdminDocument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerAboutToSubmitTest extends AbstractControllerTest {

    private static final DocumentReference DOCUMENT = DocumentReference.builder().build();

    ManageDocumentsControllerAboutToSubmitTest() {
        super("manage-docs");
    }

    @Test
    void shouldAppendUploadedDocumentsToOtherCourtAdminDocumentsWhenMainCollectionIsNull() {
        CourtAdminDocument courtAdminDocument = buildCourtAdminDocument("Document 1");
        CaseData caseData = CaseData.builder()
            .limitedCourtAdminDocuments(wrapElements(courtAdminDocument))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseData.getOtherCourtAdminDocuments())
            .hasSize(1)
            .extracting(Element::getValue)
            .containsOnly(courtAdminDocument);
    }

    @Test
    void shouldAppendUploadedDocumentsToOtherCourtAdminDocumentsWhenMainCollectionIsNotEmpty() {
        CourtAdminDocument document1 = buildCourtAdminDocument("Document 1");
        CourtAdminDocument document2 = buildCourtAdminDocument("Document 2");
        CaseData caseData = CaseData.builder()
            .otherCourtAdminDocuments(wrapElements(document1))
            .limitedCourtAdminDocuments(wrapElements(document2))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(asCaseDetails(caseData));

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseData.getOtherCourtAdminDocuments())
            .hasSize(2)
            .extracting(Element::getValue)
            .containsOnly(document1, document2);
    }

    private static CourtAdminDocument buildCourtAdminDocument(String name) {
        return new CourtAdminDocument(name, DOCUMENT);
    }
}
