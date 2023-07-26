package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.event.UploadableDocumentBundle;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ManageDocumentsControllerV2.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerV2MidEventTest extends AbstractCallbackTest {

    @MockBean
    ManageDocumentService manageDocumentService;

    ManageDocumentsControllerV2MidEventTest() {
        super("manage-documentsv2");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldPopulateAllowMarkDocumentConfidential(boolean allow) {
        CaseData caseData = CaseData.builder().build();

        when(manageDocumentService.allowMarkDocumentConfidential(any())).thenReturn(allow);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-action-selection");

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getManageDocumentEventData().getAllowMarkDocumentConfidential())
            .isEqualTo(allow ? "YES" : "NO");
    }

    @ParameterizedTest
    @EnumSource(value = DocumentUploaderType.class, names = {
        "SOLICITOR", "DESIGNATED_LOCAL_AUTHORITY", "SECONDARY_LOCAL_AUTHORITY", "HMCTS", "BARRISTER"
    })
    void shouldPopulateAskForPlacementNoticeRecipientType(DocumentUploaderType uploaderType) {
        CaseData caseData = CaseData.builder().build();

        when(manageDocumentService.getUploaderType(any())).thenReturn(uploaderType);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-action-selection");

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getManageDocumentEventData().getAskForPlacementNoticeRecipientType())
            .isEqualTo(DocumentUploaderType.HMCTS.equals(uploaderType) ? "YES" : "NO");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void shouldPopulateHasConfidentialParty(int testingType) {
        CaseData caseData = null;
        switch (testingType) {
            case 1:
                caseData = CaseData.builder()
                    .confidentialChildren(List.of(element(Child.builder().build())))
                    .build();
                break;
            case 2:
                caseData = CaseData.builder()
                    .confidentialRespondents(List.of(element(Respondent.builder().build())))
                    .build();
                break;
            case 3:
                caseData = CaseData.builder()
                    .confidentialOthers(List.of(element(Other.builder().build())))
                    .build();
                break;
            case 4:
                caseData = CaseData.builder()
                    .confidentialChildren(List.of(element(Child.builder().build())))
                    .confidentialRespondents(List.of(element(Respondent.builder().build())))
                    .confidentialOthers(List.of(element(Other.builder().build())))
                    .build();
                break;
            case 5:
                caseData = CaseData.builder().build();
                break;
            default:
                throw new IllegalStateException("undefined testing type");
        };

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-action-selection");

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getManageDocumentEventData().getHasConfidentialParty())
            .isEqualTo(testingType < 5 ? "YES" : "NO");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void shouldInitialiseUploadableDocumentBundle(int testingType) {
        CaseData caseData = null;
        DynamicList expectedPlacementListDynamicList = null;
        UUID childOneId = UUID.randomUUID();

        switch (testingType) {
            case 1:
                caseData = CaseData.builder().build();
                break;
            case 2:
                caseData = CaseData.builder().placementEventData(
                    PlacementEventData.builder().placements(
                        List.of(element(childOneId, Placement.builder().childName("CHILD ONE").build()))
                    ).build()
                ).build();
                expectedPlacementListDynamicList = DynamicList.builder()
                    .value(DynamicListElement.EMPTY)
                    .listItems(List.of(DynamicListElement.builder()
                        .code(childOneId.toString())
                        .label("CHILD ONE")
                        .build()))
                    .build();
                break;
            default:
                throw new IllegalStateException("undefined testing type");
        }
        final DynamicList expectedDynamicList = DynamicList.builder().build();

        when(manageDocumentService.buildDocumentTypeDynamicList(any(), anyBoolean()))
            .thenReturn(expectedDynamicList);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-action-selection");

        CaseData responseCaseData = extractCaseData(callbackResponse);
        List<Element<UploadableDocumentBundle>> bundles = responseCaseData.getManageDocumentEventData()
            .getUploadableDocumentBundle();
        assertThat(bundles).isNotNull();
        assertThat(bundles).hasSize(1);
        assertThat(bundles.get(0).getValue().getDocumentTypeDynamicList()).isEqualTo(expectedDynamicList);
        if (testingType == 2) {
            assertThat(bundles.get(0).getValue().getPlacementList()).isEqualTo(expectedPlacementListDynamicList);
        }
    }
}
