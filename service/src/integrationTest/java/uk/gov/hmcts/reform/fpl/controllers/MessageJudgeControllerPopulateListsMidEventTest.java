package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.MessageRegardingDocuments;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.MessageJudgeEventData;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerPopulateListsMidEventTest extends AbstractCallbackTest {
    @MockBean
    ManageDocumentService manageDocumentService;
    private static final DocumentReference DOCUMENT_REFERENCE_1 = testDocumentReference("Test Doc One");
    private static final UUID DOCUMENT_1_ID = randomUUID();
    private static final DocumentReference DOCUMENT_REFERENCE_2 = testDocumentReference("Test Doc Two");
    private static final UUID DOCUMENT_2_ID = randomUUID();

    MessageJudgeControllerPopulateListsMidEventTest() {
        super("message-judge/populate-lists");
    }

    @Test
    void shouldPopulateApplicationListWhenSelectingApplicationAttachmentType() {
        SupportingEvidenceBundle supportingEvidenceBundle = SupportingEvidenceBundle.builder()
            .name("Supporting evidence")
            .document(DOCUMENT_REFERENCE_2)
            .build();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundles = List.of(
            element(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .id(DOCUMENT_1_ID)
                    .uploadedDateTime("1 January 2021, 12:00pm")
                    .document(DOCUMENT_REFERENCE_1)
                    .supportingEvidenceBundle(wrapElements(supportingEvidenceBundle))
                    .build())
                .otherApplicationsBundle(OtherApplicationsBundle.builder()
                    .id(DOCUMENT_2_ID)
                    .uploadedDateTime("1 January 2021, 12:00pm")
                    .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN)
                    .build())
                .build()
            ));

        DynamicList expectedDynamicList = buildDynamicList(
            Pair.of(DOCUMENT_2_ID, "C1, 1 January 2021, 12:00pm"),
            Pair.of(DOCUMENT_1_ID, "C2, 1 January 2021, 12:00pm")
        );

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .isMessageRegardingDocuments(MessageRegardingDocuments.APPLICATION)
                .build())
            .additionalApplicationsBundle(additionalApplicationsBundles)
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("additionalApplicationsDynamicList"), DynamicList.class
        );

        assertThat(builtDynamicList).isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldPopulateDocumentListWhenSelectingDocumentAttachmentType() {
        final SkeletonArgument skeletonArgument1 = SkeletonArgument.builder()
            .document(DOCUMENT_REFERENCE_1)
            .build();

        final SkeletonArgument skeletonArgument2 = SkeletonArgument.builder()
            .document(DOCUMENT_REFERENCE_2)
            .build();

        DynamicListElement skeletonArgumentElement1 = DynamicListElement.builder()
            .code(format("hearingDocuments.skeletonArgumentList###%s", DOCUMENT_1_ID))
            .label(DOCUMENT_REFERENCE_1.getFilename())
            .build();

        DynamicListElement skeletonArgumentElement2 = DynamicListElement.builder()
            .code(format("hearingDocuments.skeletonArgumentList###%s", DOCUMENT_2_ID))
            .label(DOCUMENT_REFERENCE_2.getFilename())
            .build();

        DynamicListElement documentTypeElement = DynamicListElement.builder()
            .code("SKELETON_ARGUMENTS")
            .label("Skeleton arguments")
            .build();

        DynamicList expectedDynamicList = DynamicList.builder()
            .listItems(List.of(skeletonArgumentElement1, skeletonArgumentElement2))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .isMessageRegardingDocuments(MessageRegardingDocuments.DOCUMENT)
                .documentTypesDynamicList(DynamicList.builder().value(documentTypeElement).build())
                .build())
            .hearingDocuments(HearingDocuments.builder()
                .skeletonArgumentList(List.of(
                    element(DOCUMENT_1_ID, skeletonArgument1), element(DOCUMENT_2_ID, skeletonArgument2)))
                .build())
            .build();

        when(manageDocumentService.getUploaderCaseRoles(any())).thenReturn(List.of(CaseRole.LAMANAGING));
        when(manageDocumentService.buildAvailableDocumentsDynamicList(any(), any())).thenReturn(expectedDynamicList);

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("documentDynamicList"), DynamicList.class
        );

        assertThat(builtDynamicList).isEqualTo(expectedDynamicList);
    }

    @Test
    void shouldReturnErrorWhenNoDocumentsOnCaseOfSelectedType() {
        final SkeletonArgument skeletonArgument1 = SkeletonArgument.builder()
            .document(DOCUMENT_REFERENCE_1)
            .build();

        DynamicListElement documentTypeElement = DynamicListElement.builder()
            .code("COURT_BUNDLE")
            .label("Court Bundles")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .isMessageRegardingDocuments(MessageRegardingDocuments.DOCUMENT)
                .documentTypesDynamicList(DynamicList.builder().value(documentTypeElement).build())
                .build())
            .hearingDocuments(HearingDocuments.builder()
                .skeletonArgumentList(List.of(
                    element(DOCUMENT_1_ID, skeletonArgument1)))
                .build())
            .build();

        when(manageDocumentService.getUploaderCaseRoles(any())).thenReturn(List.of(CaseRole.LAMANAGING));
        when(manageDocumentService.buildAvailableDocumentsDynamicList(any(), any()))
            .thenReturn(DynamicList.builder().listItems(List.of()).build());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        List<String> expectedErrors = List.of("No documents available of type: Court Bundles");

        assertThat(response.getErrors()).isEqualTo(expectedErrors);
    }
}
