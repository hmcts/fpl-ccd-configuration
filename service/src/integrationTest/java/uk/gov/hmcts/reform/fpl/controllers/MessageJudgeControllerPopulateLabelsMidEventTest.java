package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.MessageRegardingDocuments;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
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

import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerPopulateLabelsMidEventTest extends AbstractCallbackTest {
    private static final UUID DYNAMIC_LIST_ITEM_ID = UUID.randomUUID();
    private static final DocumentReference DOCUMENT_REFERENCE_1 = testDocumentReference("Test Doc One");
    private static final DocumentReference DOCUMENT_REFERENCE_2 = testDocumentReference("Test Doc Two");

    MessageJudgeControllerPopulateLabelsMidEventTest() {
        super("message-judge/populate-document-labels");
    }

    @Test
    void shouldSetHearingLabelWhenNextHearingExists() {
        HearingBooking expectedNextHearing = HearingBooking.builder()
            .startDate(now().plusDays(1))
            .type(CASE_MANAGEMENT)
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(
                element(HearingBooking.builder()
                    .startDate(now().plusDays(3))
                    .type(FINAL)
                    .build()),
                element(expectedNextHearing),
                element(HearingBooking.builder()
                    .startDate(now().plusDays(5))
                    .type(ISSUE_RESOLUTION)
                    .build())
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getData().get("nextHearingLabel")).isEqualTo(
            String.format("Next hearing in the case: %s", expectedNextHearing.toLabel()));
    }

    @Test
    void shouldPopulateRelatedDocumentsFieldsWhenSendingANewJudicialMessageWithApplication() {
        DocumentReference mainDocument = DocumentReference.builder()
            .filename("c2.doc")
            .build();

        DocumentReference supportingDocument = DocumentReference.builder()
            .filename("supporting.doc")
            .build();

        SupportingEvidenceBundle supportingEvidenceBundle = SupportingEvidenceBundle.builder()
            .name("Supporting evidence")
            .document(supportingDocument)
            .build();

        UUID notSelectedBundleId = randomUUID();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundles = List.of(
            element(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .id(DYNAMIC_LIST_ITEM_ID)
                    .uploadedDateTime("1 January 2021, 12:00pm")
                    .document(mainDocument)
                    .supportingEvidenceBundle(wrapElements(supportingEvidenceBundle))
                    .build())
                .otherApplicationsBundle(OtherApplicationsBundle.builder()
                    .id(notSelectedBundleId)
                    .uploadedDateTime("1 January 2021, 12:00pm")
                    .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN)
                    .build())
                .build()
            ));

        DynamicList dynamicList = buildDynamicList(1,
            Pair.of(notSelectedBundleId, "C1, 1 January 2021, 12:00pm"),
            Pair.of(DYNAMIC_LIST_ITEM_ID, "C2, 1 January 2021, 12:00pm")
        );

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .additionalApplicationsDynamicList(dynamicList)
                .isMessageRegardingDocuments(MessageRegardingDocuments.APPLICATION)
                .build())
            .additionalApplicationsBundle(additionalApplicationsBundles)
            .build();

        String expectedDocumentLabel = mainDocument.getFilename() + "\n" + supportingDocument.getFilename();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getData().get("relatedDocumentsLabel")).isEqualTo(expectedDocumentLabel);
    }

    @Test
    void shouldPopulateRelatedDocumentsFieldsWhenSendingANewJudicialMessageWithDocument() {
        UUID notSelectedBundleId = randomUUID();

        final SkeletonArgument skeletonArgument = SkeletonArgument.builder()
            .document(DOCUMENT_REFERENCE_1)
            .build();

        final HearingCourtBundle courtBundle = HearingCourtBundle.builder()
            .courtBundle(List.of(element(notSelectedBundleId, CourtBundle.builder()
                .document(DOCUMENT_REFERENCE_2)
                .build())))
            .build();

        DynamicListElement skeletonArgumentElement = DynamicListElement.builder()
            .code(format("hearingDocuments.skeletonArgumentList###%s", DYNAMIC_LIST_ITEM_ID))
            .label(DOCUMENT_REFERENCE_1.getFilename())
            .build();

        DynamicListElement courtBundleElement = DynamicListElement.builder()
            .code(format("hearingDocuments.courtBundleListV2###%s", notSelectedBundleId))
            .label(DOCUMENT_REFERENCE_2.getFilename())
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .listItems(List.of(skeletonArgumentElement, courtBundleElement))
            .value(skeletonArgumentElement)
            .build();

        CaseData caseData = CaseData.builder()
            .messageJudgeEventData(MessageJudgeEventData.builder()
                .isMessageRegardingDocuments(MessageRegardingDocuments.DOCUMENT)
                .documentDynamicList(dynamicList)
                .build())
            .hearingDocuments(HearingDocuments.builder()
                .skeletonArgumentList(List.of(element(DYNAMIC_LIST_ITEM_ID, skeletonArgument)))
                .courtBundleListV2(List.of(element(notSelectedBundleId, courtBundle)))
                .build())
            .build();

        String expectedDocumentLabel = skeletonArgument.getDocument().getFilename();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData));

        assertThat(response.getData().get("relatedDocumentsLabel")).isEqualTo(expectedDocumentLabel);
    }
}
