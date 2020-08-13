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
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.DocumentRouter.AMEND;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.DocumentRouter.DELETE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class ManageDocumentsControllerGetDocMidEventTest extends AbstractControllerTest {

    private static final DocumentReference DOCUMENT = TestDataHelper.testDocumentReference();

    ManageDocumentsControllerGetDocMidEventTest() {
        super("manage-docs");
    }

    @Test
    void shouldShowSelectedDocumentWhenUserSelectsAmendAndChoosesDocument() {
        List<Element<CourtAdminDocument>> courtAdminDocuments = buildDocuments();

        CaseData caseData = CaseData.builder()
            .uploadDocumentsRouter(AMEND)
            .otherCourtAdminDocuments(courtAdminDocuments)
            .courtDocumentList(dynamicList(courtAdminDocuments.get(0).getId(), courtAdminDocuments.get(1).getId()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "get-doc");

        DocumentReference originalCourtDocument = mapper.convertValue(response.getData().get("originalCourtDocument"),
            DocumentReference.class);
        assertThat(originalCourtDocument).isEqualTo(courtAdminDocuments.get(0).getValue().getDocument());
    }

    //RDM-9147
    @Test
    void shouldRemoveEditedDocumentIfNoReplacementDocumentUploaded() {
        List<Element<CourtAdminDocument>> courtAdminDocuments = buildDocuments();

        CaseData caseData = CaseData.builder()
            .uploadDocumentsRouter(AMEND)
            .otherCourtAdminDocuments(courtAdminDocuments)
            .editedCourtDocument(new CourtAdminDocument("", DocumentReference.builder().build()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "get-doc");
        assertThat(response.getData()).doesNotContainKey("editedCourtDocument");
    }

    @Test
    void shouldShowDocumentToBeDeletedWhenUserSelectsDeleteAndChoosesDocument() {
        List<Element<CourtAdminDocument>> courtAdminDocuments = buildDocuments();

        CaseData caseData = CaseData.builder()
            .uploadDocumentsRouter(DELETE)
            .otherCourtAdminDocuments(courtAdminDocuments)
            .courtDocumentList(dynamicList(courtAdminDocuments.get(0).getId(), courtAdminDocuments.get(1).getId()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "get-doc");

        CourtAdminDocument deletedCourtDocument = mapper.convertValue(response.getData().get("deletedCourtDocument"),
            CourtAdminDocument.class);
        assertThat(deletedCourtDocument).isEqualTo(courtAdminDocuments.get(0).getValue());
    }

    private List<Element<CourtAdminDocument>> buildDocuments() {
        return List.of(element(new CourtAdminDocument("Document 1", DOCUMENT)),
            element(new CourtAdminDocument("Document 2", DOCUMENT)));
    }

    private DynamicList dynamicList(UUID uuid1, UUID uuid2) {
        DynamicListElement listElement1 = DynamicListElement.builder()
            .code(uuid1)
            .label("Document 1")
            .build();

        DynamicListElement listElement2 = DynamicListElement.builder()
            .code(uuid2)
            .label("Document 2")
            .build();

        List<DynamicListElement> listItems = new ArrayList<>(List.of(listElement1, listElement2));

        return DynamicList.builder()
            .value(listElement1)
            .listItems(listItems)
            .build();
    }
}
