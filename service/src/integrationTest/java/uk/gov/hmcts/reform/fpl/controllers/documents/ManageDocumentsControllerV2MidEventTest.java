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
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentAction;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageDocumentEventData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.event.UploadableDocumentBundle;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.DRUG_AND_ALCOHOL_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.LETTER_OF_INSTRUCTION;
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
        CaseData caseData = CaseData.builder()
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.UPLOAD_DOCUMENTS)
                .build())
            .build();

        when(manageDocumentService.allowMarkDocumentConfidential(any())).thenReturn(allow);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-action-selection");

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getManageDocumentEventData().getAllowMarkDocumentConfidential())
            .isEqualTo(allow ? "YES" : "NO");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldPopulateAllowSelectDocumentTypeToRemoveDocument(boolean allow) {
        CaseData caseData = CaseData.builder()
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .build())
            .build();

        when(manageDocumentService.allowSelectDocumentTypeToRemoveDocument(any())).thenReturn(allow);
        when(manageDocumentService.buildDocumentTypeDynamicListForRemoval(any()))
            .thenReturn(DynamicList.builder().listItems(List.of()).build());
        when(manageDocumentService.buildAvailableDocumentsToBeRemoved(any()))
            .thenReturn(DynamicList.builder().listItems(List.of()).build());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-action-selection");

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getManageDocumentEventData().getAllowSelectDocumentTypeToRemoveDocument())
            .isEqualTo(allow ? "YES" : "NO");
    }

    @ParameterizedTest
    @EnumSource(value = DocumentUploaderType.class, names = {
        "SOLICITOR", "DESIGNATED_LOCAL_AUTHORITY", "SECONDARY_LOCAL_AUTHORITY", "HMCTS", "BARRISTER", "CAFCASS"
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

        when(manageDocumentService.buildDocumentTypeDynamicList(any())).thenReturn(expectedDynamicList);

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

    @Test
    void shouldReturnNoDocumentErrorMessageWhenThereIsNoAvailableDocumentToBeRemoved() {
        CaseData caseData = CaseData.builder()
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .build())
            .build();

        when(manageDocumentService.allowSelectDocumentTypeToRemoveDocument(any())).thenReturn(true);
        when(manageDocumentService.buildDocumentTypeDynamicListForRemoval(any())).thenReturn(DynamicList.builder()
            .listItems(List.of())
            .build());

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-action-selection");
        extractCaseData(callbackResponse);
        assertThat(callbackResponse.getErrors()).contains("There is no document to be removed.");

        when(manageDocumentService.allowSelectDocumentTypeToRemoveDocument(any())).thenReturn(false);
        when(manageDocumentService.buildAvailableDocumentsToBeRemoved(any())).thenReturn(DynamicList.builder()
            .listItems(List.of())
            .build());

        callbackResponse = postMidEvent(caseData,"manage-document-action-selection");
        extractCaseData(callbackResponse);
        assertThat(callbackResponse.getErrors()).contains("There is no document to be removed.");
    }

    @Test
    void shouldContainErrorIfSelectedDocumentTypeIsNonUploadableInUploadDocumentAction() {
        Arrays.stream(new DocumentType[] {
            DocumentType.AA_PARENT_APPLICANTS_DOCUMENTS, DocumentType.AA_PARENT_EXPERT_REPORTS,
            DocumentType.AA_PARENT_ORDERS, DocumentType.AA_PARENT_RESPONDENTS_STATEMENTS
        }).forEach(documentType -> {
            CaseData caseData = CaseData.builder()
                .manageDocumentEventData(ManageDocumentEventData.builder()
                    .manageDocumentAction(ManageDocumentAction.UPLOAD_DOCUMENTS)
                    .uploadableDocumentBundle(List.of(element(UploadableDocumentBundle.builder()
                        .documentTypeDynamicList(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                .code(documentType.name())
                                .build())
                            .build())
                        .build())))
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
                "manage-document-upload-new-doc");
            assertThat(callbackResponse.getErrors()).contains("You are trying to upload a document to a parent folder, "
                + "you need to choose one of the available sub folders.");
        });
    }

    @Test
    void shouldContainErrorIfMultipleSelectedDocumentTypesAreNonUploadableInUploadDocumentAction() {
        Arrays.stream(new DocumentType[] {
            DocumentType.AA_PARENT_APPLICANTS_DOCUMENTS, DocumentType.AA_PARENT_EXPERT_REPORTS,
            DocumentType.AA_PARENT_ORDERS, DocumentType.AA_PARENT_RESPONDENTS_STATEMENTS
        }).forEach(documentType -> {
            CaseData caseData = CaseData.builder()
                .manageDocumentEventData(ManageDocumentEventData.builder()
                    .manageDocumentAction(ManageDocumentAction.UPLOAD_DOCUMENTS)
                    .uploadableDocumentBundle(List.of(
                        element(UploadableDocumentBundle.builder()
                            .documentTypeDynamicList(DynamicList.builder()
                                .value(DynamicListElement.builder()
                                    .code(documentType.name())
                                    .build())
                                .build())
                            .build()),
                        element(UploadableDocumentBundle.builder()
                            .documentTypeDynamicList(DynamicList.builder()
                                .value(DynamicListElement.builder()
                                    .code(documentType.name())
                                    .build())
                                .build())
                            .build())))
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
                "manage-document-upload-new-doc");
            assertThat(callbackResponse.getErrors()).contains("You are trying to upload a document to a parent folder, "
                + "you need to choose one of the available sub folders.");
        });
    }

    @Test
    void shouldContainErrorIfOneOfTheSelectedDocumentTypesIsNonUploadableInUploadDocumentAction() {
        Arrays.stream(new DocumentType[] {
            DocumentType.AA_PARENT_APPLICANTS_DOCUMENTS, DocumentType.AA_PARENT_EXPERT_REPORTS,
            DocumentType.AA_PARENT_ORDERS, DocumentType.AA_PARENT_RESPONDENTS_STATEMENTS
        }).forEach(documentType -> {
            CaseData caseData = CaseData.builder()
                .manageDocumentEventData(ManageDocumentEventData.builder()
                    .manageDocumentAction(ManageDocumentAction.UPLOAD_DOCUMENTS)
                    .uploadableDocumentBundle(List.of(
                        element(UploadableDocumentBundle.builder()
                            .documentTypeDynamicList(DynamicList.builder()
                                .value(DynamicListElement.builder()
                                    .code(documentType.name())
                                    .build())
                                .build())
                            .build()),
                        element(UploadableDocumentBundle.builder()
                            .documentTypeDynamicList(DynamicList.builder()
                                .value(DynamicListElement.builder()
                                    .code(DocumentType.CASE_SUMMARY.name())
                                    .build())
                                .build())
                            .build())))
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
                "manage-document-upload-new-doc");
            assertThat(callbackResponse.getErrors()).contains("You are trying to upload a document to a parent folder, "
                + "you need to choose one of the available sub folders.");
        });
    }

    @Test
    void shouldNotContainErrorIfSelectedDocumentTypeIsUploadableInUploadDocumentAction() {
        Arrays.stream(new DocumentType[] {
            DocumentType.COURT_BUNDLE,
            DocumentType.CASE_SUMMARY,
            DocumentType.POSITION_STATEMENTS,
            DocumentType.THRESHOLD,
            DocumentType.SKELETON_ARGUMENTS,
            DocumentType.JUDGEMENTS,
            DocumentType.TRANSCRIPTS,
            DocumentType.DOCUMENTS_FILED_ON_ISSUE,
            DocumentType.APPLICANTS_WITNESS_STATEMENTS,
            DocumentType.CARE_PLAN,
            DocumentType.PARENT_ASSESSMENTS,
            DocumentType.FAMILY_AND_VIABILITY_ASSESSMENTS,
            DocumentType.APPLICANTS_OTHER_DOCUMENTS,
            DocumentType.MEETING_NOTES,
            DocumentType.CONTACT_NOTES,
            DocumentType.RESPONDENTS_STATEMENTS,
            DocumentType.RESPONDENTS_WITNESS_STATEMENTS,
            DocumentType.GUARDIAN_EVIDENCE,
            DocumentType.FAMILY_CENTRE_ASSESSMENTS_NON_RESIDENTIAL,
            DocumentType.ADULT_PSYCHIATRIC_REPORT_ON_PARENTS,
            DocumentType.FAMILY_CENTRE_ASSESSMENTS_RESIDENTIAL,
            DocumentType.HAEMATOLOGIST,
            DocumentType.INDEPENDENT_SOCIAL_WORKER,
            DocumentType.MULTI_DISCIPLINARY_ASSESSMENT,
            DocumentType.NEUROSURGEON,
            DocumentType.OPHTHALMOLOGIST,
            DocumentType.OTHER_EXPERT_REPORT,
            DocumentType.OTHER_MEDICAL_REPORT,
            DocumentType.PEDIATRIC,
            DocumentType.PEDIATRIC_RADIOLOGIST,
            DocumentType.PROFESSIONAL_DNA_TESTING,
            DocumentType.PROFESSIONAL_DRUG_ALCOHOL,
            DocumentType.PROFESSIONAL_HAIR_STRAND,
            DocumentType.PROFESSIONAL_OTHER,
            DocumentType.PSYCHIATRIC_CHILD_ONLY,
            DocumentType.PSYCHIATRIC_CHILD_AND_PARENT,
            DocumentType.PSYCHOLOGICAL_REPORT_CHILD_ONLY_CLINICAL,
            DocumentType.PSYCHOLOGICAL_REPORT_CHILD_ONLY_EDUCATIONAL,
            DocumentType.PSYCHOLOGICAL_REPORT_PARENT_AND_CHILD,
            DocumentType.PSYCHOLOGICAL_REPORT_PARENT_FULL_COGNITIVE,
            DocumentType.PSYCHOLOGICAL_REPORT_PARENT_FULL_FUNCTIONING,
            DocumentType.TOXICOLOGY_REPORT,
            DocumentType.POLICE_DISCLOSURE,
            DocumentType.MEDICAL_RECORDS,
            DocumentType.COURT_CORRESPONDENCE,
            DocumentType.NOTICE_OF_ACTING_OR_ISSUE,
            DocumentType.PLACEMENT_RESPONSES
        }).forEach(documentType -> {
            CaseData caseData = CaseData.builder()
                .manageDocumentEventData(ManageDocumentEventData.builder()
                    .manageDocumentAction(ManageDocumentAction.UPLOAD_DOCUMENTS)
                    .uploadableDocumentBundle(List.of(element(UploadableDocumentBundle.builder()
                        .documentTypeDynamicList(DynamicList.builder()
                            .value(DynamicListElement.builder()
                                .code(documentType.name())
                                .build())
                            .build())
                        .build())))
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
                "manage-document-upload-new-doc");
            assertThat(callbackResponse.getErrors()).isNull();
        });
    }

    @Test
    void shouldNotContainErrorIfMultipleSelectedDocumentTypesAreUploadableInUploadDocumentAction() {
        Arrays.stream(new DocumentType[] {
            DocumentType.COURT_BUNDLE,
            DocumentType.CASE_SUMMARY,
            DocumentType.POSITION_STATEMENTS,
            DocumentType.THRESHOLD,
            DocumentType.SKELETON_ARGUMENTS,
            DocumentType.JUDGEMENTS,
            DocumentType.TRANSCRIPTS,
            DocumentType.DOCUMENTS_FILED_ON_ISSUE,
            DocumentType.APPLICANTS_WITNESS_STATEMENTS,
            DocumentType.CARE_PLAN,
            DocumentType.PARENT_ASSESSMENTS,
            DocumentType.FAMILY_AND_VIABILITY_ASSESSMENTS,
            DocumentType.APPLICANTS_OTHER_DOCUMENTS,
            DocumentType.MEETING_NOTES,
            DocumentType.CONTACT_NOTES,
            DocumentType.RESPONDENTS_STATEMENTS,
            DocumentType.RESPONDENTS_WITNESS_STATEMENTS,
            DocumentType.GUARDIAN_EVIDENCE,
            DocumentType.FAMILY_CENTRE_ASSESSMENTS_NON_RESIDENTIAL,
            DocumentType.ADULT_PSYCHIATRIC_REPORT_ON_PARENTS,
            DocumentType.FAMILY_CENTRE_ASSESSMENTS_RESIDENTIAL,
            DocumentType.HAEMATOLOGIST,
            DocumentType.INDEPENDENT_SOCIAL_WORKER,
            DocumentType.MULTI_DISCIPLINARY_ASSESSMENT,
            DocumentType.NEUROSURGEON,
            DocumentType.OPHTHALMOLOGIST,
            DocumentType.OTHER_EXPERT_REPORT,
            DocumentType.OTHER_MEDICAL_REPORT,
            DocumentType.PEDIATRIC,
            DocumentType.PEDIATRIC_RADIOLOGIST,
            DocumentType.PROFESSIONAL_DNA_TESTING,
            DocumentType.PROFESSIONAL_DRUG_ALCOHOL,
            DocumentType.PROFESSIONAL_HAIR_STRAND,
            DocumentType.PROFESSIONAL_OTHER,
            DocumentType.PSYCHIATRIC_CHILD_ONLY,
            DocumentType.PSYCHIATRIC_CHILD_AND_PARENT,
            DocumentType.PSYCHOLOGICAL_REPORT_CHILD_ONLY_CLINICAL,
            DocumentType.PSYCHOLOGICAL_REPORT_CHILD_ONLY_EDUCATIONAL,
            DocumentType.PSYCHOLOGICAL_REPORT_PARENT_AND_CHILD,
            DocumentType.PSYCHOLOGICAL_REPORT_PARENT_FULL_COGNITIVE,
            DocumentType.PSYCHOLOGICAL_REPORT_PARENT_FULL_FUNCTIONING,
            DocumentType.TOXICOLOGY_REPORT,
            DocumentType.POLICE_DISCLOSURE,
            DocumentType.MEDICAL_RECORDS,
            DocumentType.COURT_CORRESPONDENCE,
            DocumentType.NOTICE_OF_ACTING_OR_ISSUE,
            DocumentType.PLACEMENT_RESPONSES
        }).forEach(documentType -> {
            CaseData caseData = CaseData.builder()
                .manageDocumentEventData(ManageDocumentEventData.builder()
                    .manageDocumentAction(ManageDocumentAction.UPLOAD_DOCUMENTS)
                    .uploadableDocumentBundle(List.of(
                        element(UploadableDocumentBundle.builder()
                            .documentTypeDynamicList(DynamicList.builder()
                                .value(DynamicListElement.builder()
                                    .code(documentType.name())
                                    .build())
                                .build())
                            .build()),
                        element(UploadableDocumentBundle.builder()
                            .documentTypeDynamicList(DynamicList.builder()
                                .value(DynamicListElement.builder()
                                    .code(documentType.name())
                                    .build())
                                .build())
                            .build())))
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
                "manage-document-upload-new-doc");
            assertThat(callbackResponse.getErrors()).isNull();
        });
    }

    @Test
    void shouldPopulateDocumentsToBeRemovedAfterSelectingDocumentType() {
        CaseData caseData = CaseData.builder()
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .availableDocumentTypesForRemoval(DynamicList.builder()
                    .value(DynamicListElement.builder().code(DocumentType.CASE_SUMMARY.name()).build())
                    .build())
                .build())
            .build();

        DynamicList expectedDynamicList = DynamicList.builder().listItems(List.of()).build();
        when(manageDocumentService.buildAvailableDocumentsToBeRemoved(any(), eq(DocumentType.CASE_SUMMARY)))
            .thenReturn(expectedDynamicList);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-type-selection");

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getManageDocumentEventData()
            .getDocumentsToBeRemoved())
            .isEqualTo(expectedDynamicList);
    }

    @ParameterizedTest
    @EnumSource(value = DocumentType.class, names = {"POSITION_STATEMENTS_CHILD", "POSITION_STATEMENTS_RESPONDENT",
        "ARCHIVED_DOCUMENTS", "EXPERT_REPORTS", "DRUG_AND_ALCOHOL_REPORTS", "LETTER_OF_INSTRUCTION"})
    void shouldPopulateDocumentsToBeRemovedAfterSelectingLegacyDocument(DocumentType documentType) {
        CaseData caseData = CaseData.builder()
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .availableDocumentTypesForRemoval(DynamicList.builder()
                    .value(DynamicListElement.builder().code(documentType.name()).build())
                    .build())
                .build())
            .build();

        DynamicList expectedDynamicList = DynamicList.builder().listItems(List.of()).build();
        when(manageDocumentService.buildAvailableDocumentsToBeRemoved(any(), eq(documentType)))
            .thenReturn(expectedDynamicList);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-type-selection");

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getManageDocumentEventData()
            .getDocumentsToBeRemoved())
            .isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldGetErrorWhenSelectingAnInvalidDocumentType() {
        CaseData caseData = CaseData.builder()
            .manageDocumentEventData(ManageDocumentEventData.builder()
                .manageDocumentAction(ManageDocumentAction.REMOVE_DOCUMENTS)
                .availableDocumentTypesForRemoval(DynamicList.builder()
                    .value(DynamicListElement.builder().code(DocumentType.AA_PARENT_EXPERT_REPORTS.name()).build())
                    .build())
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-type-selection");

        extractCaseData(callbackResponse);

        assertThat(callbackResponse.getErrors()).contains("You are trying to remove a document from a parent folder, "
            + "or a document that is not uploadable, "
            + "you need to choose one of the available sub folders.");
    }
}
