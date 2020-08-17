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
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ManageDocumentsAction.AMEND;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ManageDocumentsAction.DELETE;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ManageDocumentsAction.UPLOAD;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
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
            .newCourtDocuments(wrapElements(courtAdminDocument))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

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
            .newCourtDocuments(wrapElements(document2))
            .manageDocumentsAction(UPLOAD)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseData.getOtherCourtAdminDocuments())
            .hasSize(2)
            .extracting(Element::getValue)
            .containsOnly(document1, document2);
    }

    @Test
    void shouldUpdateDocumentListWithReplacementDocumentWhenUserSelectsAmend() {
        List<Element<CourtAdminDocument>> courtAdminDocuments = buildDocuments();
        CourtAdminDocument editedDocument = new CourtAdminDocument("Replacement Document",
            TestDataHelper.testDocumentReference());

        CaseData caseData = CaseData.builder()
            .otherCourtAdminDocuments(courtAdminDocuments)
            .manageDocumentsAction(AMEND)
            .editedCourtDocument(editedDocument)
            .courtDocumentList(dynamicListWithFirstElementSelected(
                courtAdminDocuments.get(0).getId(), courtAdminDocuments.get(1).getId()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseData.getOtherCourtAdminDocuments().get(0).getValue()).isEqualTo(editedDocument);
    }

    @Test
    void shouldRemoveDocumentFromDocumentListWhenUserSelectsDelete() {
        List<Element<CourtAdminDocument>> courtAdminDocuments = buildDocuments();

        CaseData caseData = CaseData.builder()
            .otherCourtAdminDocuments(courtAdminDocuments)
            .manageDocumentsAction(DELETE)
            .courtDocumentList(dynamicListWithFirstElementSelected(
                courtAdminDocuments.get(0).getId(), courtAdminDocuments.get(1).getId()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        CaseData responseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseData.getOtherCourtAdminDocuments()).doesNotContain(courtAdminDocuments.get(0));
    }

    @Test
    void shouldRemoveIntermediaryCollection() {
        CourtAdminDocument document1 = buildCourtAdminDocument("Document 1");
        CaseData caseData = CaseData.builder()
            .newCourtDocuments(wrapElements(document1))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData);

        assertThat(response.getData()).doesNotContainKey("newCourtDocuments");
    }

    private static List<Element<CourtAdminDocument>> buildDocuments() {
        return List.of(element(buildCourtAdminDocument("Document 1")),
            element(buildCourtAdminDocument("Document 2")));
    }

    private static CourtAdminDocument buildCourtAdminDocument(String name) {
        return new CourtAdminDocument(name, DOCUMENT);
    }

    private static DynamicList dynamicListWithFirstElementSelected(UUID uuid1, UUID uuid2) {
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
