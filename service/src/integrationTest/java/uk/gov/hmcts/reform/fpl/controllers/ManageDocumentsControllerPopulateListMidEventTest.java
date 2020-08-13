package uk.gov.hmcts.reform.fpl.controllers;

import org.assertj.core.api.Assertions;
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
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.DocumentRouter.UPLOAD;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.EMPTY;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.builder;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class ManageDocumentsControllerPopulateListMidEventTest extends AbstractControllerTest {

    private static final DocumentReference DOCUMENT = TestDataHelper.testDocumentReference();

    ManageDocumentsControllerPopulateListMidEventTest() {
        super("manage-docs");
    }

    @Test
    void shouldBuildDynamicListOfDocumentsWhenUserSelectsAmend() {
        List<Element<CourtAdminDocument>> courtAdminDocuments = buildDocuments();

        CaseData caseData = CaseData.builder()
            .uploadDocumentsRouter(AMEND)
            .otherCourtAdminDocuments(courtAdminDocuments)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "populate-list");
        DynamicList documentList = mapper.convertValue(response.getData().get("courtDocumentList"), DynamicList.class);

        DynamicList expectedList = dynamicList(courtAdminDocuments.get(0).getId(), courtAdminDocuments.get(1).getId());

        assertThat(documentList).isEqualTo(expectedList);
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

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "populate-list");
        assertThat(response.getData()).doesNotContainKey("editedCourtDocument");
    }

    @Test
    void shouldBuildDynamicListOfDocumentsWhenUserSelectsDelete() {
        List<Element<CourtAdminDocument>> courtAdminDocuments = buildDocuments();

        CaseData caseData = CaseData.builder()
            .uploadDocumentsRouter(DELETE)
            .otherCourtAdminDocuments(courtAdminDocuments)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "populate-list");
        DynamicList documentList = mapper.convertValue(response.getData().get("courtDocumentList"), DynamicList.class);

        DynamicList expectedList = dynamicList(courtAdminDocuments.get(0).getId(), courtAdminDocuments.get(1).getId());

        assertThat(documentList).isEqualTo(expectedList);
    }

    @Test
    void shouldNotBuildDynamicListWhenUserSelectsUpload() {
        CaseData caseData = CaseData.builder()
            .uploadDocumentsRouter(UPLOAD)
            .otherCourtAdminDocuments(buildDocuments())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "populate-list");
        Assertions.assertThat(response.getData().get("courtDocumentList")).isNull();
    }

    private List<Element<CourtAdminDocument>> buildDocuments() {
        return List.of(element(new CourtAdminDocument("Document 1", DOCUMENT)),
            element(new CourtAdminDocument("Document 2", DOCUMENT)));
    }

    private DynamicList dynamicList(UUID uuid1, UUID uuid2) {
        List<DynamicListElement> listItems = new ArrayList<>(List.of(
            builder()
                .code(uuid1)
                .label("Document 1")
                .build(),
            builder()
                .code(uuid2)
                .label("Document 2")
                .build()
        ));

        return DynamicList.builder()
            .value(EMPTY)
            .listItems(listItems)
            .build();
    }
}
